package net.pixelsystems;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import net.pixelsystems.StateUtil.CLIENT_STATE;
import net.pixelsystems.server.CameraData;
import net.pixelsystems.server.ServerConnectionException;
import net.pixelsystems.server.ServerConnector;
import net.pixelsystems.thread.FeedbackEvent;
import net.pixelsystems.thread.FeedbackWorker;
import net.pixelsystems.thread.PingClass;
import net.pixelsystems.thread.ServerFeedbackEvent;
import net.pixelsystems.thread.ThreadFeedback;

public class WhosHomeDialog extends JDialog implements ActionListener,WhosHomeThreadHandler,ThreadFeedback {
	private static final String CLOSE_BUTTON="CLOSE_BUTTON";
	private static final String START_BUTTON="START_BUTTON";
	private static final String SERVER_CONNECT_BUTTON="SERVER_CONNECT_BUTTON";
	private static final String NAME_COLUMN="Name";
	private static final String ENABLED_COLUMN="Enabled";
	private static final String IS_MOTION_DETECT="Is Motion Detecting";
	private static final String IS_INCLUDED="Include?";

	private JLabel appStatusLabel = new JLabel("Status:");
	private JLabel serverStatusLabel = new JLabel("Status:");
	private JLabel hostsStatusLabel = new JLabel("Status:");
	private JTextField willCell = new JTextField(15);
	private JTextField lauraCell = new JTextField(15);
	private MyTableModel camTableModel = new MyTableModel();
	private JTable camTable = null;
	private JTextField timeBeforeCheck = new JTextField(3);
	private StateUtil.APP_STATUS appStatus=StateUtil.APP_STATUS.DISABLED;
	private StateUtil.CLIENT_STATE hostStatus = StateUtil.CLIENT_STATE.MISSING;
	private StateUtil.SERVER_STATUS serverStatus = StateUtil.SERVER_STATUS.DISCONNECTED;	
	private Timer tickerTimer=new Timer();
	private int connectionTimerLength;
	private ServerConnector connector=null;
	private JPasswordField ncsServerPassword= new JPasswordField(8);
	private JTextField ncsServerIP= new JTextField(15);
	private JTextField ncsServerPort = new JTextField(4);
	private JTextField ncsServerUserName = new JTextField(10);
	private JButton ncsServerConnect = new JButton("Connect...");
	List<PingClass>clients = new ArrayList<PingClass>();
	private TickClass ticker;
	
	
	class MyTableModel extends AbstractTableModel{
		private String[]columnNames=new String[]{NAME_COLUMN,ENABLED_COLUMN,IS_MOTION_DETECT,IS_INCLUDED};

		List<CameraData>cams = new ArrayList<CameraData>();
		Map<CameraData,Boolean>includeCam=new HashMap<CameraData,Boolean>();
		
		public void addCam(CameraData cam){
			cams.add(cam);
			includeCam.put(cam, true);
			fireTableDataChanged();
			//fireTableCellUpdated(cams.size()-1, 0);
		}
		
		@Override
		public void setValueAt(Object value,int row, int col){
			if(col==3){
				CameraData cam = cams.get(row);
				includeCam.put(cam, (Boolean)value);
				fireTableCellUpdated(row,col);
			}
		}
		@Override
		public int getRowCount() {
			return cams.size();
		}

		@Override
		public String getColumnName(int col){
			return columnNames[col];
		}
		@Override
		public Class getColumnClass(int c){
			if(c==0){
				return String.class;
			}
			return Boolean.class;
			
		}
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			CameraData cam = cams.get(rowIndex);
			switch(columnIndex){
			case 0:
				return cam.getName();
			case 1:
				return cam.isEnabled();
			case 2:
				return cam.isMotionDetector();
			case 3:
				return includeCam.get(cam);
			}
			return "UNKNOWN";
		}
		
		@Override
		public boolean isCellEditable(int row,int col){
			if(col==3){
				return true;
			}
			return false;
		}
		
	}
	
	public WhosHomeDialog(){
		super((Frame)null,true);
		setTitle("WhosHome v1.0");
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		
		JPanel buttonPanel = getButtonPanel();
		JPanel mainPanel = getMainPanel();
		JPanel topPanel = getTopPanel();
		content.add(topPanel,BorderLayout.NORTH);
		content.add(mainPanel,BorderLayout.CENTER);
		content.add(buttonPanel,BorderLayout.SOUTH);
		setSize(500, 620);
		setStatus("Waiting...","Waiting...","Waiting...");
	}
	private JPanel getButtonPanel(){
		JPanel buttonPanel = new JPanel();
		JButton closeButton = new JButton("Close");
		JToggleButton startButton = new JToggleButton("Start");
		buttonPanel.add(startButton);
		startButton.setName(START_BUTTON);
		buttonPanel.add(closeButton);
		closeButton.setName(CLOSE_BUTTON);
		closeButton.addActionListener(this);
		startButton.addActionListener(this);
		return buttonPanel;
	}
	
	private JPanel getMainPanel(){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());		
		GridBagConstraints gbcmonitors = new GridBagConstraints();
		
		JPanel monitorsPanel = new JPanel(new GridBagLayout());
		monitorsPanel.setBorder(new TitledBorder("Monitors:"));
		
		
		gbcmonitors.gridx=0;
		gbcmonitors.gridy=0;
		gbcmonitors.insets=new Insets(3,3,3,3);
		gbcmonitors.anchor=GridBagConstraints.LINE_START;
		// row 1
		monitorsPanel.add(new JLabel("Will Cell IP:"),gbcmonitors);
		gbcmonitors.gridx=1;
		
		monitorsPanel.add(willCell,gbcmonitors);
		
		// row 2
		gbcmonitors.gridy=1;
		gbcmonitors.gridx=0;
		monitorsPanel.add(new JLabel("Laura Cell IP:"),gbcmonitors);
		gbcmonitors.gridx=1;
		monitorsPanel.add(lauraCell,gbcmonitors);
		
		// row 3
		gbcmonitors.gridy=2;
		gbcmonitors.gridx=0;
		monitorsPanel.add(new JLabel("Time before check (sec):"),gbcmonitors);
		gbcmonitors.gridx=1;
		timeBeforeCheck.setText("5");
		monitorsPanel.add(timeBeforeCheck,gbcmonitors);
		
		JPanel serverPanel = new JPanel(new GridBagLayout());
		serverPanel.setBorder(new TitledBorder("Server Connection:"));
		GridBagConstraints gbcncs = new GridBagConstraints();
		gbcncs.insets=new Insets(3,3,3,3);
		gbcncs.anchor=GridBagConstraints.LINE_START;
		gbcncs.gridy=0;
		gbcncs.gridx=0;
		serverPanel.add(new JLabel("NCS Server IP:"),gbcncs);
		gbcncs.gridx=1;
		ncsServerIP.setText("127.0.0.1");
		serverPanel.add(ncsServerIP,gbcncs);

		gbcncs.gridy=1;
		gbcncs.gridx=0;
		serverPanel.add(new JLabel("NCS Server Port:"),gbcncs);
		gbcncs.gridx=1;
		ncsServerPort.setText("8124");
		serverPanel.add(ncsServerPort,gbcncs);


		gbcncs.gridy=2;
		gbcncs.gridx=0;
		serverPanel.add(new JLabel("NCS Server User Name:"),gbcncs);
		gbcncs.gridx=1;
		ncsServerUserName.setText("Admin");
		serverPanel.add(ncsServerUserName,gbcncs);

		gbcncs.gridy=3;
		gbcncs.gridx=0;
		serverPanel.add(new JLabel("NCS Server User Password:"),gbcncs);
		gbcncs.gridx=1;
		ncsServerPassword.setText("0inluoyn");
		serverPanel.add(ncsServerPassword,gbcncs);
		gbcncs.gridy=4;
		//34.236.193.204
		ncsServerConnect.setName(SERVER_CONNECT_BUTTON);
		ncsServerConnect.addActionListener(this);
		serverPanel.add(ncsServerConnect,gbcncs);

		GridBagConstraints gbcoverall = new GridBagConstraints();
		gbcoverall.fill=GridBagConstraints.BOTH;
		gbcoverall.insets=new Insets(6,6,6,6);
		mainPanel.add(monitorsPanel,gbcoverall);
		gbcoverall.gridy=1;
		mainPanel.add(serverPanel,gbcoverall);
		return mainPanel;
	}

	private JPanel getTopPanel(){
		JPanel topPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets= new Insets(6, 6, 6, 6);
		gbc.gridx=0;
		gbc.gridy=0;
		topPanel.add(serverStatusLabel,gbc);
		gbc.gridy=1;
		topPanel.add(hostsStatusLabel,gbc);
		gbc.gridy=2;
		topPanel.add(appStatusLabel,gbc);
		gbc.gridy=3;
		JPanel camPanel = new JPanel();
		camPanel.setBorder(new TitledBorder("Defined Cameras:"));

		camTable = new JTable(camTableModel);
		camTable.setPreferredScrollableViewportSize(new Dimension(400,100));
		camTable.setFillsViewportHeight(true);
		JScrollPane scrollPane = new JScrollPane(camTable);
		camTable.getColumnModel().getColumn(2).setPreferredWidth(200);
		//camTableModel.addCam(new CameraData("Test",true,true));
		camTable.updateUI();
		camPanel.add(scrollPane, gbc);
		topPanel.add(camPanel,gbc);
		return topPanel;
	}
	
	private void setStatus(String appStatusText,String serverStatusTest, String hostStatusTest){
		if(appStatusText!=null){
			appStatusLabel.setText("[App Status]:"+appStatusText);
		}
		if(serverStatusTest!=null){
			serverStatusLabel.setText("[Server Status]:"+serverStatusTest);
		}
		if(hostStatusTest!=null){
			hostsStatusLabel.setText("[Client Status]:"+hostStatusTest);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source instanceof Component){
			Component sourceComp = (Component)source;
			if(sourceComp.getName().equals(START_BUTTON)){
				JToggleButton startButton = (JToggleButton)sourceComp;
				switch(appStatus){
				case DISABLED:
					startButton.setText("Stop");
					appStatus = StateUtil.APP_STATUS.ENABLED;
					connectionTimerLength=Integer.parseInt(timeBeforeCheck.getText());
					break;
				case ENABLED:
					startButton.setText("Start");
					appStatus = StateUtil.APP_STATUS.DISABLED;
					break;
				}
				// start thread
				handleStatusChange();
			}else if(sourceComp.getName().equals(CLOSE_BUTTON)){
				appStatus = StateUtil.APP_STATUS.SHUTDOWN;
				// TODO: handle thread shutdowns
				//handleStatusChange();
				dispose();				
			}else if(sourceComp.getName().equals(SERVER_CONNECT_BUTTON)){				
				connector = new ServerConnector(ncsServerIP.getText(), ncsServerPort.getText(),this);
				String pass = new String(ncsServerPassword.getPassword());
				connector.login(ncsServerUserName.getText(), pass);						
			}
			updateUIElements();
		}		
	}
	
	private void updateUIElements(){
			willCell.setEnabled(isAppDisabled());
			lauraCell.setEnabled(isAppDisabled());
			timeBeforeCheck.setEnabled(isAppDisabled());
	}
	
	private void handleStatusChange(){
		if(!isAppDisabled()){
			int timeInSeconds = Integer.parseInt(timeBeforeCheck.getText());
			
			clients.add(new PingClass(this,willCell.getText()));
			//hosts.add(lauraConnection);// TODO: add second field
			tickerTimer.purge();
			ticker = new TickClass();
			tickerTimer.schedule(ticker, 0,1000);
			for(PingClass client:clients){
				tickerTimer.schedule(client, 1000*timeInSeconds,1000*timeInSeconds);
			}
		}else{
			timeBeforeCheck.setText(Integer.toString(connectionTimerLength));
			for(PingClass client:clients){
				client.cancel();
			}
			ticker.cancel();
			tickerTimer.purge();
		}
	}
	
	class TickClass extends TimerTask{
		public void run(){
			if(isAppDisabled()){
				int time = Integer.parseInt(timeBeforeCheck.getText());
				if(time != connectionTimerLength){
					timeBeforeCheck.setText(Integer.toString(connectionTimerLength));
				}
				return;
			}
			int time = Integer.parseInt(timeBeforeCheck.getText());
			time--;
			if(time <0){
				time = connectionTimerLength-1;
			}
			timeBeforeCheck.setText(Integer.toString(time));
			
		}
	}

	@Override
	public boolean isAppDisabled() {
		return (appStatus==StateUtil.APP_STATUS.DISABLED);
	}
	@Override
	public void setAppStatus(String appStatusText) {
		setStatus(appStatusText, null, null);
		
	}
	@Override
	public void setServerStatus(String serverStatusText) {
		setStatus(null, serverStatusText, null);
		
	}
	@Override
	public void setClientStatus(String hostStatusText) {
		setStatus(null, null, hostStatusText);
		
	}
	@Override
	public boolean isAppShuttingDown() {
		return false;
	}
	
	
	
	@Override
	public void feedbackEvent(FeedbackEvent event) {
		FeedbackWorker worker = new FeedbackWorker(event, this);
		worker.run();
	}
	
	@Override
	public void setCameraInfo(List<CameraData> data) {
		if(data!=null){
			for(CameraData cam:data){
				camTableModel.addCam(cam);
			}
			setStatus(null,"Connected...",null);
		}else{
			setStatus(null,"Could not find any cams on server",null);
		}
		
	}
	
	@Override
	public void loginOK(ServerFeedbackEvent evt) {
		setServerStatus(evt.getFeedback());
		connector.getCameras();
	}
	
	@Override
	public void setClientFeedback(PingClass pc, String feedbackTxt) {
		setClientStatus(feedbackTxt);
		if(pc!=null){
			List<String>enabledCams=new ArrayList<String>();		
			if(pc.getState()!=CLIENT_STATE.EXISTING){
				// enable security
				for(int i=0;i<camTableModel.getRowCount();i++){
					int includeIdx = camTable.getColumn(IS_INCLUDED).getModelIndex();
					Boolean applySecurity = (Boolean)camTableModel.getValueAt(i, includeIdx);
					if(applySecurity){
						int nameIdx = camTable.getColumn(NAME_COLUMN).getModelIndex();
						String camera = (String)camTableModel.getValueAt(i, nameIdx);
						enabledCams.add(camera);
					}					
				}
				connector.monitor(enabledCams);
				return;
			}
		}
	}	
}

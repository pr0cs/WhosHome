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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.pixelsystems.StateUtil.CLIENT_STATE;
import net.pixelsystems.client.Client;
import net.pixelsystems.server.CameraData;
import net.pixelsystems.server.ServerConnector;
import net.pixelsystems.thread.FeedbackEvent;
import net.pixelsystems.thread.FeedbackWorker;
import net.pixelsystems.thread.PingClass;
import net.pixelsystems.thread.ServerFeedbackEvent;
import net.pixelsystems.thread.ThreadFeedback;
import net.pixelsystems.ui.AddClientDialog;
import net.pixelsystems.ui.CameraListTableModel;
import net.pixelsystems.ui.ClientListTableModel;

@SuppressWarnings("serial")
public class WhosHomeDialog extends JDialog implements ActionListener,WhosHomeThreadHandler,ThreadFeedback, ListSelectionListener {
	private static final String CLOSE_BUTTON="CLOSE_BUTTON";
	private static final String START_BUTTON="START_BUTTON";
	private static final String ADD_CLIENT_BUTTON="ADD_CLIENT_BUTTON";
	private static final String REMOVE_CLIENT_BUTTON="REMOVE_CLIENT_BUTTON";
	private static final String SERVER_CONNECT_BUTTON="SERVER_CONNECT_BUTTON";
	public static final Icon ADD_CLIENT_ICON = new ImageIcon("resources/add-client.png");
	public static final Icon REMOVE_CLIENT_ICON = new ImageIcon("resources/remove-client.png");
	private static final String SESSION_FILE="WhosHome.json";

	private JButton addClientButton = new JButton(ADD_CLIENT_ICON);
	private JButton removeClientButton = new JButton(REMOVE_CLIENT_ICON);
	private JLabel appStatusLabel = new JLabel("Status:");
	private JLabel serverStatusLabel = new JLabel("Status:");
	private JLabel hostsStatusLabel = new JLabel("Status:");
	private CameraListTableModel camTableModel = new CameraListTableModel();
	private ClientListTableModel clientTableModel = new ClientListTableModel();
	private JTable camTable = null;
	private JTable clientTable = null;
	private JTextField timeBeforeCheck = new JTextField(3);
	private StateUtil.APP_STATUS appStatus=StateUtil.APP_STATUS.DISABLED;	
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
		setSize(500, 770);
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
		GridBagConstraints gbcClients = new GridBagConstraints();

		JPanel clientsPanel = new JPanel(new GridBagLayout());
		clientsPanel.setBorder(new TitledBorder("Client Functionality:"));


		gbcClients.gridx=0;
		gbcClients.gridy=0;		
		gbcClients.gridwidth=0;
		gbcClients.anchor=GridBagConstraints.LINE_START;
		// row 1
		JPanel clientPanel = new JPanel();
		clientPanel.setBorder(new TitledBorder("Defined Clients:"));


		clientTable = new JTable(clientTableModel);		
		clientTable.setPreferredScrollableViewportSize(new Dimension(400,100));
		clientTable.setFillsViewportHeight(true);
		JScrollPane clientScrollPane = new JScrollPane(clientTable);
		clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientTable.getSelectionModel().addListSelectionListener(this);

		//clientTable.getColumnModel().getColumn(2).setPreferredWidth(200);
		clientTable.updateUI();
		clientPanel.add(clientScrollPane);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addClientButton);
		addClientButton.addActionListener(this);
		addClientButton.setName(ADD_CLIENT_BUTTON);
		buttonPanel.add(removeClientButton);
		removeClientButton.addActionListener(this);
		removeClientButton.setName(REMOVE_CLIENT_BUTTON);
		addClientButton.setPreferredSize(new Dimension(ADD_CLIENT_ICON.getIconWidth()+6, ADD_CLIENT_ICON.getIconHeight()+6));
		addClientButton.setSize(ADD_CLIENT_ICON.getIconWidth()+6, ADD_CLIENT_ICON.getIconHeight()+6);
		addClientButton.setContentAreaFilled(false);
		addClientButton.setToolTipText("Click to add a new client to monitor");

		removeClientButton.setPreferredSize(new Dimension(REMOVE_CLIENT_ICON.getIconWidth()+6, REMOVE_CLIENT_ICON.getIconHeight()+6));
		removeClientButton.setSize(REMOVE_CLIENT_ICON.getIconWidth()+6, REMOVE_CLIENT_ICON.getIconHeight()+6);
		removeClientButton.setContentAreaFilled(false);
		removeClientButton.setToolTipText("Click to remove the currently selected client");
		removeClientButton.setEnabled(false);
		clientsPanel.add(buttonPanel,gbcClients);


		gbcClients.gridx=0;
		gbcClients.gridy=1;		
		gbcClients.gridwidth=3;
		gbcClients.insets=new Insets(3,3,3,3);
		clientsPanel.add(clientPanel,gbcClients);

		// row 3
		gbcClients.gridwidth=1;
		gbcClients.gridy=2;
		gbcClients.gridx=0;
		clientsPanel.add(new JLabel("Time before check (sec):"),gbcClients);
		gbcClients.gridx=1;
		timeBeforeCheck.setText("5");
		clientsPanel.add(timeBeforeCheck,gbcClients);

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
		mainPanel.add(clientsPanel,gbcoverall);
		gbcoverall.gridy=1;
		mainPanel.add(serverPanel,gbcoverall);
		try{
			restoreState();
		}catch(IOException ioex){
			setAppStatus(ioex.getMessage());
		}
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
		JScrollPane camScrollPane = new JScrollPane(camTable);
		camTable.getColumnModel().getColumn(2).setPreferredWidth(200);
		//camTableModel.addCam(new CameraData("Test",true,true));
		camTable.updateUI();
		camPanel.add(camScrollPane, gbc);
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
				try{
					saveState();
				}catch(IOException ioex){
					JOptionPane.showMessageDialog(this, "Error saving session:\n"+ioex.getMessage());
				}
				dispose();				
			}else if(sourceComp.getName().equals(SERVER_CONNECT_BUTTON)){				
				connector = new ServerConnector(ncsServerIP.getText(), ncsServerPort.getText(),this);
				String pass = new String(ncsServerPassword.getPassword());
				connector.login(ncsServerUserName.getText(), pass);						
			}else if(sourceComp.equals(addClientButton)){
				AddClientDialog dlg = new AddClientDialog(this, null);
				dlg.setVisible(true);
				Client result = dlg.getClient();
				if(result!=null){
					clientTableModel.addClient(result);
				}
			}else if(sourceComp.equals(removeClientButton)){
				int row =clientTable.getSelectedRow();
				clientTableModel.removeClientIndex(row);
			}
			updateUIElements();
		}		
	}

	private void updateUIElements(){
		timeBeforeCheck.setEnabled(isAppDisabled());
	}

	private void handleStatusChange(){
		if(!isAppDisabled()){
			int timeInSeconds = Integer.parseInt(timeBeforeCheck.getText());
			List<Client>tblClients =clientTableModel.getClients();
			for(Client client:tblClients){
				clients.add(new PingClass(this,client));
			}
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
			clients.clear();
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
		clientTableModel.fireTableDataChanged();
		if(pc!=null){
			List<String>enabledCams=new ArrayList<String>();		
			if(pc.getState()!=CLIENT_STATE.EXISTING){
				// enable security
				for(int i=0;i<camTableModel.getRowCount();i++){
					int includeIdx = camTableModel.getIncludedColIndex(camTable);
					Boolean applySecurity = (Boolean)camTableModel.getValueAt(i, includeIdx);
					if(applySecurity){
						int nameIdx = camTableModel.getNameColIndex(camTable);
						String camera = (String)camTableModel.getValueAt(i, nameIdx);
						enabledCams.add(camera);
					}					
				}
				Boolean result = connector.monitor(enabledCams);
				if(result){
					System.out.println("need to update table");
				}
				return;
			}
		}
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		removeClientButton.setEnabled(false);
		if(clientTable.getSelectedRow()!=-1){
			removeClientButton.setEnabled(true);
		}

	}

	public void saveState()throws IOException{
		FileOutputStream fout = new FileOutputStream(new File(SESSION_FILE));
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(fout,"UTF-8"));
		writer.beginObject();
		clientTableModel.saveState(writer);
		writer.name("timeBeforeCheck").value(timeBeforeCheck.getText());
		writer.name("ncsServerIP").value(ncsServerIP.getText());
		writer.name("ncsServerPassword").value(new String(ncsServerPassword.getPassword()));
		writer.name("ncsServerPort").value(ncsServerPort.getText());
		writer.name("ncsServerUserName").value(ncsServerUserName.getText());
		writer.endObject();
		writer.close();
	}

	public void restoreState()throws IOException{
		File state = new File(SESSION_FILE);
		if(state.exists()){
			FileInputStream fin = new FileInputStream(state);
			JsonReader reader = null;
			reader = new JsonReader(new InputStreamReader(fin,"UTF-8"));
			reader.beginObject();
			clientTableModel.restoreState(reader);
			while (reader.hasNext()) {
				String name = reader.nextName();
				if(name.equals("timeBeforeCheck")){
					timeBeforeCheck.setText(reader.nextString());
				}else if(name.equals("ncsServerIP")){
					ncsServerIP.setText(reader.nextString());
				}else if(name.equals("ncsServerPassword")){
					ncsServerPassword.setText(reader.nextString());
				}else if(name.equals("ncsServerPort")){
					ncsServerPort.setText(reader.nextString());
				}else if(name.equals("ncsServerUserName")){
					ncsServerUserName.setText(reader.nextString());
				}
			}
			reader.endObject();
			reader.close();
		}

	}
}

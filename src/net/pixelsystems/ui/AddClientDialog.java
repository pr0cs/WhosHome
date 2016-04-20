package net.pixelsystems.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.pixelsystems.client.Client;
import net.pixelsystems.client.Client.State;

@SuppressWarnings("serial")
public class AddClientDialog extends JDialog implements ActionListener {

	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");
	private JTextField name = new JTextField(20);
	private JTextField ip = new JTextField(15);
	private Client client=null;
	public AddClientDialog(JDialog parent,Client client){
		super(parent,true);
		this.client = client;
		
		setTitle("Define Client");
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		
		JPanel buttonPanel = getButtonPanel();
		JPanel mainPanel = getMainPanel();
		content.add(mainPanel,BorderLayout.CENTER);
		content.add(buttonPanel,BorderLayout.SOUTH);
		setSize(400, 150);
	}
	
	private JPanel getButtonPanel(){
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		return buttonPanel;
	}

	private JPanel getMainPanel(){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());		
		GridBagConstraints gbcClient = new GridBagConstraints();
		gbcClient.anchor=GridBagConstraints.LINE_START;
		gbcClient.insets=new Insets(3,3,3,3);
		mainPanel.add(new JLabel("Name:"),gbcClient);
		gbcClient.gridx=1;
		mainPanel.add(name, gbcClient);
		gbcClient.gridy=1;
		gbcClient.gridx=0;

		mainPanel.add(new JLabel("IP:"),gbcClient);
		gbcClient.gridx=1;
		mainPanel.add(ip, gbcClient);
		return mainPanel;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(cancelButton)){
			client=null;
			dispose();
		}else if(e.getSource().equals(okButton)){
			if(client!=null){
				client.setName(name.getText());
				client.setIP(ip.getText());
				client.setState(State.UNKNOWN);
			}else{
				client = new Client(name.getText(),ip.getText());
			}
			dispose();
		}
	}
	
	public Client getClient(){
		return client;
	}
	
}

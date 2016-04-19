package net.pixelsystems;

import net.pixelsystems.server.ServerConnectionException;
import net.pixelsystems.server.ServerConnector;

public class WhosHomeApp {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WhosHomeDialog dlg = new WhosHomeDialog();
		
		dlg.setVisible(true);
		System.exit(0);
		
		/*
		ServerConnector connector = new ServerConnector("34.236.193.204","8124");
		try {
			connector.login("Admin", "password");		
			connector.getCameras();
		} catch (ServerConnectionException e) {
			e.printStackTrace();
		}
		*/
	}

}

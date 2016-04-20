package net.pixelsystems.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

@SuppressWarnings("serial")
public class ServerConnectionException extends Exception {

	public ServerConnectionException(MalformedURLException mue) {
		super(mue);
	}

	public ServerConnectionException(UnsupportedEncodingException e) {
		super(e);
	}

	public ServerConnectionException(IOException e) {
		super(e);
	}

	public ServerConnectionException(String string) {
		super(string);
	}
	
}

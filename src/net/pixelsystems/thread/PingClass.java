package net.pixelsystems.thread;

import java.io.IOException;
import java.util.TimerTask;

import net.pixelsystems.StateUtil;
import net.pixelsystems.client.Client;
import net.pixelsystems.client.Client.State;

public class PingClass extends TimerTask{
	private StateUtil.CLIENT_STATE clientState;
	private ThreadFeedback feedback;
	private Client client=null;
	
	public PingClass(ThreadFeedback feedback,Client client){
		this.clientState=StateUtil.CLIENT_STATE.MISSING;
		this.feedback = feedback;
		this.client = client;
	}

	public StateUtil.CLIENT_STATE getState(){
		return clientState;
	}
	public void run(){
		if(StateUtil.isClientChecking(clientState)){
			return;
		}
		clientState = StateUtil.CLIENT_STATE.CHECKING;
		feedback.feedbackEvent(new ClientFeedbackEvent("Check for connection..."));

		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");		
	    ProcessBuilder processBuilder = new ProcessBuilder("ping", isWindows? "-n" : "-c", "1","-w","5", client.getIP());
	    Process proc;
		try {
			proc = processBuilder.start();
			int returnVal = proc.waitFor();
			if(returnVal==0){				
				clientState = StateUtil.CLIENT_STATE.EXISTING;
				feedback.feedbackEvent(new ClientFeedbackEvent(client.getName()+" verified!",this));
				client.setState(State.AVAILABLE);
				
			}else{				
				clientState = StateUtil.CLIENT_STATE.MISSING;
				feedback.feedbackEvent(new ClientFeedbackEvent(client.getName()+" not available",this));
				client.setState(State.MISSING);
			}
			
		} catch (IOException | InterruptedException e) {			
			e.printStackTrace();
			feedback.feedbackEvent(new AppFeedbackEvent("{ERROR}:"+e.getMessage()));
		}
	}
}
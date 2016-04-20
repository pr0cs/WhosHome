package net.pixelsystems.thread;

import java.io.IOException;
import java.util.TimerTask;

import net.pixelsystems.StateUtil;
import net.pixelsystems.WhosHomeThreadHandler;

public class PingClass extends TimerTask{
	private StateUtil.CLIENT_STATE clientState;
	private ThreadFeedback feedback;
	private String ip=null;
	
	public PingClass(ThreadFeedback feedback,String ip){
		this.clientState=StateUtil.CLIENT_STATE.MISSING;
		this.feedback = feedback;
		this.ip = ip;
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
	    ProcessBuilder processBuilder = new ProcessBuilder("ping", isWindows? "-n" : "-c", "1","-w","5", ip);
	    Process proc;
		try {
			proc = processBuilder.start();
			int returnVal = proc.waitFor();
			if(returnVal==0){				
				clientState = StateUtil.CLIENT_STATE.EXISTING;
				feedback.feedbackEvent(new ClientFeedbackEvent(ip+" verified!",this));
				
			}else{				
				clientState = StateUtil.CLIENT_STATE.MISSING;
				feedback.feedbackEvent(new ClientFeedbackEvent(ip+" not available",this));
			}
			
		} catch (IOException | InterruptedException e) {			
			e.printStackTrace();
			feedback.feedbackEvent(new AppFeedbackEvent("{ERROR}:"+e.getMessage()));
		}
	}
}
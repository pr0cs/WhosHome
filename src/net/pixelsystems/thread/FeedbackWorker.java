package net.pixelsystems.thread;

import javax.swing.SwingWorker;


import net.pixelsystems.WhosHomeThreadHandler;

public class FeedbackWorker extends SwingWorker<Boolean, Void> {
	private FeedbackEvent event;
	private WhosHomeThreadHandler handler;
	 
	public FeedbackWorker(FeedbackEvent evt,WhosHomeThreadHandler handler){
		this.event = evt;
		this.handler = handler;
	}
	@Override
	protected Boolean doInBackground() throws Exception {
		switch(event.getType()){
			case SERVER:
				handleServerFeedback((ServerFeedbackEvent)event);
				break;
			case CLIENT:
				handleClientFeedback((ClientFeedbackEvent)event);
				break;
			case APP:
				handleAppFeedback((AppFeedbackEvent)event);
				break;
		}
		return true;
	}

	private void handleAppFeedback(AppFeedbackEvent event2) {
		handler.setAppStatus(event2.getFeedback());
		
	}
	private void handleClientFeedback(ClientFeedbackEvent event2) {
		// might need to be handled for each PingClass within
		handler.setClientFeedback(event2.getClient(),event2.getFeedback());
		
	}
	private void handleServerFeedback(ServerFeedbackEvent evt){
		switch(evt.getServerEventType()){
			case LOGIN_OK:
				handler.loginOK(evt);
				break;
			case LOGIN_FAILED:
				handler.setServerStatus(evt.getFeedback());
				break;
			case CAMERA_INFO:
				handler.setCameraInfo(evt.getRetrievedCams());
				break;
			case ERROR:
				handler.setServerStatus(evt.getFeedback());
				break;
			
		}
		
	}
}

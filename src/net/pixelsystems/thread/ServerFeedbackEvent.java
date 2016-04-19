package net.pixelsystems.thread;

import net.pixelsystems.thread.ThreadFeedback.FeedbackType;

public class ServerFeedbackEvent extends FeedbackEvent {
	public enum ServerEventType{LOGIN_FAILED,LOGIN_OK,ERROR};
	private String event;
	private ServerEventType type;
	public ServerFeedbackEvent(String serverEventTxt,ServerEventType type ) {
		super(FeedbackType.SERVER);
		event = serverEventTxt;
		this.type = type;
	}
	@Override
	public String getFeedback() {
		return event;
	}
	
	@Override
	public String toString(){
		return event;
	}
	public ServerEventType getServerEventType(){
		return type;
	}
}

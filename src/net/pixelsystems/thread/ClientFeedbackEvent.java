package net.pixelsystems.thread;

public class ClientFeedbackEvent extends FeedbackEvent {
	private String event;
	private PingClass client=null;
	public ClientFeedbackEvent(String eventTxt){
		super(ThreadFeedback.FeedbackType.CLIENT);
		event = eventTxt;
	}
	public ClientFeedbackEvent(String eventTxt,PingClass client){
		this(eventTxt);
		this.client = client;
	}
	
	public PingClass getClient(){
		return client;
	}
	@Override
	public String getFeedback() {
		return event;
	}
	
	@Override
	public String toString(){
		return event;
	}
}

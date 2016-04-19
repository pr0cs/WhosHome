package net.pixelsystems.thread;

import net.pixelsystems.thread.ThreadFeedback.FeedbackType;

public class AppFeedbackEvent extends FeedbackEvent {
	private String event;
	public AppFeedbackEvent(String eventText){
		super(FeedbackType.APP);
		this.event = eventText;
	}
	@Override
	public String getFeedback() {
		return event;
	}

}

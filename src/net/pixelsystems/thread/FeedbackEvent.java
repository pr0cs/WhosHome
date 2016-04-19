package net.pixelsystems.thread;

import net.pixelsystems.thread.ThreadFeedback.FeedbackType;

public abstract class FeedbackEvent {
	
	private FeedbackType type;
	public FeedbackEvent(FeedbackType type){
		this.type = type;
	}
	public FeedbackType getType(){
		return type;
	}
	public abstract String getFeedback();
}

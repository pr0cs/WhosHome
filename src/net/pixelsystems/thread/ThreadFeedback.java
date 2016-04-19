package net.pixelsystems.thread;

public interface ThreadFeedback {
	public enum FeedbackType{CLIENT,SERVER,APP};
	public void feedbackEvent(FeedbackEvent event);
}

package net.pixelsystems.thread;

import java.util.List;

import net.pixelsystems.server.CameraData;
import net.pixelsystems.thread.ThreadFeedback.FeedbackType;

public class ServerFeedbackEvent extends FeedbackEvent {
	public enum ServerEventType{LOGIN_FAILED,LOGIN_OK,ERROR,CAMERA_INFO};
	private String event;
	private ServerEventType type;
	private List<CameraData> retrievedCams= null;
	public ServerFeedbackEvent(String serverEventTxt,ServerEventType type ) {
		super(FeedbackType.SERVER);
		event = serverEventTxt;
		this.type = type;
	}
	public ServerFeedbackEvent(List<CameraData> retrievedCams){
		super(FeedbackType.SERVER);
		this.type = ServerEventType.CAMERA_INFO;
		this.retrievedCams = retrievedCams;
	}
	
	public List<CameraData>getRetrievedCams(){
		return retrievedCams;
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

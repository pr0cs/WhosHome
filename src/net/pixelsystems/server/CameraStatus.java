package net.pixelsystems.server;

public class CameraStatus {
	Integer CurrentSchedulerState;  
	String ErrorMessage;
	Integer FPS;
	Boolean IsAudioDetector;
	Boolean IsMotionDetector;
	Boolean IsRecording;
	Boolean IsStreaming;
	Boolean IsTimeLapse;
	String LastAlert;
    String LastErrorReceived;
    String LastImageReceived;
	String NextSchedulerChange;
	 Integer NextSchedulerState;	
	Boolean SupportPTZ;	
	
	public CameraStatus(){
		
	}
}

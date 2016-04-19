package net.pixelsystems.server;


public class CameraData {
	public class CameraStatus{
		public CameraStatus(){
			
		}
		Integer CurrentSchedulerState;
		String ErrorMessage;
		Integer FPS;
		Boolean HasAudio;
		Boolean HasPTZ;
		Boolean IsAudioDetector;
		Boolean IsLPR;
		Boolean IsMotionDetector;
		Boolean IsMotionAlerting;
		Boolean IsRecording;
		Boolean IsStreaming;
		Boolean IsTimeLapse;
		String LastAlert;
	    String LastErrorReceived;
	    String LastImageReceived;
	    String NextSchedulerChange;
	    Integer NextSchedulerState;
	}
	
	Integer Id;
	CameraStatus Status;
	String SourceUrl;
	Integer Width;
	Integer Height;
	Boolean Enabled;
	String SourceName;
	String SourceUID;
	
	public Boolean isEnabled(){
		return Enabled;
	}
	public Boolean isMotionDetector(){
		return Status.IsMotionDetector;
	}
	public String getName(){
		return SourceName;
	}
	public int getID(){
		return Id;
	}
	public CameraData() {
	}
	public CameraData(String name, Boolean enabled, Boolean motionDetector){
		Status = new CameraStatus();
		SourceName = name;
		Enabled = enabled;
		Status.IsMotionDetector = motionDetector;
	}
}
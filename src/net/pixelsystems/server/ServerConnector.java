package net.pixelsystems.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.pixelsystems.thread.ServerFeedbackEvent;
import net.pixelsystems.thread.ServerFeedbackEvent.ServerEventType;
import net.pixelsystems.thread.ThreadFeedback;
@SuppressWarnings("unused")
public class ServerConnector{

	//////////////   NETCAM CONNECTOR CLASSES
	private class UserRoles{
		String Name;
		String Description;
	}
	private class ServerLogin{
		String PermToken;
		String SessionToken;
		String Username;
		String Email;
		String Icon;
		Boolean ReadOnly;
		String LastConnection;
		String CreationDate;
		List<UserRoles> Roles;
		String FailedLoginMessage;
		Boolean PushEnabled;
	}
	//////////////   INTERNAL CLASSES
	private class ServerFeedback{
		
	}
	private final String serverIP;
	private final String port;
	private ServerLogin login;
	private String baseString;
	private ServerLogin connectedServer=null;
	private List<CameraData>cachedCams = null;
	private ThreadFeedback feedback; 
	
	/**
	 * 
	 * @param serverIP
	 * @param port
	 * @param feedback
	 */
	public ServerConnector(String serverIP,String port,ThreadFeedback feedback){
		this.serverIP = serverIP;
		this.port = port;
		baseString="http://"+serverIP+":"+port+"/Json/";
		this.feedback = feedback;
	}
	// helper methods
	private static ServerFeedbackEvent loginError(String error){
		return new ServerFeedbackEvent(error,ServerFeedbackEvent.ServerEventType.LOGIN_FAILED);
	}
	private static ServerFeedbackEvent loginOK(String message){
		return new ServerFeedbackEvent(message,ServerFeedbackEvent.ServerEventType.LOGIN_OK);
	}

	/**
	 * Spawns a login thread, sends feedback depending on result
	 * @param username
	 * @param password
	 */
	public void login(String username,String password){
		Thread serverThread = new Thread(){
			@Override
			public void run(){
				try{
					URL hostURL = new URL(baseString+"Login?username="+username+"&password="+password);
		
					//Type listType = new TypeToken<List<String>>(){}.getType();
					JsonReader reader = new JsonReader(new InputStreamReader(hostURL.openStream(),"UTF-8"));
					Gson login = new Gson();
					ServerLogin result = (ServerLogin)login.fromJson(reader, ServerLogin.class);		
					reader.close();
					if(result.FailedLoginMessage.length()>5){
						feedback.feedbackEvent(loginError(result.FailedLoginMessage));
					}else{
						connectedServer = result;
						feedback.feedbackEvent(loginOK("Login succeeded..."));
					}
				}catch(IOException mue){
					feedback.feedbackEvent(loginError(mue.getMessage()));
				} 
			}
		};
		serverThread.start();
	}
	
	public boolean connectionEstablished(){
		if(connectedServer!=null){
			return (connectedServer.SessionToken.length()<5);
		}
		return false;
	}
	/**
	 * Spawns a verify thread, sends feedback depending on result
	 * TODO:  needs to be called regularly, perhaps per-ping, to ensure connection to server stays valid
	 */
	public Boolean verify(){
		if(!connectionEstablished()){
			feedback.feedbackEvent(loginError("Connection to the server has not yet been created..."));
		}
		if(connectedServer.SessionToken.length()<5){
			feedback.feedbackEvent(loginError("Connection to the server is using an invalid token..."));
		}
		Boolean verify = false;
		try{
			URL hostURL = new URL(baseString+"VerifyToken?token="+connectedServer.SessionToken);
			JsonReader reader = new JsonReader(new InputStreamReader(hostURL.openStream(),"UTF-8"));
			Gson login = new Gson();
			verify = (Boolean)login.fromJson(reader, Boolean.class);
			reader.close();
			return verify;
		} catch (IOException e) {
			feedback.feedbackEvent(loginError("Could not verify connection..."));
		}
		return false;
	}
	
	public void getCameras(){
		Thread serverThread = new Thread(){
			@SuppressWarnings("unchecked")
			@Override
			public void run(){
				try{
					if(!verify()){
						return;
					}
					URL hostURL = new URL(baseString+"GetCameras?authToken="+connectedServer.SessionToken);
					JsonReader reader = new JsonReader(new InputStreamReader(hostURL.openStream(),"UTF-8"));
					Gson login = new Gson();
					Type listType = new TypeToken<List<CameraData>>(){}.getType();
					Object camObj = login.fromJson(reader, listType);
					List<CameraData>cams = (List<CameraData>) camObj;

					reader.close();
					//for(CameraData cam:cams){
					//				System.out.println("FOUND:"+cam.SourceName+" is enabled:"+cam.isEnabled()+" is motion detector:"+cam.isMotionDetector());
					//		}
					cachedCams = cams;
					feedback.feedbackEvent(new ServerFeedbackEvent(cams));
				} catch (IOException e) {
					feedback.feedbackEvent(new ServerFeedbackEvent(e.getMessage(), ServerEventType.ERROR));
				}
			}
		};
		serverThread.start();
	}
	public Boolean monitor(List<String> enabledCams) {
		Boolean result=false;
		try{
			for(int cc=0;cc<cachedCams.size();cc++){
				String cachedCamName = cachedCams.get(cc).getName();
				if(enabledCams.contains(cachedCamName)){
					int c=enabledCams.indexOf(cachedCamName);
					if(cachedCams.get(cc).getName().equals(enabledCams.get(c))){
						if(!isCameraMotionDetector(cc)){
						//System.out.println("Do we need to turn "+cachedCamName+" *ON* for motion detection: "+!camStatus.IsMotionDetector);
							Boolean motionOn = enableMotionDetection(cc,true);
							result = result & motionOn;
							if(motionOn){
								feedback.feedbackEvent(new ServerFeedbackEvent("Enabled "+cachedCamName+" for motion detection", ServerEventType.MONITOR)); 
							}
						}
						
					}
				}else{
					if(isCameraMotionDetector(cc)){
						// cached cam is not enabled, stop monitoring
						Boolean motionOn = enableMotionDetection(cc,false);
						result = result & motionOn;
						if(motionOn){
							feedback.feedbackEvent(new ServerFeedbackEvent("Disabled "+cachedCamName+" for motion detection", ServerEventType.MONITOR)); 
						}
					}
				}
			}
		}catch(IOException ioex){
			feedback.feedbackEvent(new ServerFeedbackEvent(ioex.getMessage(), ServerEventType.ERROR));
		}
		return result;
	}
	
	private boolean isCameraMotionDetector(int sourceIdx)throws IOException{
		URL hostURL = new URL(baseString+"GetCamStatus?authToken="+connectedServer.SessionToken+"&sourceIdx="+sourceIdx);						
		JsonReader reader = new JsonReader(new InputStreamReader(hostURL.openStream(),"UTF-8"));
		Gson getCamStatus = new Gson();
		CameraStatus camStatus = getCamStatus.fromJson(reader, CameraStatus.class);
		reader.close();
		return camStatus.IsMotionDetector;
	}
	
	private boolean enableMotionDetection(int sourceId,boolean enabled)throws IOException{
		URL hostURL = new URL(baseString+"StartStopMotionDetector?authToken="+connectedServer.SessionToken+"&sourceId="+sourceId+"&enabled="+Boolean.toString(enabled));						
		JsonReader reader = new JsonReader(new InputStreamReader(hostURL.openStream(),"UTF-8"));
		Gson StartStopMotionDetector = new Gson();
		Boolean motionOn = StartStopMotionDetector.fromJson(reader, Boolean.class);
		reader.close();
		return motionOn;
	}
}

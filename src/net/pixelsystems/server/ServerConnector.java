package net.pixelsystems.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.pixelsystems.thread.FeedbackEvent;
import net.pixelsystems.thread.ServerFeedbackEvent;
import net.pixelsystems.thread.ServerFeedbackEvent.ServerEventType;
import net.pixelsystems.thread.ThreadFeedback;

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
	//////////////   INTERNAL MEMBERS
	private enum ServerState{VERIFY,LOGIN,GETCAM};
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
}

package net.pixelsystems.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.pixelsystems.thread.ServerFeedbackEvent;
import net.pixelsystems.thread.ServerFeedbackEvent.ServerEventType;
import net.pixelsystems.thread.ThreadFeedback;

public class ServerConnector {

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
	private final String serverIP;
	private final String port;
	private ServerLogin login;
	private String baseString;
	private ServerLogin connectedServer=null;
	private List<CameraData>cachedCams = null;
	private ThreadFeedback feedback;
	public ServerConnector(String serverIP,String port,ThreadFeedback feedback){
		this.serverIP = serverIP;
		this.port = port;
		baseString="http://"+serverIP+":"+port+"/Json/";
		this.feedback = feedback;
	}
	
	public static ServerFeedbackEvent loginError(String error){
		return new ServerFeedbackEvent(error,ServerFeedbackEvent.ServerEventType.LOGIN_FAILED);
	}
	public static ServerFeedbackEvent loginOK(String message){
		return new ServerFeedbackEvent(message,ServerFeedbackEvent.ServerEventType.LOGIN_OK);
	}

	public void login(String username,String password)throws ServerConnectionException{
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
				//System.out.println("Auth Key:"+result.SessionToken);
			}
		}catch(MalformedURLException mue){
			throw new ServerConnectionException(mue);
		} catch (UnsupportedEncodingException e) {
			throw new ServerConnectionException(e);
		} catch (IOException e) {
			throw new ServerConnectionException(e);
		}
		if(verify()){
			feedback.feedbackEvent(new ServerFeedbackEvent("OK", ServerEventType.LOGIN_OK));
		}
	}
	
	public boolean verify()throws ServerConnectionException{
		if(connectedServer==null){
			return false;
		}
		if(connectedServer.SessionToken.length()<5){
			return false;
		}
		Boolean verify = false;
		try{
			URL hostURL = new URL(baseString+"VerifyToken?token="+connectedServer.SessionToken);
			JsonReader reader = new JsonReader(new InputStreamReader(hostURL.openStream(),"UTF-8"));
			Gson login = new Gson();
			verify = (Boolean)login.fromJson(reader, Boolean.class);
			reader.close();
		}catch(MalformedURLException mue){
			throw new ServerConnectionException(mue);
		} catch (UnsupportedEncodingException e) {
			throw new ServerConnectionException(e);
		} catch (IOException e) {
			throw new ServerConnectionException(e);
		}
		return verify;
	}
	
	public List<CameraData> getCameras()throws ServerConnectionException{
		if(!verify()){
			throw new ServerConnectionException("Server is not connected");
		}
		
		try{
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
			return cams;
		}catch(MalformedURLException mue){
			throw new ServerConnectionException(mue);
		} catch (UnsupportedEncodingException e) {
			throw new ServerConnectionException(e);
		} catch (IOException e) {
			throw new ServerConnectionException(e);
		}
	}
	
}

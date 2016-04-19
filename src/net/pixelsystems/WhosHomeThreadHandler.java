package net.pixelsystems;

import java.util.List;

import net.pixelsystems.server.CameraData;
import net.pixelsystems.thread.PingClass;
import net.pixelsystems.thread.ServerFeedbackEvent;

public interface WhosHomeThreadHandler {

	public boolean isAppDisabled();
	public void setAppStatus(String status);
	public void setServerStatus(String status);
	public void setClientStatus(String status);
	public boolean isAppShuttingDown();
	public void setCameraInfo(List<CameraData>data);
	public void loginOK(ServerFeedbackEvent evt);
	public void setClientFeedback(PingClass pc, String feedbackTxt);
}

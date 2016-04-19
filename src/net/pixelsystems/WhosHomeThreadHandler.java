package net.pixelsystems;

public interface WhosHomeThreadHandler {

	public boolean isAppDisabled();
	public void setAppStatus(String status);
	public void setServerStatus(String status);
	public void setClientStatus(String status);
	public boolean isAppShuttingDown();
}

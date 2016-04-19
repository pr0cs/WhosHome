package net.pixelsystems;

public class StateUtil {
	public enum APP_STATUS{DISABLED,ENABLED,SHUTDOWN};
	public enum SERVER_STATUS{DISCONNECTED,CONNECTED};
	public enum CLIENT_STATE{MISSING,CHECKING,EXISTING};
	
	public static boolean isClientChecking(CLIENT_STATE clientState){
		return (clientState==StateUtil.CLIENT_STATE.CHECKING);
	}
}

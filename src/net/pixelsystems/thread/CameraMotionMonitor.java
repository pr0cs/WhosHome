package net.pixelsystems.thread;

import java.util.List;
import java.util.TimerTask;

import net.pixelsystems.StateUtil;
import net.pixelsystems.WhosHomeThreadHandler;

public class CameraMotionMonitor extends TimerTask{
	private List<PingClass> hosts;
	private WhosHomeThreadHandler handler;
	public CameraMotionMonitor(List<PingClass> hosts,WhosHomeThreadHandler handler){
		this.hosts = hosts;
		this.handler = handler;
	}
	public void run(){
		/*
		if(host1.hostState==StateUtil.HOST_STATE.EXISTING && host2.hostState==StateUtil.HOST_STATE.EXISTING){
			// TURN OFF MONITORING
		}else if(host1.hostState == StateUtil.HOST_STATE.MISSING && host2.hostState==StateUtil.HOST_STATE.MISSING){
			// TURN ON MONITORING
		}
		*/
	}
}
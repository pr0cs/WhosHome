package net.pixelsystems.client;

public class Client {
	public enum State{AVAILABLE,UNKNOWN,MISSING};
	   
	private String name;
	private String ip;
	private State state;
	
	public Client(String name, String ip){
		state = State.UNKNOWN;
		this.name = name;
		this.ip = ip;
	}
	
	public String getName(){
		return name;
	}
	public String getIP(){
		return ip;
	}
	public State getState(){
		return state;
	}
	public void setName(String newName){
		name = newName;
	}
	public void setIP(String newIP){
		ip = newIP;
	}
	public void setState(State newState){
		state = newState;
	}
	
}

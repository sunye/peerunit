package fr.inria.ant;

import org.apache.tools.ant.ProjectComponent;



/**
  */
public class Include extends ProjectComponent {
    
	private String ip;
	private String ip_from;
	private String ip_to;
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getFrom() {
		return ip_from;
	}

	public void setFrom(String ip_from) {
		this.ip_from = ip_from;
	}

	public String getTo() {
		return ip_to;
	}

	public void setTo(String ip_to) {
		this.ip_to = ip_to;
	}
	
}

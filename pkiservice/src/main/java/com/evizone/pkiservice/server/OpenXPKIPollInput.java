package com.evizone.pkiservice.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="pollInput", propOrder={"cn", "password"})
public class OpenXPKIPollInput {
	
	private String cn;
	
	private String password;	
	
	@XmlElement(name="cn", required=true)
	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}	

	@XmlElement(name="password", required=true)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}

package com.evizone.pkiservice.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="registerInput", propOrder={"cn", "email"})
public class EJBCARegisterInput {
	
	private String cn;
	
	private String email;
	
	@XmlElement(name="cn", required=true)
	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	@XmlElement(name="email", required=true)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}

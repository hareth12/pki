package com.evizone.pkiservice.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="enrollAndGetCertInput", propOrder={"cn", "challenge"})
public class EJBCAEnrollAndGetCertInput {
	
	private String cn;
	
	private String challenge;

	@XmlElement(name="cn", required=true)
	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	@XmlElement(name="challenge", required=true)
	public String getChallenge() {
		return challenge;
	}

	public void setChallenge(String challenge) {
		this.challenge = challenge;
	}

}

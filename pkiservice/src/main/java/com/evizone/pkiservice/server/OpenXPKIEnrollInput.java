package com.evizone.pkiservice.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="enrollInput", propOrder={"cn"})
public class OpenXPKIEnrollInput {
	
	private String cn;
	
	//private String challenge;		
	
	@XmlElement(name="cn", required=true)
	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}	

}

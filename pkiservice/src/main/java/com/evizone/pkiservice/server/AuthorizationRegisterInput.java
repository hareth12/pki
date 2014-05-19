package com.evizone.pkiservice.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="user", propOrder={"cn", "email", "phoneNo"})
public class AuthorizationRegisterInput {
	
	private String cn;
	
	private String email;
	
	private Integer phoneNo;

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

	@XmlElement(name="phoneNo", required=true)
	public Integer getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(Integer phoneNo) {
		this.phoneNo = phoneNo;
	}
	
}

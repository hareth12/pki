/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evizone.pkiservice;

import com.beust.jcommander.Parameter;
import com.evizone.pkiservice.CLI.Mode;
import com.evizone.pkiservice.CLI.Provider;

/**
 *
 * @author asyd
 */
public class AppParameters {

	@Parameter(names = "--cn", description = "Subject CN to request", required = true)
    private String cn;

    @Parameter(names = "--challenge", description = "Challenge password (EJBCA entity password) (required when enrolling to EJBCA)")
    private String challenge;

    @Parameter(names = "--password", description = "One-time-password (required when poll/download from OpenXPKI)")
    private String password;
    
	@Parameter(names = "--pkcs12-file", description = "PKCS#12 output file")
    private String pkcs12File = "cert.p12";
    
    @Parameter(names = "--email", description = "User email address to send confirmation emails to (required when register)")
    private String email;

    @Parameter(names = "--phone", description = "User phone number to send PIN to (required when register)")
    private Integer phoneNo;

    @Parameter(names = "--mode", description = "Mode (register/fetch/poll), poll only available for OpenXPKI", required = true)
    private Mode mode;    

    @Parameter(names = "--provider", description = "Provider (ejbca/openxpki)", required = true)
    private Provider provider;    
    
	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getChallenge() {
		return challenge;
	}

	public void setChallenge(String challenge) {
		this.challenge = challenge;
	}

	public String getPkcs12File() {
		return pkcs12File;
	}

	public void setPkcs12File(String pkcs12File) {
		this.pkcs12File = pkcs12File;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(Integer phoneNo) {
		this.phoneNo = phoneNo;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

    public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}

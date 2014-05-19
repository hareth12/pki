package com.evizone.pkiservice;

import java.io.FileOutputStream;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class CLI {	
	
	private EJBCA ejbca;
	
	private Authorization authorization;
	
	public static enum Mode {REGISTER, ENROLL};
	
	public static class ModeConverter implements IStringConverter<Mode> {

		public Mode convert(String value) {
			if (value.toLowerCase().contains("register")) {
				return Mode.REGISTER;
			} else if (value.toLowerCase().contains("enroll")) {
				return Mode.ENROLL;
			}
			return null;
		}
		
	}	
	
	public CLI() {
		ejbca = new EJBCAService().getEJBCAPort();
		authorization = new AuthorizationService().getAuthorizationPort();
	}
	
	public void authorizationRegistration(String cn, String email, Integer phoneNo) {
		User input = new User();
		input.setCn(cn);
		input.setEmail(email);
		input.setPhoneNo(phoneNo);		
		String res = authorization.register(input);
		System.out.println(res);				
	}

	public void ejbcaEnrollAndGetCert(String cn, String password, String filename) {
		byte[] res = null;
		try {
			EnrollAndGetCertInput input = new EnrollAndGetCertInput();
			input.setCn(cn);
			input.setChallenge(password);
			res = ejbca.enrollAndGetCert(input, 
				null);
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(res);
			fos.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("PKCS12 saved: " + filename);
	}
	
	public static void main(String[] args) {
        CLI app = new CLI();
        AppParameters params = new AppParameters();
        JCommander jcmd = new JCommander(params);

        try {
            jcmd.parse(args);
            if (params.getMode() == Mode.REGISTER) {
            	if (params.getEmail() != null &&
            		params.getPhoneNo() != null) {
            		app.authorizationRegistration(
            			params.getCn(), 
            			params.getEmail(),
            			params.getPhoneNo());
            	} else {
            		jcmd.usage();
            	}
            } else if (params.getMode() == Mode.ENROLL) {
            	if (params.getChallenge() != null) {
            		app.ejbcaEnrollAndGetCert(
            			params.getCn(),
            			params.getChallenge(),
            			params.getPkcs12File());
            	} else {
            		jcmd.usage();
            	}
            } else {
            	jcmd.usage();
            }
        } catch (ParameterException e) {
            jcmd.usage();
        }
		
	}

}

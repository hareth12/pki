package com.evizone.pkiservice;

import java.io.FileOutputStream;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class CLI {	
	
	private EJBCA ejbca;
	
	private OpenXPKI openxpki;
	
	private Authorization authorization;
	
	public static enum Mode {REGISTER, FETCH, POLL};

	public static enum Provider {EJBCA, OPENXPKI};
	
	public static class ModeConverter implements IStringConverter<Mode> {

		public Mode convert(String value) {
			if (value.toLowerCase().contains("register")) {
				return Mode.REGISTER;
			} else if (value.toLowerCase().contains("fetch")) {
				return Mode.FETCH;
			} else if (value.toLowerCase().contains("poll")) {
				return Mode.POLL;
			}
			return null;
		}
		
	}	

	public static class ProviderConverter implements IStringConverter<Provider> {

		public Provider convert(String value) {
			if (value.toLowerCase().contains("ejbca")) {
				return Provider.EJBCA;
			} else if (value.toLowerCase().contains("openxpki")) {
				return Provider.OPENXPKI;
			}
			return null;
		}
		
	}	
	
	public CLI() {
		ejbca = new EJBCAService().getEJBCAPort();
		openxpki = new OpenXPKIService().getOpenXPKIPort();
		authorization = new AuthorizationService().getAuthorizationPort();
	}
	
	public void authorizationEjbcaRegistration(String cn, String email, Integer phoneNo) {
		User input = new User();
		input.setCn(cn);
		input.setEmail(email);
		input.setPhoneNo(phoneNo);		
		String res = authorization.ejbcaRegister(input);
		System.out.println(res);				
	}

	public void authorizationOpenxpkiRegistration(String cn, String email, Integer phoneNo) {
		User input = new User();
		input.setCn(cn);
		input.setEmail(email);
		input.setPhoneNo(phoneNo);		
		String res = authorization.openxpkiRegister(input);
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

	public void openxpkiEnroll(String cn) {
		String res = null;
		try {
			EnrollInput input = new EnrollInput();
			input.setCn(cn);
			res = openxpki.enroll(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Enrollment response: " + res);
	}

	public void openxpkiPoll(String cn, String password) {
		CsrStatus res = null;
		try {
			PollInput input = new PollInput();
			input.setCn(cn);
			input.setPassword(password);
			res = openxpki.poll(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Enrollment response: " + res);
	}

	public void openxpkiDownload(String cn, String password, String filename) {
		byte[] res = null;
		try {
			PollInput input = new PollInput();
			input.setCn(cn);
			input.setPassword(password);
			res = openxpki.download(input);
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
            		if (params.getProvider() == Provider.EJBCA) {
	            		app.authorizationEjbcaRegistration(
	            			params.getCn(), 
	            			params.getEmail(),
	            			params.getPhoneNo());
            		} else if (params.getProvider() == Provider.OPENXPKI) {
	            		app.authorizationOpenxpkiRegistration(
	            			params.getCn(), 
	            			params.getEmail(),
	            			params.getPhoneNo());            			
            		}
            	} else {
            		jcmd.usage();
            	}
            } else if (params.getMode() == Mode.POLL) {
        		if (params.getProvider() == Provider.OPENXPKI) {
	            	if (params.getPassword() != null) {
	            		app.openxpkiPoll(
	            			params.getCn(),
	            			params.getPassword());        			
	        		} else {
	        			jcmd.usage();
	        		}
        		} else {
        			jcmd.usage();
        		}
            } else if (params.getMode() == Mode.FETCH) {
        		if (params.getProvider() == Provider.EJBCA) {
	            	if (params.getChallenge() != null) {
	            		app.ejbcaEnrollAndGetCert(
	            			params.getCn(),
	            			params.getChallenge(),
	            			params.getPkcs12File());
	            	} else {
	            		jcmd.usage();
	            	}
        		} else if (params.getProvider() == Provider.OPENXPKI) {
        			if (params.getPassword() != null) {
	            		app.openxpkiDownload(
	            			params.getCn(),
	            			params.getPassword(),
	            			params.getPkcs12File());
        			} else {
        				jcmd.usage();
        			}
        		}
            } else {
            	jcmd.usage();
            }
        } catch (ParameterException e) {
            jcmd.usage();
        }
		
	}

}

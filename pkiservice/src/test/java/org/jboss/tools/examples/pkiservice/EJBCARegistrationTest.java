package org.jboss.tools.examples.pkiservice;

import java.util.HashMap;

import net.samcik.java.io.HttpConnection;
import net.samcik.java.utils.Parser;
import net.samcik.java.utils.UserAgent;
import net.samcik.java.utils.UserAgent.Response;

public class EJBCARegistrationTest {
	
	
	private static String REGISTRATION_URL = "http://vps59351.ovh.net:8080/ejbca/enrol/reg_submit.jsp";
	private static String RESPONSE_REGEXP = "<h1 class=\"title\">Request Registration</h1>\\s+<p>(.*?)</p>";	
	
	public static void main(String[] args) {
		String cn = "cn";
		String email = "email";
		UserAgent ua = new UserAgent(new HttpConnection());
    	try {
        	HashMap<String, String> headers = new HashMap<String, String>();
        	headers.put("hidemenu", "false");
        	headers.put("certType", "1");
        	headers.put("dnfield_0", cn);
        	headers.put("email", email);
        	headers.put("code", email.charAt(email.length() - 1) + "");
			Response res = ua.post(headers, REGISTRATION_URL);
			boolean ok = false;
			System.out.println(res.code);
			if (res.code == HttpConnection.OK) {
				String msg = Parser.extract(1, RESPONSE_REGEXP, res.html);
				System.out.println(msg);
				if (msg.contains("The registration request has been successfully submitted ")) {
					ok = true;
				} 
			} else {
				//ERROR
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

}

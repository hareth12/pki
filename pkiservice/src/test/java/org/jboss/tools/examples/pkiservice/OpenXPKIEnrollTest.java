package org.jboss.tools.examples.pkiservice;

import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.client.CertificateVerificationCallback;
import org.jscep.client.Client;
import org.jscep.client.EnrollmentResponse;

import com.evizone.pkiservice.server.OpenXPKIEnrollInput;
import com.evizone.pkiservice.util.CertUtil;
import com.evizone.pkiservice.util.KeyManager;
import com.evizone.pkiservice.util.SqlUtil;

public class OpenXPKIEnrollTest {
	
	private String openxpkiScepUrl = "http://vps59351.ovh.net/scep/pkiclient.exe";

	private String caIdentifier = null;
	
	private Integer keySize = 2048;	
	
	private Connection conn;		
	
	private SqlUtil sqlUtil = new SqlUtil();
	
	public OpenXPKIEnrollTest() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(
			"jdbc:mysql://localhost/idp",
			"root",
			"as0rtsao");		
	}
	
	public String enroll(OpenXPKIEnrollInput input) {
		/*
		 * DN for simplicity only includes CN
		 */
		String dn = "CN=" + input.getCn();
		String challenge = "challenge";
    	System.out.println("cn=" + input.getCn());
    	System.out.println("dn=" + dn);
    	
    	try {
	        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	
	        KeyManager km = new KeyManager();
	        CertUtil certutil = new CertUtil();
            Client client = new Client(new URL(openxpkiScepUrl), new ConsoleCallbackHandler());
	
	        System.out.println("Generating RSA key...");
	        KeyPair kp = km.createRSA(keySize);	
	        X509Certificate cert = certutil.createSelfSignedCertificate(kp, dn);
	
	        System.out.println("Generating PKCS#10 signing request...");
	        PKCS10CertificationRequest request = certutil.createCertificationRequest(
	        	kp,
				dn,
				challenge);	

	        System.out.println("Getting CA certificates...");
            CertStore caCertificateStore = client.getCaCertificate(caIdentifier);
            Collection <? extends Certificate> caCertificates = caCertificateStore.getCertificates(null);
            System.out.println("Received " + caCertificates.size() + " CA certificate(s).");
            
            System.out.println("Starting enrollment request...");
            EnrollmentResponse response = client.enrol(cert,
				kp.getPrivate(),
				request,
				caIdentifier);
            
            System.out.println("CSR sent (transactionId: " + response.getTransactionId() + ")");
            
            sqlUtil.updatePrincipal(
            	conn,
            	input.getCn(),
            	response.getTransactionId(),
            	cert,
            	kp.getPrivate());            
        } catch (IOException e) {
        	e.printStackTrace();
            System.err.println(e.getMessage());
            if (e.getMessage().contains("400")) {
                System.err.println(". Probably a template issue, look at PKI log");
            } else if (e.getMessage().contains("404")) {
                System.err.println(". Invalid URL or CA identifier");
            } else if (e.getMessage().contains("401")) {
                System.err.println(". Probably EJBCA invalid entity status");
            }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
		return "OK";		
	}
	
    private static class ConsoleCallbackHandler implements CallbackHandler {

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof CertificateVerificationCallback) {
                    CertificateVerificationCallback callback = (CertificateVerificationCallback) callbacks[i];
                    callback.setVerified(true);
                } else {
                    throw new UnsupportedCallbackException(callbacks[i]);
                }
            }
        }
        
    }			

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		OpenXPKIEnrollInput input = new OpenXPKIEnrollInput();
		input.setCn("test102");
		OpenXPKIEnrollTest test = new OpenXPKIEnrollTest();
		String msg = test.enroll(input);
		System.out.println(msg);
	}

}

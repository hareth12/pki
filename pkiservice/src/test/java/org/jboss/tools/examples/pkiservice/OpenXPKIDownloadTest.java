package org.jboss.tools.examples.pkiservice;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;
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

import org.jscep.client.CertificateVerificationCallback;
import org.jscep.client.Client;
import org.jscep.client.EnrollmentResponse;
import org.jscep.transaction.TransactionId;

import com.evizone.pkiservice.server.OpenXPKIPollInput;
import com.evizone.pkiservice.util.CertUtil;
import com.evizone.pkiservice.util.SqlUtil;

public class OpenXPKIDownloadTest {
	
	private String openxpkiScepUrl = "http://vps59351.ovh.net/scep/pkiclient.exe";

	private String caIdentifier = null;
	
	private Connection conn;		
	
	private SqlUtil sqlUtil = new SqlUtil();
	
	public OpenXPKIDownloadTest() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(
			"jdbc:mysql://localhost/idp",
			"root",
			"as0rtsao");		
	}
	
	public byte[] download(OpenXPKIPollInput input) {
    	System.out.println("cn=" + input.getCn());
    	System.out.println("password=" + input.getPassword());
    	
    	/*
    	 * TODO: Add checking password
    	 */  
    	byte[] out = null;
    	try {
	        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	
	        CertUtil certutil = new CertUtil();	        
            Client client = new Client(new URL(openxpkiScepUrl), new ConsoleCallbackHandler());
            
            TransactionId tid = sqlUtil.getTransactionId(
            	conn, 
            	input.getCn());            
            X509Certificate cert = sqlUtil.getCert(
            	conn, 
            	input.getCn());            
            PrivateKey pkey = sqlUtil.getPrivateKey(
            	conn, 
            	input.getCn());

            System.err.println("Polling request...");
            EnrollmentResponse response = client.poll(cert, 
					pkey,
	                cert.getSubjectX500Principal(),
	                tid,
	                caIdentifier);
            
            CertStore store = response.getCertStore();
            Collection<? extends Certificate> certs = store.getCertificates(null);

            System.out.println("Received response containing " + certs.size() + " certificate(s).");
            X509Certificate certificate = (X509Certificate)certs.toArray()[0];
            out = certutil.savePKCS12ToByteArray(input.getCn(), certificate, pkey, 
            	sqlUtil.getPassword(
            		conn, input.getCn()));
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
    	
    	return out;
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

	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
		OpenXPKIPollInput input = new OpenXPKIPollInput();
		input.setCn("test102");
		input.setPassword("test");
		OpenXPKIDownloadTest test = new OpenXPKIDownloadTest();
		byte[] bytes = test.download(input);
		FileOutputStream fos = new FileOutputStream(input.getCn() + ".p12");
		fos.write(bytes);
		fos.close();
	}

}

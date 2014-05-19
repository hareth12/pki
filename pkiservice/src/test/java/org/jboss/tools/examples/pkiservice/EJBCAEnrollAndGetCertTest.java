package org.jboss.tools.examples.pkiservice;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import net.samcik.java.io.HttpConnection;
import net.samcik.java.utils.UserAgent;

import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.client.CertificateVerificationCallback;
import org.jscep.client.Client;
import org.jscep.client.EnrollmentResponse;

import com.evizone.pkiservice.util.CertUtil;
import com.evizone.pkiservice.util.KeyManager;

public class EJBCAEnrollAndGetCertTest {

	private static int KEY_SIZE = 2048;
	private static String CA_IDENTIFIER = "ManagementCA";
	
	public static void main(String[] args) {
		String cn = "test10";
		String challenge = "mXUIMIAB";
		String url = "http://vps59351.ovh.net:8080/ejbca/publicweb/apply/scep/pkiclient.exe";
		
		String dn = "CN=" + cn;
		String filename = cn + ".pem";
    	System.out.println("cn=" + cn);
    	System.out.println("dn=" + dn);
    	System.out.println("challenge=" + challenge);
    	System.out.println("url=" + url);
    	
    	byte[] out = null;
    	try {    	
	        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	
	        KeyManager km = new KeyManager();
	        CertUtil certutil = new CertUtil();
            Client client = new Client(new URL(url), new ConsoleCallbackHandler());
	
	        System.out.println("Generating RSA key...");
	        KeyPair kp = km.createRSA(KEY_SIZE);	
	        X509Certificate cert = certutil.createSelfSignedCertificate(kp, dn);
	
	        System.out.println("Generating PKCS#10 signing request...");
	        PKCS10CertificationRequest request = certutil.createCertificationRequest(
	        	kp,
				dn,
				challenge);	

	        System.out.println("Getting CA certificates...");
            CertStore caCertificateStore = client.getCaCertificate(CA_IDENTIFIER);
            Collection <? extends Certificate> caCertificates = caCertificateStore.getCertificates(null);
            System.out.println("Received " + caCertificates.size() + " CA certificate(s).");
            
            System.out.println("Starting enrollment request...");
            EnrollmentResponse response = client.enrol(cert,
				kp.getPrivate(),
				request,
				CA_IDENTIFIER);            
            
            if (response.isSuccess()) {
                System.out.println("Enrollment request successful!");
                CertStore store = response.getCertStore();
                Collection<? extends Certificate> certs = store.getCertificates(null);

                System.out.println("Received response containing " + certs.size() + " certificate(s).");
                X509Certificate certificate = (X509Certificate)certs.toArray()[0];
                saveToPEM(filename, certificate);
                out = savePEMToByteArray(certificate);
            }
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
	}
	
    public static void saveToPEM(String filename, Object data) {
        try {
            System.out.println("Saving: " + filename);
            PEMWriter writer = new PEMWriter(new FileWriter(new File(filename), true));
            writer.writeObject(data);
            writer.close();
        } catch (IOException e) {
        	e.printStackTrace();
            System.err.println("Could not save file: " + filename);
            System.err.println(e.getMessage());
        }
    }	
	
    public static byte[] savePEMToByteArray(Object data) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PEMWriter writer = new PEMWriter(
            	new BufferedWriter(
                	new OutputStreamWriter(baos)));
            writer.writeObject(data);
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return baos.toByteArray();
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

}

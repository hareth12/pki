package com.evizone.pkiservice.server;

import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.client.CertificateVerificationCallback;
import org.jscep.client.Client;
import org.jscep.client.EnrollmentResponse;
import org.jscep.transaction.TransactionId;

import com.evizone.pkiservice.util.CertUtil;
import com.evizone.pkiservice.util.KeyManager;
import com.evizone.pkiservice.util.SqlUtil;

@WebService(endpointInterface = "com.evizone.pkiservice.server.OpenXPKI",
            targetNamespace = "http://pkiservice.evizone.com/",
            name = "OpenXPKI",
            serviceName = "OpenXPKIService",
            portName = "OpenXPKIPort")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
public class OpenXPKIImpl implements OpenXPKI {
	
	@Resource(name = "openxpkiScepUrl")
	private String openxpkiScepUrl;

	@Resource(name = "keySize")
	private Integer keySize;
	
	@Resource(name = "caIdentifier")
	private String caIdentifier;
	
	@Resource(name = "datasourceContext")
	private static String datasourceContext;		
	
	private SqlUtil sqlUtil = new SqlUtil();
	
	@Override
	public String enroll(@WebParam(name = "input") OpenXPKIEnrollInput input) {
		/*
		 * DN for simplicity only includes CN
		 */
		String dn = "CN=" + input.getCn();
		String challenge = "challenge";
    	System.out.println("cn=" + input.getCn());
    	System.out.println("dn=" + dn);
    	//System.out.println("challenge=" + input.getChallenge());
    	
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
            
            System.err.println("Starting enrollment request...");
            EnrollmentResponse response = client.enrol(cert,
				kp.getPrivate(),
				request,
				caIdentifier);
            
            System.err.println("CSR sent (transactionId: " + response.getTransactionId() + ")");
            
            sqlUtil.updatePrincipal(
            	sqlUtil.getJNDIConnection(datasourceContext),
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

	@Override
	public CSRStatus poll(@WebParam(name = "input") OpenXPKIPollInput input) {
    	System.out.println("cn=" + input.getCn());
    	System.out.println("password=" + input.getPassword());
    	
    	/*
    	 * TODO: Add checking password
    	 */    	
    	try {
	        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	
            Client client = new Client(new URL(openxpkiScepUrl), new ConsoleCallbackHandler());
            
            TransactionId tid = sqlUtil.getTransactionId(
            	sqlUtil.getJNDIConnection(datasourceContext), 
            	input.getCn());            
            X509Certificate cert = sqlUtil.getCert(
            	sqlUtil.getJNDIConnection(datasourceContext), 
            	input.getCn());            
            PrivateKey pkey = sqlUtil.getPrivateKey(
            	sqlUtil.getJNDIConnection(datasourceContext), 
            	input.getCn());

            System.err.println("Polling request...");
            EnrollmentResponse response = client.poll(cert, 
					pkey,
	                cert.getSubjectX500Principal(),
	                tid,
	                caIdentifier);
            
            if (response.isFailure()) {
            	return CSRStatus.FAILURE; 
            } else if (response.isPending()) {
            	return CSRStatus.PENDING;
            } else if (response.isSuccess()) {
            	return CSRStatus.SUCCESS;
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
    	
    	return CSRStatus.SUCCESS;
	}

	@Override
	public byte[] download(@WebParam(name = "input") OpenXPKIPollInput input) {
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
            	sqlUtil.getJNDIConnection(datasourceContext), 
            	input.getCn());            
            X509Certificate cert = sqlUtil.getCert(
            	sqlUtil.getJNDIConnection(datasourceContext), 
            	input.getCn());            
            PrivateKey pkey = sqlUtil.getPrivateKey(
            	sqlUtil.getJNDIConnection(datasourceContext), 
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
            		sqlUtil.getJNDIConnection(datasourceContext), input.getCn()));
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

}


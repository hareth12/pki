package com.evizone.pkiservice.server;

import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.ws.Holder;

import net.samcik.java.io.HttpConnection;
import net.samcik.java.utils.Parser;
import net.samcik.java.utils.UserAgent;
import net.samcik.java.utils.UserAgent.Response;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.client.CertificateVerificationCallback;
import org.jscep.client.Client;
import org.jscep.client.EnrollmentResponse;

import com.evizone.pkiservice.util.CertUtil;
import com.evizone.pkiservice.util.KeyManager;
import com.evizone.pkiservice.util.SqlUtil;

@WebService(endpointInterface = "com.evizone.pkiservice.server.EJBCA",
            targetNamespace = "http://pkiservice.evizone.com/",
            name = "EJBCA",
            serviceName = "EJBCAService",
            portName = "EJBCAPort")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
public class EJBCAImpl implements EJBCA {
	
	private UserAgent ua = new UserAgent(new HttpConnection());
	
	@Resource(name = "ejbcaRegistrationUrl")
	private String ejbcaRegistrationUrl;
	
	@Resource(name = "ejbcaScepUrl")
	private String ejbcaScepUrl;

	@Resource(name = "keySize")
	private Integer keySize;
	
	@Resource(name = "caIdentifier")
	private String caIdentifier;
	
	@Resource(name = "datasourceContext")
	private static String datasourceContext;		
	
	private SqlUtil sqlUtil = new SqlUtil();
	
	private final String RESPONSE_REGEXP = "<h1 class=\"title\">Request Registration</h1>\\s+<p>(.*?)</p>";	
	
	@Override
	public String register(EJBCARegisterInput input) {
    	System.out.println("cn=" + input.getCn());
    	System.out.println("email=" + input.getEmail());

    	String msg = "";
    	try {
        	HashMap<String, String> headers = new HashMap<String, String>();
        	headers.put("hidemenu", "false");
        	headers.put("certType", "1");
        	headers.put("dnfield_0", input.getCn());
        	headers.put("email", input.getEmail());
        	headers.put("code", input.getEmail().charAt(input.getEmail().length() - 1) + "");
			Response res = ua.post(headers, ejbcaRegistrationUrl);
			//System.out.println(res.html);
			msg = res.code + "";
			if (res.code == HttpConnection.OK) {
				msg += " : " + Parser.extract(1, RESPONSE_REGEXP, res.html);
			} 
		} catch (Exception e) {
			msg = e.getMessage();
		}		
		System.out.println("msg=" + msg);
    	
		return msg;
	}

	@Override
	public byte[] enrollAndGetCert(EJBCAEnrollAndGetCertInput input,
			@WebParam(header = true, mode = Mode.OUT) Holder<String> headerParam) {
		/*
		 * DN for simplicity only includes CN
		 */
		String dn = "CN=" + input.getCn();

		System.out.println("cn=" + input.getCn());
    	System.out.println("dn=" + dn);
    	System.out.println("challenge=" + input.getChallenge());
    	
    	byte[] out = null;
    	try {
	        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	
	        KeyManager km = new KeyManager();
	        CertUtil certutil = new CertUtil();
            Client client = new Client(new URL(ejbcaScepUrl), new ConsoleCallbackHandler());
	
	        System.out.println("Generating RSA key...");
	        KeyPair kp = km.createRSA(keySize);	
	        X509Certificate cert = certutil.createSelfSignedCertificate(kp, dn);
	
	        System.out.println("Generating PKCS#10 signing request...");
	        PKCS10CertificationRequest request = certutil.createCertificationRequest(
	        	kp,
				dn,
				input.getChallenge());	

	        System.out.println("Getting CA certificates...");
            CertStore caCertificateStore = client.getCaCertificate(caIdentifier);
            Collection <? extends Certificate> caCertificates = caCertificateStore.getCertificates(null);
            System.out.println("Received " + caCertificates.size() + " CA certificate(s).");
            
            System.err.println("Starting enrollment request...");
            EnrollmentResponse response = client.enrol(cert,
				kp.getPrivate(),
				request,
				caIdentifier);            
            
            if (response.isSuccess()) {
                System.err.println("Enrollment request successful!");
                CertStore store = response.getCertStore();
                Collection<? extends Certificate> certs = store.getCertificates(null);

                System.out.println("Received response containing " + certs.size() + " certificate(s).");
                X509Certificate certificate = (X509Certificate)certs.toArray()[0];
                out = certutil.savePKCS12ToByteArray(input.getCn(), certificate, kp.getPrivate(), 
                	sqlUtil.getPassword(
                		sqlUtil.getJNDIConnection(datasourceContext), input.getCn()));
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


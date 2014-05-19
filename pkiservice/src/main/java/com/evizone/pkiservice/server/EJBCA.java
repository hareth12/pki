package com.evizone.pkiservice.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;

@WebService(targetNamespace = "http://pkiservice.evizone.com/")
public interface EJBCA {
	
    /**
     * Sends user registration request to EJBCA
     * @param input Input containing user's common name and email
     * @return Status message
     */
    String register(EJBCARegisterInput input);
    
    /**
     * Enrolls CSR, retreives user certificate from RA and returns PKCS#12 encrypted with 
     * one-time password
     * @param input Input including user's common name, subject DN to request 
     * (in the example profile defined as CN=<cn>) and challenge sent after registration 
     * request approval in notification email
     * @return Status message
     */
    byte[] enrollAndGetCert(EJBCAEnrollAndGetCertInput input, Holder<String> headerParam);
    
}


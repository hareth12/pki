package com.evizone.pkiservice.server;

import javax.jws.WebService;

@WebService(targetNamespace = "http://pkiservice.evizone.com/")
public interface OpenXPKI {
	
    /**
     * Enrolls CSR 
     * @param cn User common name
     * @param dn Subject DN to request (in the example profile defined as CN=<cn>)
     * @param url URL of SCEP service
     * @param challenge Password to include in CSR
     * @return Transaction ID
     */
    String enroll(String cn, String dn, String url, String challenge);

    /**
     * Polls status of CSR request and retreives cert to local store if available 
     * @param cn User common name
     * @param url URL of SCEP service
     * @param transactionID Transaction ID
     * @return Status message
     */
    CSRStatus poll(String cn, String url, String transactionID);

    /**
     * Retreives certificate from local store 
     * @param cn User common name
     * @param url URL of SCEP service
     * @param transactionID Transaction ID
     * @return Status message
     */
    byte[] download(String cn, String url, String transactionID);
    
}


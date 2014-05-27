package com.evizone.pkiservice.server;

import javax.jws.WebService;

@WebService(targetNamespace = "http://pkiservice.evizone.com/")
public interface OpenXPKI {
	
    /**
     * Enrolls CSR 
     * @param input Input including:
     * - dn Subject DN to request (in the example profile defined as CN=<cn>)
     * - url URL of SCEP service
     * - challenge Password to include in CSR
     * @return Transaction ID
     */
    String enroll(OpenXPKIEnrollInput input);

    /**
     * Polls status of CSR request and retreives cert to local store if available 
     * @param input Input including:
     * - cn User common name
     * - oneTimePassword User one-time-password
     * @return Status message
     */
    CSRStatus poll(OpenXPKIPollInput input);

    /**
     * Retreives certificate from local store 
     * @param input Input including:
     * - cn User common name
     * - oneTimePassword User one-time-password
     * @return Byte array containing pkcs#12 file
     */
    byte[] download(OpenXPKIPollInput input);
    
}


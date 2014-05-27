package com.evizone.pkiservice.server;

import javax.jws.WebService;

@WebService(targetNamespace = "http://pkiservice.evizone.com/")
public interface Authorization {
	
    /**
     * Sends user registration request to EJBCA
     * @param user User to register
     * return Status message
     */
    String ejbcaRegister(AuthorizationRegisterInput user);
    
    /**
     * Sends user registration request to OpenXPKI
     * @param user User to register
     * @return Status message
     */
    String openxpkiRegister(AuthorizationRegisterInput user);
    
}


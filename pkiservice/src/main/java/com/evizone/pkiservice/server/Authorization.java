package com.evizone.pkiservice.server;

import javax.jws.WebService;

@WebService(targetNamespace = "http://pkiservice.evizone.com/")
public interface Authorization {
	
    /**
     * Sends user registration request to EJBCA
     * @param user User to register
     */
    String register(AuthorizationRegisterInput user);    
    
}


package com.evizone.pkiservice.server;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

@WebService(endpointInterface = "com.evizone.pkiservice.server.OpenXPKI",
            targetNamespace = "http://pkiservice.evizone.com/",
            name = "OpenXPKI",
            serviceName = "OpenXPKIService",
            portName = "OpenXPKIPort")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
public class OpenXPKIImpl implements OpenXPKI {

	@Override
	public String enroll(
			@WebParam(name = "cn") String cn, 
			@WebParam(name = "dn") String dn, 
			@WebParam(name = "url") String url,
			@WebParam(name = "challenge") String challenge) {
    	System.out.println("cn=" + cn);
    	System.out.println("dn=" + dn);
    	System.out.println("url=" + url);
		return "OK";
	}

	@Override
	public CSRStatus poll(
			@WebParam(name = "cn") String cn, 
			@WebParam(name = "url") String url, 
			@WebParam(name = "transactionID") String transactionID) {
    	System.out.println("cn=" + cn);
    	System.out.println("url=" + url);
    	System.out.println("transactionID=" + transactionID);
    	return CSRStatus.SUCCESS;
	}

	@Override
	public byte[] download(
			@WebParam(name = "cn") String cn, 
			@WebParam(name = "url") String url, 
			@WebParam(name = "transactionID") String transactionID) {
    	System.out.println("cn=" + cn);
    	System.out.println("url=" + url);
    	System.out.println("transactionID=" + transactionID);
    	return "OK".getBytes();
	}

}


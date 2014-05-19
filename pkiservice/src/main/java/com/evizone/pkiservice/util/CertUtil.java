/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evizone.pkiservice.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.PKCS12PfxPduBuilder;
import org.bouncycastle.pkcs.PKCS12SafeBag;
import org.bouncycastle.pkcs.PKCS12SafeBagBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/**
 *
 * @author asyd
 */
public class CertUtil {

    /*
     * @description This method create a self signed certificated
     */
    public X509Certificate createSelfSignedCertificate(KeyPair kp, String dn) throws Exception {
        Date now = new Date();
        BigInteger serial = new BigInteger("1");

        X500Principal principal = new X500Principal(dn);

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        certGen.setIssuerDN(principal);
        certGen.setSubjectDN(principal);
        certGen.setSerialNumber(serial);
        certGen.setNotBefore(now);
        certGen.setNotAfter(now);
        certGen.setPublicKey(kp.getPublic());
        certGen.setSignatureAlgorithm("SHA1withRSA");


        return certGen.generate(kp.getPrivate(), "BC");
    }

    public PKCS10CertificationRequest createCertificationRequest(KeyPair kp, String dn, String password) {
        PKCS10CertificationRequest request = null;

        try {
            JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(new X500Principal(dn), kp.getPublic());
            DERPrintableString passwordDer = new DERPrintableString(password);
            builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, passwordDer);

            JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA1withRSA");
            request = builder.build(signerBuilder.build(kp.getPrivate()));
/*
            request = new PKCS10CertificationRequest("SHA1withRSA",
                    parseDN(dn),
                    kp.getPublic(),
                    attributes,
                    kp.getPrivate());
*/

        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }
        return request;
    }

    public X500Principal parseDN(String dn) {
        return new X500Principal(dn);
    }
    
    public X509Certificate loadCertFromPEM(String filename) {
    	X509Certificate cert = null;
    	PemReader pemReader = null;
        try {
        	pemReader = new PemReader(new FileReader(filename));
        	PemObject pem = pemReader.readPemObject();
        	CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        	InputStream in = new ByteArrayInputStream(pem.getContent());
        	cert = (X509Certificate)certFactory.generateCertificate(in);
        } catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("Could not load certificate: " + e.getMessage());
        } finally {
        	try {
				pemReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return cert;
    }
    
    public PrivateKey loadPrivateKey(String filename) {
    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    	PrivateKey privKey = null;
    	PemReader pemReader = null;
        try {
        	pemReader = new PemReader(new FileReader(filename));
        	PemObject pem = pemReader.readPemObject();
        	KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
        	PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(pem.getContent());
        	privKey = kf.generatePrivate(ks);        	
        } catch (Exception e) {
        	e.printStackTrace();
        	System.out.println("Could not load private key: " + e.getMessage());
        } finally {
        	try {
				pemReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return privKey;
    }    
    
    public void savePKCS12(String filename, X509Certificate cert, PrivateKey privKey, String password) {
    	PKCS12PfxPdu pfx = null;
    	try {
	    	JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
	
	        PKCS12SafeBagBuilder eeCertBagBuilder = new JcaPKCS12SafeBagBuilder(cert);
	
	        eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Eric's Key"));
	        eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(cert.getPublicKey()));
	
	        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(privKey, 
	        	new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, 
	        		new CBCBlockCipher(new DESedeEngine())).build(
	        			password.toCharArray()));
	
	        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Eric's Key"));
	        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(cert.getPublicKey()));
	
	        PKCS12PfxPduBuilder pfxPduBuilder = new PKCS12PfxPduBuilder();
	        PKCS12SafeBag[] certs = new PKCS12SafeBag[1];
	        certs[0] = eeCertBagBuilder.build();
	
	        pfxPduBuilder.addEncryptedData(
	        	new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, 
	        		new CBCBlockCipher(new RC2Engine())).build(
		        			password.toCharArray()), certs);
	
	        pfxPduBuilder.addData(keyBagBuilder.build());
	
	        pfx = pfxPduBuilder.build(
	        	new BcPKCS12MacCalculatorBuilder(), 
	        		password.toCharArray());
	        
			OutputStream pfxOut = new FileOutputStream("test.p12");
			pfxOut.write(pfx.toASN1Structure().getEncoded(ASN1Encoding.DL));
			pfxOut.close();	        
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.out.println("Could not generate PKCS#12: " + e.getMessage());
    	}
    }
    
    public byte[] savePKCS12ToByteArray(String cn, X509Certificate cert, PrivateKey privKey, String password) {
    	PKCS12PfxPdu pfx = null;
    	byte[] out = null;
    	try {
	    	JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
	
	        PKCS12SafeBagBuilder eeCertBagBuilder = new JcaPKCS12SafeBagBuilder(cert);
	
	        eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(cn + "'s Key"));
	        eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(cert.getPublicKey()));
	
	        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(privKey, 
	        	new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, 
	        		new CBCBlockCipher(new DESedeEngine())).build(
	        			password.toCharArray()));
	
	        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Eric's Key"));
	        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(cert.getPublicKey()));
	
	        PKCS12PfxPduBuilder pfxPduBuilder = new PKCS12PfxPduBuilder();
	        PKCS12SafeBag[] certs = new PKCS12SafeBag[1];
	        certs[0] = eeCertBagBuilder.build();
	
	        pfxPduBuilder.addEncryptedData(
	        	new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, 
	        		new CBCBlockCipher(new RC2Engine())).build(
		        			password.toCharArray()), certs);
	
	        pfxPduBuilder.addData(keyBagBuilder.build());
	
	        pfx = pfxPduBuilder.build(
	        	new BcPKCS12MacCalculatorBuilder(), 
	        		password.toCharArray());
	        
	        out = pfx.toASN1Structure().getEncoded(ASN1Encoding.DL);
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.out.println("Could not generate PKCS#12: " + e.getMessage());
    	}
    	return out; 
    }    
    
    public void saveToPEM(String filename, Object data) {
        try {
            PEMWriter writer = new PEMWriter(new FileWriter(new File(filename), true));
            writer.writeObject(data);
            writer.close();
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Could not save file: " + e.getMessage());
        }
    }    
	
    public byte[] savePEMToByteArray(Object data) {
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

}

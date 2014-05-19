package org.jboss.tools.examples.pkiservice;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.PKCS12PfxPduBuilder;
import org.bouncycastle.pkcs.PKCS12SafeBag;
import org.bouncycastle.pkcs.PKCS12SafeBagBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class PKCS12Test {
	
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
    
    public void generatePKCS12(String filename, X509Certificate cert, PrivateKey privKey, String password) {
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
    
	public static void main(String[] args) throws IOException {
		X509Certificate cert = new PKCS12Test().loadCertFromPEM("c:\\java\\projects\\evizone-sso\\jscep-cli-jdk6\\cert.pem");
		PrivateKey privKey = new PKCS12Test().loadPrivateKey("c:\\java\\projects\\evizone-sso\\jscep-cli-jdk6\\privkey.pem");
		new PKCS12Test().generatePKCS12("test.p12", cert, privKey, "test");		
	}

}

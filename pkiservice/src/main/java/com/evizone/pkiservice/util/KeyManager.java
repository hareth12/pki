/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evizone.pkiservice.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 *
 * @author asyd
 */
public class KeyManager {

    public KeyPair createRSA(Integer keySize) {
        KeyPairGenerator kpg;
        KeyPair kp = null;
        
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(keySize.intValue());
            kp = kpg.genKeyPair();
            
        } catch (Exception e) {
            
        }

        return kp;
    }
}

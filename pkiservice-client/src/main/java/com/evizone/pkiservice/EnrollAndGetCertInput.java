
package com.evizone.pkiservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enrollAndGetCertInput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="enrollAndGetCertInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="challenge" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "enrollAndGetCertInput", propOrder = {
    "cn",
    "challenge"
})
public class EnrollAndGetCertInput {

    @XmlElement(required = true)
    protected String cn;
    @XmlElement(required = true)
    protected String challenge;

    /**
     * Gets the value of the cn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCn() {
        return cn;
    }

    /**
     * Sets the value of the cn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCn(String value) {
        this.cn = value;
    }

    /**
     * Gets the value of the challenge property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChallenge() {
        return challenge;
    }

    /**
     * Sets the value of the challenge property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChallenge(String value) {
        this.challenge = value;
    }

}

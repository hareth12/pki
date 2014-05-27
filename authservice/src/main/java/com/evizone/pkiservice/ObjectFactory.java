
package com.evizone.pkiservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.evizone.pkiservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Poll_QNAME = new QName("http://pkiservice.evizone.com/", "poll");
    private final static QName _DownloadResponse_QNAME = new QName("http://pkiservice.evizone.com/", "downloadResponse");
    private final static QName _EnrollResponse_QNAME = new QName("http://pkiservice.evizone.com/", "enrollResponse");
    private final static QName _Download_QNAME = new QName("http://pkiservice.evizone.com/", "download");
    private final static QName _PollResponse_QNAME = new QName("http://pkiservice.evizone.com/", "pollResponse");
    private final static QName _Enroll_QNAME = new QName("http://pkiservice.evizone.com/", "enroll");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.evizone.pkiservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Poll }
     * 
     */
    public Poll createPoll() {
        return new Poll();
    }

    /**
     * Create an instance of {@link PollResponse }
     * 
     */
    public PollResponse createPollResponse() {
        return new PollResponse();
    }

    /**
     * Create an instance of {@link Download }
     * 
     */
    public Download createDownload() {
        return new Download();
    }

    /**
     * Create an instance of {@link EnrollResponse }
     * 
     */
    public EnrollResponse createEnrollResponse() {
        return new EnrollResponse();
    }

    /**
     * Create an instance of {@link DownloadResponse }
     * 
     */
    public DownloadResponse createDownloadResponse() {
        return new DownloadResponse();
    }

    /**
     * Create an instance of {@link Enroll }
     * 
     */
    public Enroll createEnroll() {
        return new Enroll();
    }

    /**
     * Create an instance of {@link EnrollInput }
     * 
     */
    public EnrollInput createEnrollInput() {
        return new EnrollInput();
    }

    /**
     * Create an instance of {@link PollInput }
     * 
     */
    public PollInput createPollInput() {
        return new PollInput();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Poll }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pkiservice.evizone.com/", name = "poll")
    public JAXBElement<Poll> createPoll(Poll value) {
        return new JAXBElement<Poll>(_Poll_QNAME, Poll.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DownloadResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pkiservice.evizone.com/", name = "downloadResponse")
    public JAXBElement<DownloadResponse> createDownloadResponse(DownloadResponse value) {
        return new JAXBElement<DownloadResponse>(_DownloadResponse_QNAME, DownloadResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnrollResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pkiservice.evizone.com/", name = "enrollResponse")
    public JAXBElement<EnrollResponse> createEnrollResponse(EnrollResponse value) {
        return new JAXBElement<EnrollResponse>(_EnrollResponse_QNAME, EnrollResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Download }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pkiservice.evizone.com/", name = "download")
    public JAXBElement<Download> createDownload(Download value) {
        return new JAXBElement<Download>(_Download_QNAME, Download.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PollResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pkiservice.evizone.com/", name = "pollResponse")
    public JAXBElement<PollResponse> createPollResponse(PollResponse value) {
        return new JAXBElement<PollResponse>(_PollResponse_QNAME, PollResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Enroll }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pkiservice.evizone.com/", name = "enroll")
    public JAXBElement<Enroll> createEnroll(Enroll value) {
        return new JAXBElement<Enroll>(_Enroll_QNAME, Enroll.class, null, value);
    }

}

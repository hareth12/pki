package com.evizone.pkiservice.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.samcik.java.io.HttpConnection;
import net.samcik.java.utils.Parser;
import net.samcik.java.utils.UserAgent;
import net.samcik.java.utils.UserAgent.Response;
import pl.smsapi.Client;
import pl.smsapi.api.SmsFactory;
import pl.smsapi.api.action.sms.Send;
import pl.smsapi.api.response.MessageResponse;
import pl.smsapi.api.response.StatusResponse;
import pl.smsapi.exception.ActionException;
import pl.smsapi.exception.ClientException;
import pl.smsapi.exception.HostException;
import pl.smsapi.exception.SmsapiException;

import com.evizone.pkiservice.util.SqlUtil;

@WebService(endpointInterface = "com.evizone.pkiservice.server.Authorization",
            targetNamespace = "http://pkiservice.evizone.com/",
            name = "Authorization",
            serviceName = "AuthorizationService",
            portName = "AuthorizationPort")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
public class AuthorizationImpl implements Authorization {
	
	@Resource(name = "ejbcaRegistrationUrl")
	private String ejbcaRegistrationUrl;
	
	@Resource(name = "mailContext")
	private String mailContext;	
	
	@Resource(name = "smsapiLogin")
	private String smsapiLogin;
	
	@Resource(name = "smsapiPassword")
	private String smsapiPassword;
	
	@Resource(name = "datasourceContext")
	private static String datasourceContext;	
	
	private SqlUtil sqlUtil = new SqlUtil();
	
	private final String RESPONSE_REGEXP = "<h1 class=\"title\">Request Registration</h1>\\s+<p>(.*?)</p>";			

	private final String EMAIL_REGEXP = "\\b[A-Z0-9._%+-]+@(?:[A-Z0-9-]+\\.)+[A-Z]{2,4}\\b";	
	
	@Override
	public String register(
			@WebParam(name = "user") AuthorizationRegisterInput user) {
		if (isInputValid(user)) {
			Connection conn = sqlUtil.getJNDIConnection(datasourceContext);
			try {
				if (conn != null) {
					if (!sqlUtil.userExists(conn, user.getCn())) {
						String password = UUID.randomUUID().toString().substring(0, 8);
						int pin = 1000 + new Random().nextInt(9000);
						
						int res = sqlUtil.insertPrincipal(conn, 
							user.getCn(), 
							password,
							pin,
							user.getEmail(), 
							user.getPhoneNo());
						res += sqlUtil.insertRole(conn, 
								user.getCn());
						
						if (res == 2) {
							/*
							 * New user registration request sent to EJBCA
							 */
							sendSelfRegistrationRequest(user.getCn(), user.getEmail());
							/*
							 * Confirmation email with one-time-password
							 */
							sendConfirmationEmail(user.getCn(), user.getEmail(), password);
							/*
							 * SMS with PIN code
							 */
							sendConfirmationSMS(user.getPhoneNo(), pin);
							
							return "OK";
						} else {
							return "No rows have been updated";
						}
					} else {
						return "User with the given username already exits";
					}
				} else {
					return "Connection error";
				}
			} catch (Exception e) {
				return e.getMessage();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			return "Invalid input";
		}
	}
	
	private void sendSelfRegistrationRequest(String cn, String email) {
		UserAgent ua = new UserAgent(new HttpConnection());
    	try {
        	HashMap<String, String> headers = new HashMap<String, String>();
        	headers.put("hidemenu", "false");
        	headers.put("certType", "1");
        	headers.put("dnfield_0", cn);
        	headers.put("email", email);
        	headers.put("code", email.charAt(email.length() - 1) + "");
			Response res = ua.post(headers, ejbcaRegistrationUrl);
			boolean ok = false;
			System.out.println("EJBCA Response Code: " + res.code);
			if (res.code == HttpConnection.OK) {
				String msg = Parser.extract(1, RESPONSE_REGEXP, res.html);
				System.out.println(msg);
				if (msg.contains("The registration request has been successfully submitted ")) {
					ok = true;
				} 
			} else {
				//ERROR
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("CA self-registration failed: " + e.getMessage());
		}				
	}
	
	private void sendConfirmationEmail(String cn, String email, String password) {
        try {
        	/*
        	 * Jboss7 does not allow properties configuration in mail-service
        	 */
        	/*
        	Properties props = new Properties();
		    props.put("mail.transport.protocol", "smtp");
		    props.put("mail.host", "smtp.gmail.com");
		    props.put("mail.port", "465");
		    props.put("mail.smtp.ssl.enable", "true");
		    props.put("mail.smtp.ssl.trust", "*");
		    props.put("mail.smtp.auth", "true");
		    
		    Session mailSession = Session.getDefaultInstance(props, 
		    	new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("tomeksamcik@gmail.com","as0rtsao");
					}
				});
			*/                        
        	Session mailSession = getJNDIMailSession();
		    
            MimeMessage m = new MimeMessage(mailSession);
            Address from = new InternetAddress("authorization_service@evizone.com");
            Address[] to = new InternetAddress[] { new InternetAddress(
                email) };
            m.setFrom(from);
            m.setRecipients(Message.RecipientType.TO, to);
            m.setSubject("Registration Confirmation");
            m.setContent("CN: " + cn + "\nOne-time-password: " + password, "text/plain");
            Transport.send(m);

            System.out.println("Mail Sent Successfully.");
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        	System.out.println("Send email failed: " + e.getMessage());
        }		
	}

	private void sendConfirmationSMS(int phoneNo, int pin) {
		try {

		    Client client = new Client(smsapiLogin);
		    client.setPasswordHash(smsapiPassword);

		    SmsFactory smsApi = new SmsFactory(client);
		    Send action = smsApi.actionSend()
		            .setText("Evizone Authorization Service PIN: " + pin)
		            .setTo(String.valueOf(phoneNo))
		            .setSender("ECO");

		    StatusResponse result = action.execute();

		    for(MessageResponse status : result.getList() ) {

		        System.out.println( status.getNumber() + " " + status.getStatus() );
		    }

		} catch(ActionException e) {
		    /**
		     * Błędy związane z akcją (z wyłączeniem błędów 101,102,103,105,110,1000,1001 i 8,666,999,201)
		     * http://www.smsapi.pl/sms-api/kody-bledow
		     */
		    System.out.println(e.getMessage());
		} catch(ClientException e) {
		    /**
		     * 101 Niepoprawne lub brak danych autoryzacji.
		     * 102 Nieprawidłowy login lub hasło
		     * 103 Brak punków dla tego użytkownika
		     * 105 Błędny adres IP
		     * 110 Usługa nie jest dostępna na danym koncie
		     * 1000 Akcja dostępna tylko dla użytkownika głównego
		     * 1001 Nieprawidłowa akcja
		     */
		    System.out.println(e.getMessage());
		} catch(HostException e) {
		    /* błąd po stronie servera lub problem z parsowaniem danych
		     *
		     * 8 - Błąd w odwołaniu
		     * 666 - Wewnętrzny błąd systemu
		     * 999 - Wewnętrzny błąd systemu
		     * 201 - Wewnętrzny błąd systemu
		     */
		    System.out.println(e.getMessage());
		} catch(SmsapiException e) {
		    System.out.println(e.getMessage());
		}				
	}
	
	private boolean isInputValid(AuthorizationRegisterInput user) {
		if (!Parser.match(EMAIL_REGEXP, user.getEmail())) {
			return false;
		}
		if (user.getPhoneNo().toString().length() != 9) {
			return false;
		}
		return true;
	}
	
	private Session getJNDIMailSession() {
		Session result = null;
		try {
			Context initialContext = new InitialContext();
			result = (Session)initialContext.lookup(mailContext);
			if (result == null) {
				System.out.println("Failed to lookup mail session.");
			}
		} catch (Exception ex) {
			System.out.println("Cannot get mail session: " + ex);
		}
		return result;
	}	
	
}
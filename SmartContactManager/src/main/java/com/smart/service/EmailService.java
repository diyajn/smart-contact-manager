package com.smart.service;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;



@Service
public class EmailService {

	public boolean sendEmail(String subject,String message,String to) {
		 boolean f=false;
		 String from="studentdiyajain@gmail.com";
		 
		 //varaible for gmail host
		 String host="smtp.gmail.com";
		 
		 //get the system properties
		 Properties properties=System.getProperties();
		 
		 //set the info to properties
		 properties.put("mail.smtp.host", host);
		 properties.put("mail.smtp.port", "465");
		 properties.put("mail.smtp.ssl.enable","true");
		 properties.put("mail.smtp.auth", "true");
		 
		 //step1:to get the session object
			Session session = Session.getInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("studentdiyajain@gmail.com", "ebddbdsgnygyltph");
				}
			});
	
		 //step2:composed the message
			MimeMessage m=new MimeMessage(session);
			try {
				
				m.setFrom(new InternetAddress(from));
				m.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
				m.setSubject(subject);
				
				//to send nomral message
				//m.setText(message);
				
				//to send message in html form
				m.setContent(message,"text/html");
				
				//step3:send the message 
				Transport.send(m);
				f=true;
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		    return f;
	 }
	
}

package com.contact.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {

		boolean f = false;

		String from = "chandangope555.cg@gmail.com";

		// Variable for gmail host
		String host = "smtp.gmail.com";

		// getting system properties

		Properties properties = System.getProperties();

		// setting important information to properties object

		// setting host
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// Step 1: Setting session Object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {

				return new javax.mail.PasswordAuthentication("chandangope555.cg@gmail.com", "***password***");
			}

		});

		session.setDebug(true);

		// Step 2: Composing the message
		MimeMessage m = new MimeMessage(session);

		try {

			// from email
			m.setFrom(from);

			// adding recipient to message

			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject to message
			m.setSubject(subject);

			// adding text to message
			m.setContent(message, "text/html");

			// Step 3: send message using Transport class

			Transport.send(m);

			f = true;

		} catch (Exception e) {

			e.printStackTrace();

		}
		return f;

	}

}

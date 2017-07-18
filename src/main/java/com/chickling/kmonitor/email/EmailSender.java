package com.chickling.kmonitor.email;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmonitor.config.AppConfig;

/**
 * @author Hulva Luva.H
 *
 */
public class EmailSender {
	private static Logger LOG = LoggerFactory.getLogger(EmailSender.class);

	private static AppConfig config;

	public static void setConfig(AppConfig _config) {
		config = _config;
	}

	public static void sendEmail(String message, String sendTo, String group_topic) {
		Properties properties = System.getProperties();

		if (config.getSmtpAuth()) {
			properties.setProperty("mail.user", config.getSmtpUser());
			properties.setProperty("mail.password", config.getSmtpPasswd());
		}
		properties.setProperty("mail.smtp.host", config.getSmtpServer());

		Session session = Session.getDefaultInstance(properties);

		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			String[] sendToArr = sendTo.split(";");
			mimeMessage.setFrom(new InternetAddress(config.getMailSender()));
			if (sendToArr.length > 1) {
				String cc = "";
				for (int i = 1; i < sendToArr.length; i++) {
					cc += i == sendToArr.length - 1 ? sendToArr[i] : sendToArr[i] + ",";
				}
				mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
			}
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(sendToArr[0]));
			
			String[] group_topicArr = group_topic.split("_");
			String subject = config.getMailSubject();
			if(subject.contains("{group}")) {
				subject.replace("{group}", group_topicArr[0]);
			}
			if(subject.contains("{topic}")) {
				subject.replace("{topic}", group_topicArr[1]);
			}
			mimeMessage.setSubject(config.getMailSubject());
			mimeMessage.setSentDate(new Date());
			mimeMessage.setContent(message, "text/html");

			Transport.send(mimeMessage);
		} catch (

		Exception e) {
			LOG.error("sendEmail faild!", e);
		}
	}
}

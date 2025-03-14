package server;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import logic.Subscriber;

/**
 * MessageController handles the sending of messages, including emails and
 * Imitation of sending an SMS, to subscribers in the system.
 */
public class MessageController {

	private Session session;
	private static MessageController instance;

	/**
	 * Singleton pattern to ensure only one instance of MessageController.
	 * 
	 * @return the single instance of MessageController.
	 */
	public static MessageController getInstance() {
		if (!(instance instanceof MessageController)) {
			instance = new MessageController();
		}
		return instance;
	}

	/**
	 * Private constructor initializes the email session with necessary properties
	 * and authentication details for the SMTP server.
	 */
	private MessageController() {
		initialize();
	}

	/**
	 * Initializes the email session with the SMTP server properties and
	 * authentication.
	 */
	private void initialize() {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
		props.put("mail.smtp.port", "587"); // TLS Port
		props.put("mail.smtp.auth", "true"); // enable authentication
		props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS
		Authenticator auth = new Authenticator() {
			// override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				// The API key was hardcoded to fit the requirement of our course, focusing on functionality over production-level security.
				return new PasswordAuthentication("libraryblib@gmail.com", "jtzm oybh unum qptr");
			}
		};
		session = Session.getInstance(props, auth);
	}

	/**
	 * Sends a message (both email and SMS) to the subscriber.
	 * 
	 * @param subTo   the subscriber to whom the message will be sent.
	 * @param subject the subject of the email.
	 * @param text    the body of the message.
	 */
	public void sendMessage(Subscriber subTo, String subject, String text) {
		sendEmail(subTo, subject, text);
		sendSMS(subTo.getPhone(), text);
	}

	/**
	 * Sends an email to the subscriber.
	 * 
	 * @param subTo   the subscriber to whom the email will be sent.
	 * @param subject the subject of the email.
	 * @param text    the body of the email.
	 */
	public void sendEmail(Subscriber subTo, String subject, String text) {
		Thread t = new Thread(() -> {
			String emailTo = subTo.getEmail();
			System.out.println("sending email to " + emailTo);

			try {
				MimeMessage msg = new MimeMessage(session);
				msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
				msg.addHeader("format", "flowed");
				msg.addHeader("Content-Transfer-Encoding", "8bit");
				msg.setSubject(subject, "UTF-8");

				msg.setText(text, "UTF-8");

				msg.setSentDate(new Date());
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo, false));
				Transport.send(msg);
				System.out.println("email sent");
			} catch (MessagingException mex) {
				System.out.println("send failed, exception: " + mex);
				mex.printStackTrace();
			}
		});
		t.start();
	}

	/**
	 * Simulates sending an SMS by displaying the recipient's phone number and the
	 * message.
	 * 
	 * @param phoneTo the phone number to which the SMS will be sent.
	 * @param msg     the message to be sent via SMS.
	 */
	public void sendSMS(String phoneTo, String msg) {
		// Simulate the process of sending an SMS by printing the phone number and
		// message.
		Thread t = new Thread(() -> {
			System.out.println("sending SMS to " + phoneTo);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("SMS sent");
		});
		t.start();
	}
}

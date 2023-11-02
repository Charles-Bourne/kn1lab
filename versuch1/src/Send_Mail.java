import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage; 

public class Send_Mail {
	public static void main(String[] args) {
		sendMail();   
	}
	
	public static void sendMail() {
		try {
			// Setzen der Properties und deren Werte
			Properties props = new Properties();
			props.put("mail.smtp.host", "localhost"); // SMTP-Host
			props.put("mail.smtp.auth", "false");
			props.put("mail.smtp.starttls.enable", "false");

			// Session-Objekt erstellen
			Session session = Session.getInstance(props);

			// Erstellen der Nachricht
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("from-labrat@localhost"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("labrat@localhost"));
			message.setSubject("Testnachricht Betreff");
			message.setText("Hallo Welt!");

			// Nachricht senden
			Transport.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

public class Receive_Mail {
	public static void main(String[] args) throws Exception {
		fetchMail();
	}
	
	public static void fetchMail() {
		try {
			// your code here
			// Setzen der Properties und deren Werte
			Properties props = new Properties();
			props.put("mail.store.protocol", "pop3");  // Verwendetes Protokoll
			props.put("mail.pop3.host", "localhost");  // POP3-Host-Name

			// Session-Objekt erstellen
			Session session = Session.getDefaultInstance(props);

			// Verbindung zum Store herstellen
			Store store = session.getStore();
			store.connect();

			// Öffnen des Posteingangsfolders
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);

			// Anzahl der Nachrichten im Posteingang ermitteln
			int messageCount = inbox.getMessageCount();
			System.out.println("Anzahl der Nachrichten: " + messageCount);

			// Nachrichten abrufen und anzeigen
			Message[] messages = inbox.getMessages();
			for (Message message : messages) {
				System.out.println("Nachrichtennummer: " + message.getMessageNumber());
				System.out.println("Betreff: " + message.getSubject());
				System.out.println("Von: " + message.getFrom()[0]);
				System.out.println("Inhalt: " + message.getContent().toString());
				System.out.println("---------------------------");
			}

			// Ressourcen schließen
			inbox.close(false);
			store.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

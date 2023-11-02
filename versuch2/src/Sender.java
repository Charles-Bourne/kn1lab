import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Die "Klasse" Sender liest einen String von der Konsole und zerlegt ihn in einzelne Worte. Jedes Wort wird in ein
 * einzelnes {@link Packet} verpackt und an das Medium verschickt. Erst nach dem Erhalt eines entsprechenden
 * ACKs wird das nächste {@link Packet} verschickt. Erhält der Sender nach einem Timeout von einer Sekunde kein ACK,
 * überträgt er das {@link Packet} erneut.
 */
public class Sender {
    /**
     * Hauptmethode, erzeugt Instanz des {@link Sender} und führt {@link #send()} aus.
     * @param args Argumente, werden nicht verwendet.
     */
    public static void main(String[] args) {
        Sender sender = new Sender();
        try {
            sender.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Erzeugt neuen Socket. Liest Text von Konsole ein und zerlegt diesen. Packt einzelne Worte in {@link Packet}
     * und schickt diese an Medium. Nutzt {@link SocketTimeoutException}, um eine Sekunde auf ACK zu
     * warten und das {@link Packet} ggf. nochmals zu versenden.
     * @throws IOException Wird geworfen falls Sockets nicht erzeugt werden können.
     */
    private void send() throws IOException {
        // Socket erzeugen auf Port 9998 und Timeout auf eine Sekunde setzen
        DatagramSocket clientSocket = new DatagramSocket(9998);
        clientSocket.setSoTimeout(1000);

        // Empfängeradresse und Port setzen
        InetAddress destIPAddress = InetAddress.getByName("localhost");
        int destPort = 9997;

        // Text von Konsole lesen und in Worte zerlegen
        ArrayList<String> words = getWordsFromSentence( getSentenceFromConsole() );
        // EOT (End Of Transmission) anfügen
        words.add("EOT");
        
        // Iteration über den Konsolentext
        for (int seqNum = 0; seqNum < words.size(); seqNum++) {
           boolean ackReceived = false;
           while (!ackReceived) {
               //sende Wort
               sendWord(clientSocket, destIPAddress, destPort, seqNum, seqNum + 1, words.get(seqNum));
               // ACK empfangen
               try {
                   byte[] packetInBytes = new byte[256];
                   DatagramPacket packetInSerialized = new DatagramPacket(packetInBytes, packetInBytes.length);
                   // Auf ACK warten und erst dann Schleifenzähler inkrementieren
                   clientSocket.receive(packetInSerialized);
                   Packet ackPacket = getDeserializedPacket(packetInBytes);
                   if (ackPacket.isAckFlag() && ackPacket.getAckNum() == seqNum + 1) {
                       ackReceived = true;
                       System.out.println("Receive ACK for packet " + seqNum + " with ACK-Num " + ackPacket.getAckNum());
                   }
               } catch (SocketTimeoutException e) {
                   System.out.println("Receive timed out, retrying...");
               } catch (ClassNotFoundException e) {
                   e.printStackTrace();
               }
           }
       }
        
        // Wenn alle Packete versendet und von der Gegenseite bestätigt sind, Programm beenden
        clientSocket.close();
        if(System.getProperty("os.name").equals("Linux")) {
            clientSocket.disconnect();
        }

        System.exit(0);
    }

    private ArrayList<String> getWordsFromSentence(String sentence) {
        return new ArrayList<>(Arrays.asList(sentence.split(" ")));
    }

    private byte[] getSerializedPacket(Packet packet) throws IOException {
        // serialize Packet for sending
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(packet);
        return b.toByteArray();
    }

    private Packet getDeserializedPacket(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(data);
        ObjectInputStream o = new ObjectInputStream(b);
        return (Packet) o.readObject();
    }

    private void sendWord(DatagramSocket socket, InetAddress destInetAddress, int destPort, int seq, int ackNum, String word) throws IOException {
        // Erzeugen eines neuen Packets
        Packet packet = new Packet(seq, ackNum, false, word.getBytes());
        // Serialisieren des Packets für das Senden
        byte[] serializedPacket = getSerializedPacket(packet);
        // Erstellen eines DatagramPackets und Senden
        DatagramPacket packetSerialized = new DatagramPacket(serializedPacket, serializedPacket.length, destInetAddress, destPort);
        socket.send(packetSerialized);
    }

    private String getSentenceFromConsole() throws IOException {
        //Text von Konsole einlesen
        System.out.print("Enter a sentence: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return reader.readLine();
    }
}

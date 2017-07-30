package general;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Server;

public class Message {

    public static void sendStringTCP(Socket tcpSocket, String msg) {
        sendObjectTCP(tcpSocket, msg);
    }
    
    public static void sendObjectTCP(Socket tcpSocket, Object obj) {
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(tcpSocket.getOutputStream());
            outputStream.flush();
            outputStream.writeObject(obj);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

package general;

import config.ChatConfig;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import server.Room;
import server.Server;

public class General {

    public static void sendStringTCP(Socket tcpSocket, String msg) {
        sendObjectTCP(tcpSocket, msg);
    }

    public static void sendObjectTCP(Socket tcpSocket, Object obj) {

        if (tcpSocket.isConnected()) {
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

    public static void sendStringTCP(String ip, int port, String msg) throws IOException {
        Socket tcpSocket;
        ObjectOutputStream outputStream = null;

        tcpSocket = new Socket(ip, port);

        if (tcpSocket.isConnected()) {
            outputStream = new ObjectOutputStream(tcpSocket.getOutputStream());
            outputStream.flush();
            outputStream.writeObject(msg);
        }

        outputStream.close();
        tcpSocket.close();
    }

    public static void sendStringMulticast(String multicastIp, String msg) {

        InetAddress inetAddress;
        MulticastSocket socketMulticast;
        DatagramPacket dtgrm;

        try {
            inetAddress = InetAddress.getByName(multicastIp);

            socketMulticast = new MulticastSocket(ChatConfig.getMcastPort());
            socketMulticast.joinGroup(inetAddress);

            dtgrm = new DatagramPacket(
                    msg.getBytes("UTF-8"),
                    msg.length(),
                    inetAddress,
                    ChatConfig.getMcastPort());

            socketMulticast.send(dtgrm);

        } catch (UnknownHostException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void sendStringToUDPServer(String msg) {
        try {
            DatagramSocket socketUDP = new DatagramSocket();
            byte[] sendData = new byte[ChatConfig.getMessageMaxLength()];
            
            sendData = msg.getBytes("UTF-8");

            DatagramPacket sendPacket
                    = new DatagramPacket(
                            sendData,
                            sendData.length,
                            InetAddress.getByName(ChatConfig.getServerIp()),
                            ChatConfig.getUdpPort());

            socketUDP.send(sendPacket);
            
            socketUDP.close();

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}
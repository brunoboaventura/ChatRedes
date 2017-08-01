package config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import server.Room;

public class ChatConfig {

    private static ChatConfig instance = null;

    private static String serverIp;
    private static int tcpPort1;
    private static int tcpPort2;
    private static int udpPort;
    private static int mcastPort;
    private static int messageMaxLength;
    private static String XMLpath;

    private ChatConfig() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("Chat.properties"));

        serverIp = props.getProperty("chat.server.ip");
        tcpPort1 = Integer.parseInt(props.getProperty("chat.port.tcp1"));
        tcpPort2 = Integer.parseInt(props.getProperty("chat.port.tcp2"));
        udpPort = Integer.parseInt(props.getProperty("chat.port.udp"));
        mcastPort = Integer.parseInt(props.getProperty("chat.port.mcast"));
        messageMaxLength = Integer.parseInt(props.getProperty("chat.message.max_length"));

        XMLpath = getClass().getResource("rooms.xml").getPath();
    }

    public static ChatConfig getInstance() throws IOException {

        if (instance == null) {
            instance = new ChatConfig();
        }

        return instance;
    }

    public static String getServerIp() {
        return serverIp;
    }

    public static int getTcpPort1() {
        return tcpPort1;
    }

    public static int getTcpPort2() {
        return tcpPort2;
    }

    public static int getUdpPort() {
        return udpPort;
    }

    public static int getMcastPort() {
        return mcastPort;
    }

    public static int getMessageMaxLength() {
        return messageMaxLength;
    }

    public static String getXMLpath() {
        return XMLpath;
    }

    public static ArrayList<Room> getRooms() throws ParserConfigurationException, SAXException, IOException {

        ArrayList<Room> rooms = new ArrayList();
        NodeList nodeList;

        File fXmlFile = new File(ChatConfig.getXMLpath());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        nodeList = doc.getElementsByTagName("room");

        for (int i = 0; i < nodeList.getLength(); i++) {

            String name = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
            String ip = nodeList.item(i).getAttributes().getNamedItem("multicastIp").getNodeValue();

            rooms.add(new Room(name, ip));
        }

        return rooms;
    }

}

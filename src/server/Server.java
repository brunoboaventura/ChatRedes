package server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Server {

    // portas do chat (serão lidas do Chat.properties)
    private static int tcpPort;
    private static int udpPort;
    private static int mcastPort;

    private static int messageMaxLength;

    // socket TCP principal, onde o cliente se conecta
    // por esse socket o cliente receberá as salas disponíveis e
    // enviará os pedidos TCP:
    private ServerSocket socketTCP;

    // socket UDP que receberá as mensagens e fará o enfileiramento,
    // será usado em seguida para enviar as mensagens enfileiradas
    private DatagramSocket socketUDP;

    // fila (FIFO) para as mensagens (thread safe)
    private final ConcurrentLinkedQueue fifo = new ConcurrentLinkedQueue();

    // lista de clientes conectados
    private final ArrayList<ClientData> clients = new ArrayList();

    // lista de salas com os endereços multicast de cada uma
    private ArrayList<Room> rooms = new ArrayList();

    public Server() throws IOException, ParserConfigurationException, SAXException {
        setPorts();
        setRooms();
        setConfig();

        // criando os sockets
        socketTCP = new ServerSocket(tcpPort);
        socketUDP = new DatagramSocket(udpPort);

        // executando o loop principal, que vai escutar a porta TCP e devolver
        // a lista de salas para o usuário, bem como criar as threads para o
        // tratamento das mensagens (conexao e desconexao) e envio de arquivos
        mainLoop();

    }

    // ler as salas do arquivo rooms.xml
    private void setRooms() throws ParserConfigurationException, SAXException, IOException {

        NodeList nodeList;

        File fXmlFile = new File(getClass().getResource("/config/rooms.xml").getPath());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        nodeList = doc.getElementsByTagName("room");

        System.out.println(getClass().getResource("/config/rooms.xml").getPath());

        for (int i = 0; i < nodeList.getLength(); i++) {
            String room = nodeList.item(i).getChildNodes().item(1).getTextContent();
            String ip = nodeList.item(i).getChildNodes().item(2).getTextContent();

            rooms.add(new Room(room, ip));
        }
    }

    // funcao para ler as portas do arquivo Chat.properties
    private void setPorts() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/config/Chat.properties"));

        tcpPort = Integer.parseInt(props.getProperty("chat.port.tcp"));
        udpPort = Integer.parseInt(props.getProperty("chat.port.udp"));
        mcastPort = Integer.parseInt(props.getProperty("chat.port.mcast"));
    }

    // funcao para ler outras configurações do arquivo Chat.properties
    private void setConfig() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/config/Chat.properties"));

        messageMaxLength = Integer.parseInt(props.getProperty("chat.message.max_length"));
    }

    private InetAddress getInetAddressFromNickname(String nickname) {

        ClientData cli = null;

        for (int i = 0; i < clients.size(); i++) {
            cli = clients.get(i);
            if (cli.getNickname().equals(nickname)) {
                break;
            }
        }

        return cli.getInetAddress();
    }

    private void mainLoop() throws IOException {

                    // thread que vai receber todos os datagramas enviados pelos clientes
            Runnable udpReceiver;
            udpReceiver = () -> {
                System.out.println("UDP receiver OK.");

                while (true) {
                    // o socket UDP ficará aguardando os mensagens:
                    // MESSAGE:NICKNAME_DESTINO:NICKNAME_ORIGEM:TEXTO
                    byte[] receiveData = new byte[messageMaxLength];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        socketUDP.receive(receivePacket);

                        String sentence = new String(receivePacket.getData());

                        System.out.println("RECEIVED: " + sentence);

                        fifo.add(sentence);

                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            };

            Thread threadUdpReceiver = new Thread(udpReceiver);
            threadUdpReceiver.start();

            // thread que vai enviar as mensagens para os clientes.
            Runnable udpSender;
            udpSender = () -> {
                System.out.println("UDP Sender OK.");
                while (true) {
                    // verifica se existem mensagens na fila e as envia
                    if (!fifo.isEmpty()) {
                        try {
                            byte[] sendData = new byte[messageMaxLength];
                            String message = (String) fifo.poll();
                            String[] split = message.split(":");

                            InetAddress inetAddress = getInetAddressFromNickname(split[1]);

                            sendData = message.getBytes();

                            DatagramPacket sendPacket
                                    = new DatagramPacket(sendData, sendData.length, inetAddress, udpPort);

                            socketUDP.send(sendPacket);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            };

            Thread threadUdpSender = new Thread(udpSender);
            threadUdpSender.start();

            // thread que vai atualizar a lista de usuarios das salas
            // padrão das mensagens:
            // LIST:SALA:USUARIO1:USUARIO2:...
            Runnable multicastServer;
            multicastServer = () -> {
                System.out.println("Multicast server OK.");

                String list;
                ArrayList<String> lists = new ArrayList();

                while (true) {
                    for (int i = 0; i < rooms.size(); i++) {
                        Room room = rooms.get(i);

                        list = "LISTA:"
                                + room.getName()
                                + ":";

                        if (clients.size() > 0) {
                            for (int j = 0; j < clients.size(); j++) {
                                ClientData cli = clients.get(j);
                                if (room.getName().equals(cli.getSala())) {
                                    list += cli.getNickname() + ":";
                                }
                            }
                        }
                        System.out.println(list);
                        lists.add(list);
                    }

//                InetAddress inetAddress;
//                MulticastSocket socketMulticast;
//                DatagramPacket dtgrm;
//
//                for (int i = 0; i < lists.size(); i++) {
//                    String[] split = lists.get(i).split(":");
//                    try {
//                        System.out.println(InetAddress.getAllByName(split[2]).toString());
//                        inetAddress = InetAddress.getByName(split[2]);
//                        socketMulticast = new MulticastSocket(mcastPort);
//                        socketMulticast.joinGroup(inetAddress);
//                        dtgrm = new DatagramPacket(lists.get(i).getBytes(), lists.get(i).length(), inetAddress, mcastPort);
//                        socketMulticast.send(dtgrm);
//
//                    } catch (UnknownHostException ex) {
//                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (IOException ex) {
//                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                    lists.clear();
                }
            };

            Thread threadMulticastServer = new Thread(multicastServer);
            threadMulticastServer.start();

        

        while (true) {
            Socket clientSocket = socketTCP.accept();

            // ao aceitar uma conexão, o servidor cria uma thread que vai
            // enviar a lista de salas
            Runnable sendRooms;
            sendRooms = () -> {
                ObjectOutputStream outputStream;
                try {
                    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    outputStream.flush();
                    outputStream.writeObject(rooms);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

            };

            Thread tSendRooms = new Thread(sendRooms);
            tSendRooms.start();

            // thread que escuta o socket TCP que será usado para receber os
            // comandos e transferir arquivos (ou não - talvez um só pra isso)
            Runnable tcpServer;
            tcpServer = () -> {
                System.out.println("TCP server OK.");

                while (true) {
                    // o socket TCP ficará aguardando os comandos via string:
                    // CONNECT:NICKNAME:SALA - compando para conectar o chat
                    // DISCONNECT:NICKNAME - desconectar todos os sockets
                    // FILE:
                    
                    if (clientSocket.isClosed())
                        break;

                    ObjectInputStream inputStream = null;
                    try {
                        inputStream = new ObjectInputStream(clientSocket.getInputStream());
                        String command = (String) inputStream.readObject();

                        if (command.startsWith("CONNECT:")) {
                            String[] split = command.split(":");

                            ClientData cli = new ClientData(
                                    split[1],
                                    clientSocket,
                                    clientSocket.getInetAddress()
                            );

                            cli.setSala(split[2]);

                            for (int i = 0; i < rooms.size(); i++) {
                                Room room = rooms.get(i);
                                if (room.getName().equals(split[2])) {
                                    cli.setIpMulticast(room.getIpMulticast());
                                }
                            }

                            clients.add(cli);

                        }

                        System.out.println(command);

                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            Thread threadTcpServer = new Thread(tcpServer);
            threadTcpServer.start();
        }
    }

    public static void main(String argv[]) throws Exception {
        Server serv = new Server();
    }
}

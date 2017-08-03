package server;

import config.ChatConfig;
import general.General;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Server {

    // socket TCP principal, onde o cliente se conecta
    // por esse socket o cliente receberá as salas disponíveis
    private ServerSocket socketTCP1;

    private ServerSocket socketTCP2;

    // fila (FIFO) para as mensagens (thread safe)
    private final ConcurrentLinkedQueue fifo = new ConcurrentLinkedQueue();

    // lista de clientes conectados
    private final ArrayList<ClientData> clients = new ArrayList();

    // lista de salas com os endereços multicast de cada uma
    private ArrayList<Room> rooms = null;

    ChatConfig config = ChatConfig.getInstance();

    public Server() throws ParserConfigurationException, SAXException, IOException {

        rooms = ChatConfig.getRooms();

        // criando os sockets
        socketTCP1 = new ServerSocket(ChatConfig.getTcpPort1());
        socketTCP2 = new ServerSocket(ChatConfig.getTcpPort2());

        // função que cria as threads que tratarão as conexões
        mainLoop();
    }

    private InetAddress getInetAddressFromNickname(String nickname) {

        ClientData cli = null;

        for (int i = 0; i < clients.size(); i++) {
            cli = clients.get(i);
            if (cli.getNickname().equals(nickname)) {
                break;
            }
        }

        System.out.println(cli.getInetAddress().getHostAddress());
        return cli.getInetAddress();
    }

    private void mainLoop() throws IOException {

        // thread que vai receber todos os datagramas enviados pelos clientes
        Runnable udpReceiver;
        udpReceiver = () -> {
            System.out.println("Receiver UDP iniciado.");

            while (true) {
                try {

                    // o socket UDP ficará aguardando os mensagens:
                    // MESSAGE:NICKNAME_DESTINO:NICKNAME_ORIGEM:TEXTO
                    // ou
                    // NICKNAME_DESTINO:NICKNAME_ORIGEM:FILE:NOME_ARQUIVO:TAMANHO_KB
                    DatagramSocket socketUDP = new DatagramSocket(ChatConfig.getUdpPort());

                    byte[] receiveData = new byte[ChatConfig.getMessageMaxLength()];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socketUDP.receive(receivePacket);

                    String msg = new String(receivePacket.getData());

                    fifo.add(msg);

                    socketUDP.close();

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
            System.out.println("Sender UDP iniciado.");
            while (true) {
                // verifica se existem mensagens na fila e as envia
                if (!fifo.isEmpty()) {
                    try {
                        DatagramSocket socketUDP = new DatagramSocket();

                        byte[] sendData = new byte[ChatConfig.getMessageMaxLength()];
                        String msg = (String) fifo.poll();

                        String[] split = msg.split(":");

                        int clientUdpPort = 0;
                        InetAddress inetAddress = null;

                        for (ClientData c : clients) {
                            if (c.getNickname().equals(split[0])) {
                                clientUdpPort = c.getUpdPort();
                                inetAddress = c.getInetAddress();
                            }
                        }
                        
                        

                        if (split[2].startsWith("FILE")) {

                            msg = msg.replace(split[0] + ":", "");

                        } else {

                            msg = split[1] + ":" + split[2];

                        }

                        sendData = msg.getBytes("UTF-8");

                        DatagramPacket sendPacket
                                = new DatagramPacket(
                                        sendData,
                                        sendData.length,
                                        inetAddress,
                                        clientUdpPort);

                        socketUDP.send(sendPacket);

                        socketUDP.close();
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
        // LIST:SALA:IP_MULTICAST:USUARIO1:USUARIO2:...
        Runnable multicastServer;
        multicastServer = () -> {
            System.out.println("Servidor Multicast iniciado.");

            String list;
            ArrayList<String> lists = new ArrayList();

            while (true) {
                for (Room room : rooms) {

                    list = "LISTA:"
                            + room.getName()
                            + ":"
                            + room.getIpMulticast()
                            + ":";

                    for (ClientData cli : clients) {
                        if (room.getName().equals(cli.getRoom())) {
                            list += cli.getNickname() + ":";
                        }
                    }
                    //System.out.println(list);
                    lists.add(list);
                }

                InetAddress inetAddress;
                MulticastSocket socketMulticast;
                DatagramPacket dtgrm;

                for (int i = 0; i < lists.size(); i++) {
                    String[] split = lists.get(i).split(":");
                    try {
                        inetAddress = InetAddress.getByName(split[2]);

                        socketMulticast = new MulticastSocket(ChatConfig.getMcastPort());
                        socketMulticast.joinGroup(inetAddress);

                        dtgrm = new DatagramPacket(
                                lists.get(i).getBytes("UTF-8"),
                                lists.get(i).length(),
                                inetAddress,
                                ChatConfig.getMcastPort());

                        socketMulticast.send(dtgrm);

                    } catch (UnknownHostException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

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

        // ao aceitar uma conexão, o servidor cria uma thread que vai
        // enviar a lista de salas e depois fechar a conexão
        Runnable sendRooms;
        sendRooms = () -> {
            while (true) {
                Socket clientSocket = null;

                try {
                    clientSocket = socketTCP1.accept();

                    if (clientSocket.isConnected()) {

                        try {
                            ObjectOutputStream outputStream;
                            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                            outputStream.flush();
                            outputStream.writeObject(rooms);

                            outputStream.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        Thread tSendRooms = new Thread(sendRooms);
        tSendRooms.start();

        // thread que escuta o socket TCP que será usado para receber os
        // comandos e transferir arquivos (ou não - talvez um só pra isso)
        Runnable tcpServer;
        tcpServer = () -> {
            System.out.println("Servidor TCP inciado.");

            String command = null;
            ObjectInputStream inputStream = null;
            String[] split = null;

            while (true) {
                // o socket TCP ficará aguardando os comandos via string:
                // CONNECT:NICKNAME:SALA - compando para conectar o chat
                // DISCONNECT:NICKNAME - desconectar todos os sockets
                // FILE:
                Socket clientSocket = null;

                try {
                    clientSocket = socketTCP2.accept();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

                command = null;
                if (clientSocket.isConnected() || !clientSocket.isClosed()) {

                    try {
                        inputStream = new ObjectInputStream(clientSocket.getInputStream());
                        command = (String) inputStream.readObject();
                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if ((command != null) && command.startsWith("CONNECT:")) {

                        split = command.split(":");

                        ClientData cli = new ClientData(
                                split[1],
                                clientSocket,
                                clientSocket.getInetAddress()
                        );

                        cli.setRoom(split[2]);

                        for (Room room : rooms) {
                            if (room.getName().equals(split[2])) {
                                cli.setIpMulticast(room.getIpMulticast());
                            }
                        }

                        clients.add(cli);
                    }

                    if ((command != null) && command.startsWith("DISCONNECT:")) {
                        String nickname = command.split(":")[1];

                        for (ClientData client : clients) {

                            if (client.getNickname().equals(nickname)) {
                                try {
                                    client.getSocketTCP().close();
                                    inputStream.close();
                                    clients.remove(client);
                                    break;
                                } catch (IOException ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }

                    }

                    if ((command != null) && command.startsWith("UDP-PORT:")) {
                        split = command.split(":");

                        for (ClientData client : clients) {
                            if (client.getNickname().equals(split[1])) {
                                client.setUpdPort(Integer.parseInt(split[2]));
                                break;
                            }
                        }
                    }

                    if ((command != null) && command.startsWith("TCP-SERVER-PORT:")) {
                        split = command.split(":");

                        for (ClientData client : clients) {
                            if (client.getNickname().equals(split[1])) {
                                client.setTcpPort(Integer.parseInt(split[2]));
                                break;
                            }
                        }
                    }

                    System.out.println(command);
                }
            }
        };

        Thread threadTcpServer = new Thread(tcpServer);
        threadTcpServer.start();
    }

    public static void main(String argv[]) throws Exception {
        Server serv = new Server();
    }
}

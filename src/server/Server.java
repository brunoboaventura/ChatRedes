package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    // portas do chat (serão lidas do Chat.properties)
    private static int tcpPort;
    private static int udpPort;
    private static int mcastPort;

    // fila (FIFO) para as mensagens (thread safe)
    private final ConcurrentLinkedQueue fifo = new ConcurrentLinkedQueue();

    // lista de clientes conectados
    private final ArrayList clients = new ArrayList();

    // lista de salas com os endereços multicast de cada uma
    private ArrayList rooms = new ArrayList();

    // socket TCP principal, onde o cliente se conecta
    // por esse socket o cliente receberá as salas disponíveis e
    // enviará os pedidos TCP:
    private ServerSocket socketTCP;

    public Server() throws IOException {
        setPorts();

        // criando as salas
        rooms.add(new Room("sala 11", "127.0.0.1"));
        rooms.add(new Room("sala 22", "127.0.0.1"));
        rooms.add(new Room("sala 33", "127.0.0.1"));
        rooms.add(new Room("sala 44", "127.0.0.1"));

        // criando o socket
        socketTCP = new ServerSocket(tcpPort);
        
        // executando o loop principal, que vai escutar a porta TCP e devolver
        // a lista de salas para o usuário, bem como criar as threads para o
        // tratamento das mensagens (conexao e desconexao) e envio de arquivos
        mainLoop();

    }

    // ler as salas do arquivo rooms.xml
    private void setRooms() {

    }

    // funcao para ler as portas do arquivo Chat.properties
    private void setPorts() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/config/Chat.properties"));

        tcpPort = Integer.parseInt(props.getProperty("chat.port.tcp"));
        udpPort = Integer.parseInt(props.getProperty("chat.port.udp"));
        mcastPort = Integer.parseInt(props.getProperty("chat.port.mcast"));
    }

    private void mainLoop() throws IOException {
        // servidor fica em loop aguardando conexões
        while (true) {

            Socket cliente = socketTCP.accept();

            // ao aceitar uma conexão, o servidor cria uma thread que vai
            // enviar a lista de salas 
            Runnable sendRooms;
            sendRooms = () -> {
                ObjectOutputStream outputStream;
                try {
                    outputStream = new ObjectOutputStream(cliente.getOutputStream());
                    outputStream.flush();
                    outputStream.writeObject(rooms);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

            };

            Thread tSendRooms = new Thread(sendRooms);
            tSendRooms.start();
                

                

//                ObjectInputStream entrada = null;
//                try {
//                    entrada = new ObjectInputStream(cliente.getInputStream());
//                    String nickname = (String) entrada.readObject();
//
//                    ClientData cli = new ClientData(
//                            nickname,
//                            cliente,
//                            cliente.getInetAddress().getHostAddress()
//                    );
//
//                    clients.add(cli);
//
//                    System.out.println(nickname);
//
//                } catch (IOException | ClassNotFoundException ex) {
//                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
//                }

        }

    }

    public static void main(String argv[]) throws Exception {
        Server serv = new Server();
    }
}

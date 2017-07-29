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
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

    private static final int PORTA_TCP = 5000;
    private static final int PORTA_TCPF = 5001;
    private static final int PORTA_UDP = 5002;
    private static final int PORTA_MCAST = 5003;

    public static void main(String argv[]) throws Exception {

        // fila (FIFO) para as mensagens (thread safe)
        ConcurrentLinkedQueue fifo = new ConcurrentLinkedQueue();

        // Lista de clientes conectados
        ArrayList clients = new ArrayList();

        // Lista de salas com os endereços multicast de cada uma
        ArrayList rooms = new ArrayList();

        // Criando as salas
        rooms.add(new Room("sala 11", "127.0.0.1"));
        rooms.add(new Room("sala 22", "127.0.0.1"));
        rooms.add(new Room("sala 33", "127.0.0.1"));
        rooms.add(new Room("sala 44", "127.0.0.1"));

        // socket TCP principal, onde o cliente se conecta
        // por esse socket o cliente receberá as salas disponíveis e
        // enviará os pedidos TCP:
       
        ServerSocket socketTCP;
        socketTCP = new ServerSocket(PORTA_TCP);

        // servidor fica em loop aguardando conexões
        while (true) {

            Socket cliente = socketTCP.accept();

            // ao aceitar uma conexão, o servidor cria uma thread que vai
            // enviar a lista de salas 
            Runnable tratarCliente;
            tratarCliente = () -> {
                ObjectOutputStream saida;
                try {
                    saida = new ObjectOutputStream(cliente.getOutputStream());
                    saida.flush();
                    saida.writeObject(rooms);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);

                }

                ObjectInputStream entrada = null;
                try {
                    entrada = new ObjectInputStream(cliente.getInputStream());
                    String nickname = (String) entrada.readObject();

                    ClientData cli = new ClientData(nickname, cliente, cliente.getInetAddress().getHostAddress(), 5002, 5003);

                    clients.add(cli);
                    
                    System.out.println (nickname);

                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

            };

            Thread t = new Thread(tratarCliente);
            t.start();
        }

//        Runnable enviaMensagens;
//        enviaMensagens = () -> {
//            if (!pilha.isEmpty()) {
//                System.out.println(pilha.poll());
//            }
//        };
//
//        Thread t = new Thread(enviaMensagens);
//        t.start();
//
//        Runnable enviaSalas;
//        enviaSalas = () -> {
//            try {
//                String salas = "sala1:224.225.226.227;sala2:224.225.226.228";
//                byte[] b = salas.getBytes();
//                InetAddress addr = InetAddress.getByName("224.225.226.227");
//                DatagramSocket ds = new DatagramSocket();
//                DatagramPacket pkg = new DatagramPacket(b, b.length, addr, PORTA_MCAST);
//                ds.send(pkg);
//            } catch (Exception e) {
//                System.out.println("Nao foi possivel enviar a mensagem");
//            }
//        };
    }

}

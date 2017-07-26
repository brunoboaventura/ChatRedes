package chatredes;

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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor {

    private static final int PORTA_TCP = 5000;
    private static final int PORTA_TCPF = 5001;
    private static final int PORTA_UDP = 5002;
    private static final int PORTA_MCAST = 5003;

    public static void main(String argv[]) throws Exception {

        // pilha para as mensagens
        ArrayBlockingQueue pilha = new ArrayBlockingQueue(5);

        // Lista de clientes conectados
        ArrayList clientes = new ArrayList();

        // Lista de salas com os endereços multicast de cada uma
        ArrayList salas = new ArrayList();

        salas.add(new Sala("sala 1", "127.0.0.1"));
        salas.add(new Sala("sala 2", "127.0.0.1"));
        salas.add(new Sala("sala 3", "127.0.0.1"));

        // criando o receptor de mensagens UDP
        Runnable receptorUDP;
        receptorUDP = () -> {

            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket(PORTA_UDP);

            } catch (SocketException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
//            byte[] msg = new byte[10];
//
//            DatagramPacket pkg = new DatagramPacket(msg, msg.length);
//
//            try {
//                ds.receive(pkg);
//            } catch (IOException ex) {
//                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
//            }rootPaneCheckingEnabled
//
//            System.out.println(pkg.getData().toString());
            ds.close();
        };

        Thread t = new Thread(receptorUDP);
        t.start();

        // socket TCP principal, onde o cliente se conecta
        ServerSocket socketTCP;
        socketTCP = new ServerSocket(PORTA_TCP);

        while (true) {

            Socket cliente = socketTCP.accept();

            Runnable tratarCliente;
            tratarCliente = () -> {
                System.out.println("Nova conexão com o cliente "
                        + cliente.getInetAddress().getHostAddress());

                ObjectOutputStream saida = null;
                try {
                    saida = new ObjectOutputStream(cliente.getOutputStream());
                    saida.flush();
                    saida.writeObject(salas);
                } catch (IOException ex) {
                    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);

                }

                ObjectInputStream entrada = null;
                try {
                    entrada = new ObjectInputStream(cliente.getInputStream());
                    String nickname = (String) entrada.readObject();

                    ClienteData cli = new ClienteData(nickname, cliente, cliente.getInetAddress().getHostAddress(), 5002, 5003);

                    clientes.add(cli);
                    
                    System.out.println (nickname);

                } catch (IOException ex) {
                    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                }

            };

            t = new Thread(tratarCliente);
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

package server;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class ClientData implements Serializable {

    private String nickname;
    private Socket socketTCP;
    private String ip;
   
    public ClientData (String n, Socket s, String ip) {
        this.nickname = n;
        this.socketTCP = s;
        this.ip = ip;
    }
}

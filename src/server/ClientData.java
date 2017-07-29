package server;

import java.io.Serializable;
import java.net.Socket;

public class ClientData implements Serializable {

    private final String nickname;
    private final Socket socketTCP;
    private final String ip;
   
    public ClientData (String n, Socket s, String ip) {
        this.nickname = n;
        this.socketTCP = s;
        this.ip = ip;
    }

    public String getNickname() {
        return nickname;
    }

    public Socket getSocketTCP() {
        return socketTCP;
    }

    public String getIp() {
        return ip;
    }    
    
}

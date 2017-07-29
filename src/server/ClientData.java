package server;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class ClientData implements Serializable {

    private String nickname;
    private Socket socket;
    private String ip;
    private int porta_udp;
    private int porta_mcast;
   
    public ClientData (String n, Socket s, String ip, int pudp, int pmcast){
        this.nickname = n;
        this.socket = s;
        this.ip = ip;
        this.porta_udp = pudp;
        this.porta_mcast = pmcast;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPorta_udp() {
        return porta_udp;
    }

    public void setPorta_udp(int porta_udp) {
        this.porta_udp = porta_udp;
    }

    public int getPorta_mcast() {
        return porta_mcast;
    }

    public void setPorta_mcast(int porta_mcast) {
        this.porta_mcast = porta_mcast;
    }
    
    
}

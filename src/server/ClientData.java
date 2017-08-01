package server;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class ClientData implements Serializable {

    private String nickname;
    private Socket socketTCP;
    private InetAddress inetAddress;
    private String sala;
    private String ipMulticast;
    private int updPort;
   
    public ClientData (String nickname, Socket socket, InetAddress inetAddress) {
        this.nickname = nickname;
        this.socketTCP = socket;
        this.inetAddress = inetAddress;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Socket getSocketTCP() {
        return socketTCP;
    }

    public void setSocketTCP(Socket socketTCP) {
        this.socketTCP = socketTCP;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getIpMulticast() {
        return ipMulticast;
    }

    public void setIpMulticast(String ipMulticast) {
        this.ipMulticast = ipMulticast;
    }

    public int getUpdPort() {
        return updPort;
    }

    public void setUpdPort(int updPort) {
        this.updPort = updPort;
    }

}

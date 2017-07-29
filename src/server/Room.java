package server;

import java.io.Serializable;

public class Room implements Serializable {

    private String nome;
    private String ip_multicast;

    public Room(String nome, String ip) {
        this.nome = nome;
        this.ip_multicast = ip;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIp_multicast() {
        return ip_multicast;
    }

    public void setIp_multicast(String ip_multicast) {
        this.ip_multicast = ip_multicast;
    }

}

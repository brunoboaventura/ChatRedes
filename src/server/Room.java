package server;

import java.io.Serializable;

public class Room implements Serializable {

    private String name;
    private String ipMulticast;

    public Room(String name, String ip) {
        this.name = name;
        this.ipMulticast = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpMulticast() {
        return ipMulticast;
    }

    public void setIpMulticast(String ipMulticast) {
        this.ipMulticast = ipMulticast;
    }

}

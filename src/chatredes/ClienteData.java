package chatredes;

import java.net.InetAddress;
import java.net.Socket;

public class ClienteData {

    String nickname;
    Socket socket;
    String ip;
    int porta_udp;
    int porta_mcast;
   
    ClienteData (String n, Socket s, String ip, int pudp, int pmcast){
        this.nickname = n;
        this.socket = s;
        this.ip = ip;
        this.porta_udp = pudp;
        this.porta_mcast = pmcast;
    }
}

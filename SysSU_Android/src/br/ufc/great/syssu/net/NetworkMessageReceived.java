package br.ufc.great.syssu.net;

import java.net.InetAddress;

public class NetworkMessageReceived {
    private InetAddress inetAddres;
    private String adress;
    private String message;
    private int port;

    public NetworkMessageReceived(InetAddress inetAddres, int port, String message) {
        this.inetAddres = inetAddres;
        this.message = message;
        this.port = port;
    }
    
    public NetworkMessageReceived(String adress, String message) {
        this.adress = adress;
        this.message = message;
        this.inetAddres = null;
        this.port = 0;
    }

    public InetAddress getInetAddres() {
        return this.inetAddres;
    }

    public String getMessage() {
        return this.message;
    }

    public int getPort() {
        return this.port;
    }
}


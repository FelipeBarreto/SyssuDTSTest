package br.ufc.great.syssu.net;

import java.io.IOException;

import br.ufc.great.syssu.net.interfaces.INetworkObserver;
import br.ufc.great.syssu.net.interfaces.INetworkServer;

public class NetworkServer implements INetworkServer {

	private int port;
	private TCPNetwokServer tcpNetwokServer;

	public NetworkServer(int port) throws IOException {
		this.port = port;
		this.tcpNetwokServer = new TCPNetwokServer(port);
	}

	public boolean isStopped() {
		return this.tcpNetwokServer.isStopped();
	}

	public int getPort() {
		return this.tcpNetwokServer.getPort();
	}

	@Override
	public void setNetworkObserver(INetworkObserver observer) {
		this.tcpNetwokServer.setNetworkObserver(observer);
	}

	@Override
	public INetworkObserver getNetworkObserver() {
		return this.tcpNetwokServer.getNetworkObserver();
	}

	@Override
	public void start() throws IOException {
		this.tcpNetwokServer.start();
	}

	@Override
	public void stop() throws IOException{
		this.tcpNetwokServer.stop();
	}

}


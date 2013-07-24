package br.ufc.great.syssu.ubibroker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import br.ufc.great.syssu.base.Provider;
import br.ufc.great.syssu.base.TupleSpaceException;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.base.interfaces.IReaction;
import br.ufc.great.syssu.base.utils.MapTuple;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Message;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Response;
import br.ufc.great.syssu.net.NetworkClient;
import br.ufc.great.syssu.net.NetworkMessageReceived;
import br.ufc.great.syssu.net.NetworkServer;
import br.ufc.great.syssu.net.interfaces.INetworkObserver;

public class UbiBroker implements INetworkObserver, Runnable {

	private NetworkClient networkClient;
	private NetworkServer networkServer;
	private List<IReaction> reactions;
	private int reactionsPort;

	private UbiBroker(String ubicentreAddress, int ubiCentrePort, int reactionsPort, Context context, String requesterAddress) throws IOException {
		this.networkClient = new NetworkClient(ubicentreAddress, ubiCentrePort, context, requesterAddress);
		this.networkServer = new NetworkServer(reactionsPort);
		this.reactions = new ArrayList<IReaction>();
		this.reactionsPort = reactionsPort;
	}

	public static UbiBroker createUbibroker(String ubicentreAddress, int ubiCentrePort, int reactionsPort) 
			throws IOException {
		return UbiBroker.createUbibroker(ubicentreAddress, ubiCentrePort, reactionsPort, null, null);
	}

	public static UbiBroker createUbibroker(String ubicentreAddress, int ubiCentrePort, int reactionsPort, Context context, String requesterAddress)
			throws IOException {
		UbiBroker instance = new UbiBroker(ubicentreAddress, ubiCentrePort, reactionsPort, context, requesterAddress);
		new Thread(instance).start();
		return instance;
	}

	public IDomain getDomain(String name) throws TupleSpaceException {
		return new Domain(name, this);
	}
	
	public IDomain getDomain(String name, Provider provider) throws TupleSpaceException {
		return new Domain(name, this, provider);
	}

	int getReactionsPort() {
		return reactionsPort;
	}

//	String sendMessage(String message) throws IOException {
//		return networkClient.sendMessage(message);
//	}

	String sendMessage(String message, Provider provider) throws IOException {
		String resultMessage = "";

		switch (provider) {
		case INFRA:
			resultMessage = networkClient.sendMessage(message);
			break;
		case ADHOC:
			resultMessage = networkClient.sendMessage(message, "BLUETOOTH");
			break;
		default:
			break;
		}
		return resultMessage;
	}

	void addReaction(IReaction reaction) throws TupleSpaceException {
		reactions.add(reaction);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String process(NetworkMessageReceived message) {
		JSONRPC2Message msn = null;

		//TODO: implementar a forma de receber as reactions dos outros providers ADHOC e LOCAL

		try {
			msn = JSONRPC2Message.parse(message.getMessage());
			if (msn instanceof JSONRPC2Response) {
				JSONRPC2Response response = (JSONRPC2Response) msn;
				if (response.indicatesSuccess()) {
					Object res = response.getResult();
					if (res instanceof Map) {
						for (IReaction reaction : reactions) {
							if (response.getID().equals(reaction.getId())) {
								reaction.react(new MapTuple((Map<String, Object>) res).getObject());
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}

		return null;
	}

	@Override
	public void run() {
		try {
			networkServer.setNetworkObserver(this);
			networkServer.start();
		} catch (IOException ex) {
		}
	}

}

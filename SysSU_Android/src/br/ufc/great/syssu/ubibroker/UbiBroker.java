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
import br.ufc.great.syssu.jsonrpc2.JSONRPC2InvalidMessageException;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Message;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2ParseException;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Response;
import br.ufc.great.syssu.net.InfraNetworkFinder;
import br.ufc.great.syssu.net.NetworkClient;
import br.ufc.great.syssu.net.NetworkMessageReceived;
import br.ufc.great.syssu.net.NetworkServer;
import br.ufc.great.syssu.net.interfaces.INetworkObserver;

public class UbiBroker implements INetworkObserver, Runnable {

	private NetworkClient networkClient;
	private NetworkServer networkServer;
	private List<IReaction> reactions;

	private static String ubicentreAddress;
	private static int ubiCentrePort;
	private static int reactionsPort;
	private Context context;
	private Provider provider;

	private UbiBroker(String ubicentreAddress, int ubiCentrePort, int reactionsPort, Context context, Provider provider) throws IOException {

		if (provider == Provider.ADHOC){
			this.networkClient = new NetworkClient(context, provider);
		} else {
			this.networkClient = new NetworkClient(ubicentreAddress, ubiCentrePort, context, provider);
		}
		this.networkServer = new NetworkServer(reactionsPort);

		UbiBroker.ubicentreAddress = ubicentreAddress;
		UbiBroker.ubiCentrePort = ubiCentrePort;
		UbiBroker.reactionsPort = reactionsPort;

		this.context = context;
		this.provider = provider;

		this.reactions = new ArrayList<IReaction>();

	}

	public static UbiBroker createUbibroker(String ubicentreAddress, int ubiCentrePort, int reactionsPort, Context context, Provider provider)
			throws IOException {
		UbiBroker instance = new UbiBroker(ubicentreAddress, ubiCentrePort, reactionsPort, context, provider);
		new Thread(instance).start();
		return instance;
	}

	public static UbiBroker createUbibroker(String ubicentreAddress, int ubiCentrePort, int reactionsPort)
			throws IOException {
		return UbiBroker.createUbibroker(ubicentreAddress, ubiCentrePort, reactionsPort, null, Provider.INFRA);
	}


	public static UbiBroker createUbibroker(Context context, Provider provider)
			throws IOException {
		return UbiBroker.createUbibroker(ubicentreAddress, ubiCentrePort, reactionsPort, context, provider);
	}

	public IDomain getDomain(String name) throws TupleSpaceException {
		return new Domain(name, this, Provider.INFRA);
	}

	public IDomain getDomain(String name, Provider provider) throws TupleSpaceException {
		return new Domain(name, this, provider);
	}

	int getReactionsPort() {
		return reactionsPort;
	}

	String sendMessage(String message) throws IOException {
		return sendMessage(message, Provider.INFRA);
	}

	String sendMessage(String message, Provider provider) throws IOException {
		String resultMessage = "NO NETWORK CONNECTION";

		if(networkClient != null){
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
		} catch (JSONRPC2ParseException e) {
			e.printStackTrace();
		} catch (JSONRPC2InvalidMessageException e) {
			e.printStackTrace();
		}
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
		return null;
	}

	public void updateInfraConnection() throws IOException{

		if(ubicentreAddress == null || ubicentreAddress == ""){
			InfraNetworkFinder infraNetworkFinder = new InfraNetworkFinder(context);
			ubicentreAddress = infraNetworkFinder.getUbicentreAddress();
		}
		this.networkClient = new NetworkClient(ubicentreAddress, ubiCentrePort, context, provider);
	}

	public boolean hasAdHocConnection() {
		return this.networkClient.hasAdHocConnection();
	}

	public boolean hasInfraConnection() {
		return this.networkClient.hasServerConnection();
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

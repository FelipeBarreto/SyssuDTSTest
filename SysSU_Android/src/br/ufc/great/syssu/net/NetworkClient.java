package br.ufc.great.syssu.net;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import br.ufc.great.somc.networklayer.base.NetworkManager;
import br.ufc.great.syssu.base.Provider;

public class NetworkClient {

	private String address;
	private int port;
	private Context context;
	private Provider provider;

	private TCPNetworkClient tcpClient;
	public NetworkManager networkManager;

	public NetworkClient(String address, int port) {
		new NetworkClient(address, port, context, null);
	}

	public NetworkClient(Context context, Provider provider) {
		this.context = context;
		this.provider = provider;
		if (provider == Provider.ADHOC && context != null)
			this.networkManager = AdhocNetworkManager.getNetworkManagerInstance(context);
	}

	public NetworkClient(String address, int port, Context context, Provider provider) {
		this.address = address;
		this.port = port;
		this.context = context;
		this.provider = provider;

		if (provider == Provider.ADHOC && context != null) {
			this.networkManager = AdhocNetworkManager.getNetworkManagerInstance(context);
		} else if (provider == Provider.INFRA){
			this.tcpClient = new TCPNetworkClient(address, port);
		}
	}

	public String sendMessage(String message) throws IOException { 
		String result = "NO SERVER CONNECTION";
		if(tcpClient.hasConnection()){
			result = this.tcpClient.sendMessage(message);
		}
		return result;
	}

	public String sendMessage(String message, String adhocNet) throws IOException { 

		String result = "NO ADHOC CONNECTION";

		if (networkManager == null){
			if (context != null){
				networkManager = AdhocNetworkManager.getNetworkManagerInstance(context);
			}else {
				return result;
			}
		}
		
		// Bluetooth client
		if (adhocNet.equalsIgnoreCase("bluetooth")) {
			try {
				System.out.println(">>> NetworkManager CurrentState " + networkManager.getCurrentState());

				if(networkManager.getCurrentState() == NetworkManager.STATE_CONNECTED){

					int qtyDevices = AdhocNetworkManager.tsMonitor.getAvailableDevice().keySet().size(); 
					//							+ AdhocNetworkManager.tsMonitor.getNotNeighborDevices().keySet().size();
					System.out.println(">>> qtyDevices " + qtyDevices);

					networkManager.sendBroadcastMessage(new JSONObject(message)); //.put("requesterAddress", this.requesterAddress));
					//networkManager.sendMessage(new JSONObject(message), this.address);

					boolean timeout = !AdhocNetworkManager.semaphore.tryAcquire(qtyDevices, qtyDevices * 6, TimeUnit.SECONDS);

					if(timeout) 
						Log.i("ad", "TIMEOUT");

					if(!AdhocNetworkManager.responseList.isEmpty())
					{
						result = AdhocNetworkManager.responseList.get(0);
						for (int i = 1; i < AdhocNetworkManager.responseList.size(); i++) {
							result = AdhocNetworkManager.concatRespose(result, AdhocNetworkManager.responseList.get(i));
						}
					}

					System.out.println(">>> reponse list size "  + AdhocNetworkManager.responseList.size());
					Toast.makeText(this.context,"Send Bluetooth Message to " 
							+ AdhocNetworkManager.responseList.size() + " devices.", Toast.LENGTH_SHORT).show();
					System.out.println(">>> send bluetooth Message" + message);
					System.out.println(">>> get bluetooth response" + result);

					AdhocNetworkManager.responseList.clear();
				}else{
					Toast.makeText(this.context,"Sem conexão", Toast.LENGTH_SHORT).show();
					System.out.println(">>> Sem conexão");
					result = "BLUETOOTH NOT CONNECTED";
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public boolean hasServerConnection(){
		return (tcpClient != null) && tcpClient.hasConnection();
	}

	public boolean hasAdHocConnection(){
		return networkManager != null;
	}

}

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

public class NetworkClient {

	private String address;
	private int port;
	private Context context = null;
	private String requesterAddress = null;
	private TCPNetworkClient tcpClient = null;
	private NetworkManager networkManager;

	public NetworkClient(String address, int port) {
		this.address = address;
		this.port = port;
		this.tcpClient = new TCPNetworkClient(address, port);
	}

	public NetworkClient(String address, int port, Context context, String requesterAddress) {
		this.address = address;
		this.port = port;
		this.context = context;
		this.requesterAddress = requesterAddress; // checar se eh null
		if (!(context == null)) {
			this.networkManager = AdhocNetworkManager.getNetworkManagerInstance(context);
		} else {
			this.tcpClient = new TCPNetworkClient(address, port);
		}
	}

	public String sendMessage(String message) throws IOException { 
		return this.tcpClient.sendMessage(message);
	}

	public String sendMessage(String message, String adhocNet) throws IOException { 

		String result = null;
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

					boolean timeout = !AdhocNetworkManager.semaphore.tryAcquire(qtyDevices, qtyDevices * 3, TimeUnit.SECONDS);

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
					result = " {\"erro\":\"erro -> BLUETOOTH NOT CONNECTED\",\"id\":0,\"jsonrpc\":\"2.0\"}";
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result;
	}


}

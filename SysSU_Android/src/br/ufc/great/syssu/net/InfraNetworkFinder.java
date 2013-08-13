package br.ufc.great.syssu.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

public class InfraNetworkFinder {

	private NetworkClient networkClient;
	private NetworkServer networkServer;
	private int DISCOVERY_PORT = 2020;

	private String ubicentreAddress;
	public final static int UBICENTRE_PORT = 9090;
	public final static int REACTIONS_PORT = 9091;

	private Context context;

	public InfraNetworkFinder (Context context) {
		this.context = context;
		this.ubicentreAddress = null;
	}

	public NetworkClient getTcpNetworkClient() {
		String ubiCentreAddress = getUbicentreAddress();

		if (ubiCentreAddress != null && ubiCentreAddress != ""){
			this.networkClient = new NetworkClient(ubiCentreAddress, UBICENTRE_PORT);
		}
		
		return networkClient;
	}

	public NetworkServer getTcpNetworkServer() throws IOException {
		this.networkServer = new NetworkServer(REACTIONS_PORT);
		return networkServer;		
	}

	public int getReactionsPort() {
		return REACTIONS_PORT;
	}

	public String getUbicentreAddress() {
		String ubiCentreIp = null;
		
		// The method execute returns the AynscTask itself, so we need to call get(). 
		// This turned async task into a sync one, as get() waits if needed for the result to be avilable.
		try {
			if(context != null) {
				ubiCentreIp = new RetreiveUbicentreIP().execute(context).get();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		ubicentreAddress = ubiCentreIp;

		return ubicentreAddress;
	}

	// Applications targeting the Honeycomb SDK or higher throws NetworkOnMainThreadException 
	// when an application attempts to perform a networking operation on its main thread.
	private class RetreiveUbicentreIP extends AsyncTask<Context, Void, String> {

		private DatagramSocket mDatagramSocket;
		String ubicentreIP = null;

		@Override
		protected String doInBackground(Context... ctx) {
			try {
				mDatagramSocket = new DatagramSocket();
				mDatagramSocket.setBroadcast(true);

				// Send UDP broadcast message
				String sendMessage = "HAS SYSSU???";
				DatagramPacket packet = new DatagramPacket(sendMessage.getBytes(), sendMessage.length(), getBroadcastAddress(ctx[0]), DISCOVERY_PORT);
				mDatagramSocket.send(packet);

				long initialTime = System.currentTimeMillis();
				long timeout = 1000;

				DatagramPacket receivePacket = new DatagramPacket(sendMessage.getBytes(), sendMessage.length());
				do{
					// Receive UDP broadcast message
					mDatagramSocket.setSoTimeout(500);
					mDatagramSocket.receive(receivePacket);
					String receiveMessage = new String(receivePacket.getData(), 0,receivePacket.getLength());

					if (receiveMessage.equalsIgnoreCase("HAS SYSSU!!!")) {
						ubicentreIP = receivePacket.getAddress().getHostAddress();
						mDatagramSocket.close();
						break;
					}	
				}while (System.currentTimeMillis() - initialTime <= timeout);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return ubicentreIP;		
		}

		private InetAddress getBroadcastAddress(Context ctx) throws Exception {
			if (ctx != null) {

				WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
				DhcpInfo dhcp = wifi.getDhcpInfo();

				int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
				byte[] quads = new byte[4];

				for (int k = 0; k < 4; k++)
					quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

				return InetAddress.getByAddress(quads);

			}else {

				return getJavaBroadcastAddress();

			}
		}

		@TargetApi(9)
		private InetAddress getJavaBroadcastAddress() throws Exception {

			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback())
					continue;    // Don't want to broadcast to the loopback interface
				for (InterfaceAddress interfaceAddress :
					networkInterface.getInterfaceAddresses()) {

					InetAddress broadcast = interfaceAddress.getBroadcast();

					if (broadcast == null)
						continue;
					System.out.println("broadcast ip = " + broadcast);
					return broadcast;
				}
			}
			return null;
		}
	}
}
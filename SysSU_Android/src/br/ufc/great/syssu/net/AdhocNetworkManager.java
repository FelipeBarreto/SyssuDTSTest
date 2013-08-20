package br.ufc.great.syssu.net;

import java.util.Vector;
import java.util.concurrent.Semaphore;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.RemoteException;
import br.ufc.great.somc.networklayer.base.NetworkEventListener;
import br.ufc.great.somc.networklayer.base.NetworkManager;
import br.ufc.great.somc.networklayer.bluetooth.BluetoothEventsListener;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2InvalidMessageException;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Message;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2ParseException;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Request;
import br.ufc.great.syssu.jsonrpc2.JSONRPC2Response;
import br.ufc.great.syssu.ubicentre.controllers.FrontController;

public class AdhocNetworkManager {

	private static NetworkManager instance;
	private static Context context;
	public static TS_Monitor tsMonitor;
	public static Semaphore semaphore = new Semaphore(0);
	public static Vector<String> responseList = new Vector<String>();

	public static NetworkManager getNetworkManagerInstance(Context context){
		if (instance == null) {
			setContext(context);
			instance = new NetworkManager(context, messageListener);
			instance.onResume();
			instance.subscribeBluetoothOptionalEvents(bluetoothEventsListener);
			tsMonitor = new TS_Monitor();
		}
		return instance;
	}

	public static Context getContext() {
		return context;
	}

	private static void setContext(Context context) {
		AdhocNetworkManager.context = context;
	}

	public static String concatRespose(String oldResponse, String newResponse){
		String response = oldResponse;
		String tempResponse = newResponse;

		response = response.substring(response.indexOf("[") + 1, response.indexOf("]"));
		response = tempResponse.substring(0, tempResponse.indexOf("[") + 1).concat(response).concat(",").concat(tempResponse.substring(tempResponse.indexOf("[") + 1));
		
		return response;
	}

	private static NetworkEventListener messageListener = new NetworkEventListener() {

		@Override
		public void onStateConnectionChanged(int state, int previousState) {
			// TODO Auto-generated method stub
			System.out.println(">>>NetworkEventListener>>> ConnectionChanged from " + previousState + " to " + state);
		}

		@Override
		public synchronized void onReceiveMessage(String deviceName, String deviceAddress, JSONObject message) {
			System.out.println("\n\n >>>*** RECIVE MSG ***<<< \n from: " + deviceName + " msg: " + message.toString());

			JSONRPC2Message msn = null;
			String requesterAddress = deviceAddress;

			try {
				msn = JSONRPC2Message.parse(message.toString());
			} catch (JSONRPC2ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONRPC2InvalidMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Receive a request message
			if(msn instanceof JSONRPC2Request){
				System.out.println("instanceof JSONRPC2Request");
				try {
					msn = JSONRPC2Message.parse(message.toString());

					String response = "";
					response = FrontController.getInstance().process(new NetworkMessageReceived(requesterAddress, message.toString()));


					// Send a bluetooth direct menssage with a response to sender device Address
					System.out.println("\n\n >>>*** RECIVE MSG ***<<< \n Send reponse to " + requesterAddress + " response msg: " + response);
					AdhocNetworkManager.instance.sendMessage(new JSONObject(response), requesterAddress);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONRPC2ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONRPC2InvalidMessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			// Receive a response message
			}else  if (msn instanceof JSONRPC2Response) {
				System.out.println("instanceof JSONRPC2Response");
				// get response and release the request process
				responseList.add(msn.toString());
				semaphore.release();
			}
			System.out.println(">>>NetworkEventListener>>> onReceiveMessage >>>END");
		}

		@Override
		public void onNotNeighborFound(String macAddress) {
			// TODO Auto-generated method stub
			try {
				System.out.println(">>>NetworkEventListener>>> NotNeighborFound -> " + instance.getRemoteDevice(macAddress).getName());
				//tsMonitor.addNotNeighborDevices(instance.getRemoteDevice(macAddress).getName(), macAddress);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onExceptionOccurred(String message) {
			// TODO Auto-generated method stub
			System.out.println(">>>NetworkEventListener>>> ExceptionOccurred >> " + message);
		}

		@Override
		public void onDeviceFound(String name, String address) {
			// TODO Auto-generated method stub			
			System.out.println(">>>NetworkEventListener>>> DeviceFound >> " + name);
		}

		@Override
		public void onDeviceDisconnected(String deviceName, String deviceAddress) {
			// TODO Auto-generated method stub
			System.out.println(">>>NetworkEventListener>>> DeviceDisconnected >> " + deviceName);
			tsMonitor.removeAvailableDevice(deviceName);
		}

		@Override
		public void onDeviceConnected(String deviceName, String deviceAddress) {
			// TODO Auto-generated method stub
			System.out.println(">>>NetworkEventListener>>> DeviceConnected >>" + deviceName);
			tsMonitor.addAvailableDevice(deviceName, deviceAddress);
		}
	};

	private static BluetoothEventsListener bluetoothEventsListener = new BluetoothEventsListener() {

		@Override
		public void onStateScanModeChanged(int state, int previousState) {
			// TODO Auto-generated method stub
			System.out.println(">>>bluetoothEventsListener>>> onStateScanModeChanged from " + previousState + " to " + state);	
		}

		@Override
		public void onStateObservingChanged(int state, int currentTime) {
			// TODO Auto-generated method stub
			System.out.println(">>>bluetoothEventsListener>>> onStateObservingChanged to >> " + state);		
		}

		@Override
		public void onStateDiscoveryChanged(String action) {
			// TODO Auto-generated method stub
			System.out.println(">>>bluetoothEventsListener>>> onStateDiscoveryChanged >> action: " + action);		
		}

		@Override
		public void onStateChange(int state, int previousState) {
			// TODO Auto-generated method stub
			System.out.println(">>>bluetoothEventsListener>>> onStateChange from " + previousState + " to " + state);
		}

		@Override
		public void onNotNeighborDeviceFound(String deviceName, String deviceAddress) {
			// TODO Auto-generated method stub
			System.out.println(">>>bluetoothEventsListener>>> onNotNeighborDeviceFound for >> " + deviceName);
		}
	};
}

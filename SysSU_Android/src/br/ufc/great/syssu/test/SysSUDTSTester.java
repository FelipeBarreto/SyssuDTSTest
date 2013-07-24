package br.ufc.great.syssu.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import br.ufc.great.somc.networklayer.R;
import br.ufc.great.somc.networklayer.base.NetworkManager;
import br.ufc.great.syssu.base.Pattern;
import br.ufc.great.syssu.base.Provider;
import br.ufc.great.syssu.base.Scope;
import br.ufc.great.syssu.base.Tuple;
import br.ufc.great.syssu.base.TupleField;
import br.ufc.great.syssu.base.TupleSpaceException;
import br.ufc.great.syssu.base.TupleSpaceSecurityException;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.net.AdhocNetworkManager;
import br.ufc.great.syssu.ubibroker.GenericDomain;
import br.ufc.great.syssu.ubibroker.GenericUbiBroker;
import br.ufc.great.syssu.ubicentre.UbiCentreProcess;

public class SysSUDTSTester extends Activity {

	// Key names received from the BluetoothChatService Handler
	private final int REQUEST_CONNECT_DEVICE = 1;

	private static int localPort = 9090;
	private static GenericUbiBroker broker;
	private static GenericDomain	domain;

	private static final String scopeGreat = "scopeGreat";

	private TextView msgTextView;
	private NetworkManager networkManager;

	private String messageTest = "SYSU-DTS Rocks!!";

	private Scope hScope = (Scope) new Scope().addField("happyScope", "happy");
	private Scope sScope = (Scope) new Scope().addField("sadScope", "sad").addField("tag", "master");
	private Scope allScope = (Scope) new Scope().addField("happyScope", "happy").addField("sadScope", "sad").addField("tag", "master").addField("x", "y");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);

		assetmanager =  getAssets();

		System.out.println(">>> onCreate");

		msgTextView = (TextView) findViewById(R.id.txt_View);
		msgTextView.setText(messageTest);

		//start Ubicentre 
		try {
			Thread t = new Thread(new UbiCentreProcess(localPort), "UbiCentre Process");
			t.start();
		} catch (Exception ex) {
			System.exit(1);
		}

		try {
			networkManager = AdhocNetworkManager.getNetworkManagerInstance(getApplicationContext());

			// Create broker
			Context ctx = getApplicationContext();
			broker = GenericUbiBroker.createUbibroker(ctx);

			// Get a domain (tuple space subset)
			domain = (GenericDomain) broker.getDomain(scopeGreat);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TupleSpaceException e) {
			e.printStackTrace();
		}	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// Get the device MAC address
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Attempt to connect to the device
				try {
					networkManager.connect(address);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_syssudts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			msgTextView.append("\n ==================================");
			switch (item.getItemId()) {

			case R.id.menu_Put:
				testarPut(domain, 2, "local");
				testarPut(domain, 2, "adhoc");
				testarPut(domain, 2, "all");
				break;

			case R.id.menu_ReadLocal:
				testarRead(domain, " genericDomain - scopeGreat", Provider.LOCAL);
				break;
			case R.id.menu_ReadInfra:
				testarRead(domain, " genericDomain - scopeGreat", Provider.INFRA);
				break;
			case R.id.menu_ReadAdhoc:
				testarRead(domain, " genericDomain - scopeGreat", Provider.ADHOC);
				break;
			case R.id.menu_ReadAll:
				testarRead(domain, " genericDomain - scopeGreat", Provider.ALL);
				break;
			case R.id.menu_ReadAny:
				testarRead(domain, " genericDomain - scopeGreat", Provider.ANY);
				break;	
			case R.id.menu_Print:
				// Read all local Tuples	
				List<Tuple> tuples;
				tuples = domain.read((Pattern) new Pattern().addField("?", "?"), "", "", allScope, Provider.LOCAL);
				// Writes tuples in tupleSpace EditText
				printTuples(tuples, "Tuplas Locais");
				break;
			case R.id.menu_Discoverable:
				msgTextView.append("\n >>> CurrentState => " + networkManager.getCurrentState());
				if(networkManager.ensureDiscoverable() == true) {
					System.out.println(">>> ensureDiscoverable TRUE");
					msgTextView.append("\n >>> ensureDiscoverable TRUE");
				}else {
					System.out.println(">>> ensureDiscoverable FALSE");
					msgTextView.append("\n >>> ensureDiscoverable FALSE");
				}
				break;
			case R.id.menu_ConnectTo:
				System.out.println("CurrentState ==>>>> " + networkManager.getCurrentState());
				msgTextView.append("\n >>> CurrentState => " + networkManager.getCurrentState());

				// Launch the DeviceListActivity in order to see available devices and do scan for new ones
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				break;		
			case R.id.menu_PrintConnectedDevices:
				Map cd = AdhocNetworkManager.tsMonitor.getAvailableDevice();
				Set<String> dn = cd.keySet();
				for (String deviceName : dn) {
					msgTextView.append("\n >>> Connected Devices => " + deviceName);
				}
				break;	
			case R.id.menu_Clear:
				msgTextView.setText("--- SysSU-DTS ---");
				break;
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TupleSpaceException e) {
			e.printStackTrace();
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		} 
		return true;
	}

	public void testarPut(IDomain domain, int qtyTuples, String value) {
		try {
			msgTextView.append("\n -- Put Tuple -- ");
			System.out.println("\n -- testarPut -- \n");
			// Create tuples
			Tuple tuple = null;
			List<Tuple> tuples = new ArrayList<Tuple>();

			for (int i = 0; i < qtyTuples; i++) {
				tuple = (Tuple) new Tuple().addField("contextkey","context.ambient.temperature").
						addField("source", "physicalmobilesensor").
						addField("value",  18 + ( int ) ( Math.random() *3 )).
						addField("timestamp", System.currentTimeMillis()).
						addField("accurace", 0.8).
						addField("unit", "C").
						addField("cont", i+1).
						addField("provider", value);

				// Define Tuple Scope
				if (i % 2 == 0) {
					tuple.setScope(hScope);
				}
				else {
					tuple.setScope(sScope);
				}
				//Tuple insert		
				domain.put(tuple, null);		
				tuples.add(tuple);
			}

			printTuples(tuples, "New Tuple(s)");
			System.out.println("Put " + qtyTuples + " tuple(s)");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testarRead(GenericDomain domain, String dsc, Provider provider) {
		try {
			msgTextView.append("\n -- testarRead SysSU-DTS --");
			System.out.println("\n -- testarRead SysSU-DTS -- \n");	
			
			// Read Tuples
			Pattern pattern = (Pattern) new Pattern().addField("contextkey", "?");
			List<Tuple> tuples = null;

			//one read
			tuples = domain.read(pattern, "function filter(tuple) {return (tuple.value > 17)}", "", hScope, provider);	

			// Writes tuples in tupleSpace EditText
			printTuples(tuples, dsc);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printTuples(List<Tuple> tuples, String dsc) {
		String srtTuple = "";

		if (tuples != null){
			// Writes tuples in tupleSpace EditText
			msgTextView.append("\n---" + dsc + "--- qty of tuples: " + tuples.size());

			for (Tuple tp : tuples) {
				srtTuple = srtTuple + " {";
				for (TupleField tupleField : tp) {

					srtTuple = srtTuple + "(" + tupleField.getName() +","+ tupleField.getValue() + ")";
				}
				srtTuple = srtTuple + "} ";
			}
		}else{
			srtTuple = "NO TUPLES FOUND";
		}

		msgTextView.append("\n" + srtTuple);
		System.out.println("Tuplas: " + srtTuple + "\n");
	}

	public static AssetManager assetmanager;

}

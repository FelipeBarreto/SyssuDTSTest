package br.ufc.great.syssu.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
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
	private String myBluetoothName = "DEVICE";
	private int tupleCont = 0;

	private String messageTest = "SYSU-DTS Rocks!!";

	private Scope hScope = (Scope) new Scope().addField("happyScope", "happy");
	private Scope sScope = (Scope) new Scope().addField("sadScope", "sad").addField("tag", "master");
	private Scope allScope = (Scope) new Scope().addField("happyScope", "happy").addField("sadScope", "sad").addField("tag", "master").addField("x", "y");

	// experiment varialbles
	private static final int qtyLocalTuple = 1;
	private static final int qtyOfReads = 10;
	private static final long interReadDelay = 300; //miliseconds - to avoid timeout

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);

		assetmanager =  getAssets();

		System.out.println(">>> onCreate");

		msgTextView = (TextView) findViewById(R.id.txt_View);
		msgTextView.setText(messageTest);

		BluetoothAdapter bluetoothDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
		if ((bluetoothDefaultAdapter != null) && (bluetoothDefaultAdapter.isEnabled())){
			myBluetoothName = BluetoothAdapter.getDefaultAdapter().getName();
		}

		//start Ubicentre 
		try {
			Thread t = new Thread(new UbiCentreProcess(localPort), "UbiCentre Process");
			t.start();
		} catch (Exception ex) {
			System.exit(1);
		}
		Context ctx = getApplicationContext();

		try {
			// Create network manager
			networkManager = AdhocNetworkManager.getNetworkManagerInstance(ctx);

			// Create broker
			broker = GenericUbiBroker.createUbibroker(ctx);

			// Get a domain (tuple space subset)
			domain = (GenericDomain) broker.getDomain(scopeGreat);

			// insert tupes
			testarPut(domain, qtyLocalTuple, myBluetoothName, Provider.LOCAL);

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

			case R.id.menu_Experiment:
				// Escopo
				Scope profScope = (Scope) new Scope().addField("cargo", "aluno").addField("disciplina", "geologia");
				Scope alunoScope = (Scope) new Scope().addField("cargo", "professor").addField("disciplina", "geologia");
				Scope geologiaScope = (Scope) new Scope().addField("disciplina", "geologia");

				testarPut(domain, qtyLocalTuple, myBluetoothName, Provider.LOCAL);
				domain.take((Pattern) new Pattern().addField("?", "?"), "", "", Provider.LOCAL);

				for (int i = 0; i < 3; i++) {			
					experiment(Provider.ANY, profScope);
				}
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				for (int i = 0; i < 3; i++) {		
					experiment(Provider.ANY, alunoScope);
				}
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for (int i = 0; i < 3; i++) {		
					experiment(Provider.ANY, geologiaScope);
				}
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for (int i = 0; i < 3; i++) {		
					experiment(Provider.ANY, null);
				}
				
//Acesso transparente				
//				for (int i = 0; i < 3; i++) {		
//					domain.take((Pattern) new Pattern().addField("?", "?"), "", "", Provider.LOCAL);
//					domain.take((Pattern) new Pattern().addField("?", "?"), "", "", Provider.INFRA);
//					
//					testarPut(domain, qtyLocalTuple, myBluetoothName, Provider.LOCAL);
//					testarPut(domain, qtyLocalTuple, "SERVER", Provider.INFRA);
//					experiment(Provider.ANY, null);
//					
//					domain.take((Pattern) new Pattern().addField("?", "?"), "", "", Provider.LOCAL);
//					experiment(Provider.ANY, null);
//					
//					domain.take((Pattern) new Pattern().addField("?", "?"), "", "", Provider.INFRA);
//					experiment(Provider.ANY, null);
//					
//					System.out.println("*EXP* " + i + " roud");
//				}

				break;
			case R.id.menu_Put:
				testarPut(domain, 1, myBluetoothName, Provider.LOCAL);
				break;
			case R.id.menu_PutInfra:
				testarPut(domain, 1, "SERVER", Provider.INFRA);
				break;
			case R.id.menu_Take:
				domain.take((Pattern) new Pattern().addField("?", "?"), "", "", Provider.LOCAL);
				System.out.println(">>> Removed all LOCAL tuples");
				msgTextView.append("\n >>> Removed all LOCAL tuples");
				break;
			case R.id.menu_TakeInfra:
				domain.take((Pattern) new Pattern().addField("contextkey", "?"), "", "", Provider.INFRA);
				System.out.println(">>> Removed all SERVER tuples");
				msgTextView.append("\n >>> Removed all SERVER tuples");
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
				networkManager.ensureDiscoverable();
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
				msgTextView.append("\n >>> Connected Devices => " + getConnectedDevices());
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


	public void experiment(Provider provider, Scope scope) throws TupleSpaceException, TupleSpaceSecurityException {
		String strScope = "vazio";
		if(scope != null)
			strScope = scope.getField(0).getValue().toString();
		
//		String logMsg = "\n Read times: " + qtyOfReads + " - Connected Devices: " + getConnectedDevices() + " by " + provider + " read" ;
		String logMsg = "\n Read times: " + qtyOfReads + " - Scope: " + strScope + " by " + provider + " read" ;
		String providers = "";
		List<Tuple> allTuples = new ArrayList<Tuple>();
		List<Tuple> tuples = null;
		Pattern pattern = (Pattern) new Pattern().addField("contextkey", "?");

		Log.i("ad", "*** Experiment ***  " + "\n Read times: " + qtyOfReads + " - Connected Devices: " + getConnectedDevices());
		Log.i("ad", "i, startTime , stopTime , (stopTime-startTime) , sourceProviders , qtyOfTuples");

		//		ActivityManager activityManager =  (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		//		MemoryInfo memoryInfo = new MemoryInfo();
		//		activityManager.getMemoryInfo(memoryInfo);
		//		long memoryFreeBefore = memoryInfo.availMem;	

		for (int i = 0; i < qtyOfReads; i++) {
			long start = System.currentTimeMillis();
			tuples = domain.read(pattern, "", scope, provider);
			long stop = System.currentTimeMillis();

			try {
				Thread.sleep(interReadDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (tuples != null && !tuples.isEmpty()){
				providers = tuples.get(0).getField(4).getValue().toString();

				Set<String> pSet = new HashSet<String>();
				pSet.add(providers);

				String p;
				for (Tuple tp : tuples) {
					p = tp.getField(4).getValue().toString();
					if (pSet.add(p)){
						providers = providers + "-" + p; 
					}
				}

				Log.i("ad", i + "," + start + "," + stop +  "," + (stop-start) + "," + providers +  "," + tuples.size());
				logMsg = logMsg + "\n" + i + "," + start + "," + stop +  "," + (stop-start) + "," + providers +  "," + tuples.size();

				allTuples.addAll(tuples);
			} else{
				Log.i("ad", i + "," + start + "," + stop +  "," + (stop-start) + "," + "noTuples" +  "," + tuples.size());
				logMsg = logMsg + "\n" + i + "," + start + "," + stop +  "," + (stop-start) + "," + "noTuples" +  "," + tuples.size();
			}
		}	

		//		activityManager.getMemoryInfo(memoryInfo);
		//		long memoryFreeAfter = memoryInfo.availMem;
		//		Log.i("ad", "memoryFreeBefore(MB), memoryFreeAfter(MB), (memoryFreeBefore - memoryFreeAfter)");
		//		Log.i("ad", (memoryFreeBefore/1024) + "," + (memoryFreeAfter/1024) +  "," + ((memoryFreeBefore/1024) - (memoryFreeAfter/1024)));

		Log.i("ad", " *** End of Experiment *** (read " + allTuples.size() + " tuples)");

		LogFile("syssudts_exp_" + provider + "_scope_" + strScope + ".txt", logMsg);

		System.out.println("Qty of read tuples: " + allTuples.size());
		msgTextView.append("\n Qty of read tuples: " + allTuples.size() + " from " + providers + " by " + provider + " read" );
	}

	public void testarPut(GenericDomain domain, int qtyTuples, String value, Provider provider) {
		try {
			msgTextView.append("\n -- Put Tuple -- ");
			System.out.println("\n -- testarPut -- \n");
			// Create tuples
			Tuple tuple = null;
			List<Tuple> tuples = new ArrayList<Tuple>();

			for (int i = 0; i < qtyTuples; i++) {

				tuple = (Tuple) new Tuple().addField("contextkey","context.ambient.temperature").
						addField("source", "mobileSensor").
						addField("value",  18 + (int) (Math.random()*5)).
						//												addField("timestamp", System.currentTimeMillis()).
						//												addField("accurace", 0.8).
						//												addField("unit", "C").
						addField("cont", tupleCont++).
						addField("provider", value);

				// Define Tuple Scope
				//				if (i % 2 == 0) {
				//					tuple.setScope(hScope);
				//				}
				//				else {
				//					tuple.setScope(sScope);
				//				}

				//Tuple insert		
				domain.put (tuple, provider);		
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
			//			tuples = domain.read(pattern, "function filter(tuple) {return (tuple.value > 17)}", "", allScope, provider);
			tuples = domain.read(pattern,"", null, provider);

			// Writes tuples in tupleSpace EditText
			printTuples(tuples, dsc);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getConnectedDevices() {
		Map cd = AdhocNetworkManager.tsMonitor.getAvailableDevice();
		Set<String> dn = cd.keySet();
		String devices = "";
		for (String deviceName : dn) {
			if(devices.equalsIgnoreCase(""))
				devices = dn.size() + "_" + deviceName;
			else	
				devices = devices + "_" + deviceName;
		}
		return devices;
	}

	public void printTuples(List<Tuple> tuples, String dsc) {
		String srtTuple = "";
		if (tuples != null){
			// Writes tuples in tupleSpace EditText
			msgTextView.append("\n---" + dsc + "--- qty of tuples: " + tuples.size());
			for (Tuple tp : tuples) {
				srtTuple = srtTuple + " \n *{";
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

	private void LogFile(String fileName, String msg )
	{
		File file;
		byte[] dados;

		try {
			dados = msg.getBytes();

			file = new File(Environment.getExternalStorageDirectory(), fileName);
			FileOutputStream fos;
			fos = new FileOutputStream(file,true);
			fos.write(dados);
			fos.flush();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

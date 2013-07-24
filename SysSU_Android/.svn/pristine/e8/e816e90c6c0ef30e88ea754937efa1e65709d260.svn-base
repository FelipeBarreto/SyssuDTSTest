package br.ufc.great.syssu.ubicentre;

import br.ufc.great.syssu.ubicentre.controllers.FrontController;
import br.ufc.great.syssu.net.TCPNetwokServer;
import br.ufc.great.syssu.servicemanagement.*;
import br.ufc.great.syssu.servicemanagement.services.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UbiCentreProcess implements Runnable {

	private TCPNetwokServer server;

	public UbiCentreProcess(int port) throws IOException {
		ServiceManager.getInstance().addService(new PutService());
		ServiceManager.getInstance().addService(new ReadService());
		ServiceManager.getInstance().addService(new ReadOneService());
		ServiceManager.getInstance().addService(new ReadSyncService());
		ServiceManager.getInstance().addService(new ReadOneSyncService());

		ServiceManager.getInstance().addService(new TakeService());
		ServiceManager.getInstance().addService(new TakeOneService());
		ServiceManager.getInstance().addService(new TakeSyncService());
		ServiceManager.getInstance().addService(new TakeOneSyncService());

		ServiceManager.getInstance().addEventService(new PutEventService());

		this.server = new TCPNetwokServer(port);
		this.server.setNetworkObserver(FrontController.getInstance());
	}

	public void run() {
		if (server != null) {
			try {
				server.start();
				Logger.getLogger("UbiCentre").log(Level.INFO, "UbiCentre started on port " + server.getPort() + ".");
			} catch (IOException ex) {
				Logger.getLogger("UbiCentre").log(Level.SEVERE, "Error in starting UbiCentre.", ex);
			}
		}
	}
}

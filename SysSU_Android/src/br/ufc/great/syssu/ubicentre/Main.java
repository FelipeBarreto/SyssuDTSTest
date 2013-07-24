package br.ufc.great.syssu.ubicentre;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import br.ufc.great.syssu.base.Tuple;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.ubibroker.LocalUbiBroker;

public class Main {

	private static int port = 9090;
	private static String logfile;

	public static void main(String[] args) {
		Logger logger = Logger.getLogger("UbiCentre");
		logger.addHandler(new StreamHandler());

		try {
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.equals("--port")) {
					port = Integer.parseInt(args[++i]);
				} else if (arg.equals("--logfile")) {
					logfile = args[++i];
				} else {
					throw new Exception();
				}
			}
		} catch (Exception ex) {
			logger.severe("Invalid parameters.");
		}

		if (logfile != null) {
			try {
				FileHandler fileHandler = new FileHandler(logfile, true);
				fileHandler.setFormatter(new CustomLogFormatter());
				logger.addHandler(fileHandler);
			} catch (Exception ex) {
				logger.severe("Error in starting UbiCentre. Invalid log file.");
				System.exit(1);
			}
		}

		try {
			Thread t = new Thread(new UbiCentreProcess(port), "UbiCentre Process");
			t.start();
			logger.info("UbiCentre started and listening in port " + port + ".");
			//t.join();


			// Create local broker
			LocalUbiBroker localBroker = LocalUbiBroker.createUbibroker();
			// Get a domain (tuple space subset) from local broker
			IDomain localDomain = localBroker.getDomain("scopeGreat");
			testarPut(localDomain, 2);
//			
//			localDomain = localBroker.getDomain("great.lab2");
//			testarPut(localDomain, 200);
//			
//			localDomain = localBroker.getDomain("great.lab3");
//			testarPut(localDomain, 200);
//			
//			localDomain = localBroker.getDomain("great.lab4");
//			testarPut(localDomain, 200);
//			
//			localDomain = localBroker.getDomain("great.lab5");
//			testarPut(localDomain, 200);
//			
//			localDomain = localBroker.getDomain("great.lab6");
//			testarPut(localDomain, 200);

		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Error in starting UbiCentre.", ex);
			System.exit(1);
		}
	}

	public static void testarPut(IDomain domain, int qtyTuples) {
		try {
			System.out.println("\n -- testarPut -- \n");
			// Create tuples
			Tuple tuple = null;

			for (int i = 0; i < qtyTuples; i++) {
				tuple = (Tuple) new Tuple().addField("contextkey","context.ambient.temperature").
						addField("source", "physicalsensor").
						addField("value",  20 + ( int ) ( Math.random() *3 )).
						addField("timestamp", System.currentTimeMillis()).
						addField("accurace", 0.8).
						addField("unit", "C").
						addField("cont", i+1).
				        addField("scope", domain.getName()).
				        addField("server", "beneditou");
				//Tuple insert
				domain.put(tuple, null);
			}
			System.out.println("Put " + qtyTuples + " tupla(s)");


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import br.ufc.great.syssu.base.Scope;
import br.ufc.great.syssu.base.Tuple;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.ubibroker.LocalUbiBroker;
import br.ufc.great.syssu.ubicentre.CustomLogFormatter;
import br.ufc.great.syssu.ubicentre.UbiCentreProcess;

public class Main {

	private static int port = 9090;
	private static String logfile;

	private static Scope hScope = (Scope) new Scope().addField("happyScope", "happy");

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
			
			
			Scope profScope = (Scope) new Scope().addField("cargo", "aluno").addField("disciplina", "geologia");
			Scope alunoScope = (Scope) new Scope().addField("cargo", "professor").addField("disciplina", "geologia");
			Scope geologiaScope = (Scope) new Scope().addField("disciplina", "geologia");
			
			testarPut(localDomain, 3, "INFRA-SERVER", profScope);
			testarPut(localDomain, 10, "INFRA-SERVER", alunoScope);
			testarPut(localDomain, 7, "INFRA-SERVER", geologiaScope);
			testarPut(localDomain, 10, "INFRA-SERVER", null);
			
			//System.out.println("removed " + localDomain.take((Pattern) new Pattern().addField("contextkey", "?"), "", "").size());


		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Error in starting UbiCentre.", ex);
			System.exit(1);
		}
	}

	public static void testarPut(IDomain domain, int qtyTuples, String value, Scope scope) {
		try {
			System.out.println("\n -- testarPut -- \n");
			// Create tuples
			Tuple tuple = null;

			for (int i = 0; i < qtyTuples; i++) {

				tuple = (Tuple) new Tuple().addField("contextkey","context.ambient.temperature").
												addField("source", "physicalsensor").
						addField("value",  18 + (int) (Math.random()*5)).
//												addField("timestamp", System.currentTimeMillis()).
//												addField("accurace", 0.8).
//												addField("unit", "C").
						addField("cont", i+1).
						addField("provider", value);

				if (scope != null)
					tuple.setScope(scope);
				

				//Tuple insert
				domain.put(tuple, null);
			}
			System.out.println("Put " + qtyTuples + " tupla(s)");


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


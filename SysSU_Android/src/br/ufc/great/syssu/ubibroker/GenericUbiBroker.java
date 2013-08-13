package br.ufc.great.syssu.ubibroker;

import java.io.IOException;

import android.content.Context;
import br.ufc.great.syssu.base.Provider;
import br.ufc.great.syssu.base.TupleSpaceException;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.net.InfraNetworkFinder;

public class GenericUbiBroker {

	private LocalUbiBroker localUbiBroker;
	private UbiBroker infraUbiBroker;
	private UbiBroker adhocUbiBroker;
	private Context context;

	private GenericUbiBroker(Context context) throws IOException {
		this.context = context;
		this.localUbiBroker = LocalUbiBroker.createUbibroker();

		String ubicentreAddress = null;
		if(context != null) {
			InfraNetworkFinder infraNetworkFinder = new InfraNetworkFinder(context);
			ubicentreAddress = infraNetworkFinder.getUbicentreAddress();
		}
		this.adhocUbiBroker = UbiBroker.createUbibroker(
				context,
				Provider.ADHOC
				);
		this.infraUbiBroker = UbiBroker.createUbibroker(
				ubicentreAddress,
				InfraNetworkFinder.UBICENTRE_PORT,
				InfraNetworkFinder.REACTIONS_PORT,
				context,
				Provider.INFRA
				);
	}

	public static GenericUbiBroker createUbibroker(Context context) throws IOException {
		GenericUbiBroker instance = new GenericUbiBroker(context);
		return instance;
	}

	public static GenericUbiBroker createUbibroker() throws IOException {
		return GenericUbiBroker.createUbibroker(null);
	}

	// Returns the UbiBroker associated domain in order to handle with tuple space operations
	public IDomain getDomain(String name) throws TupleSpaceException, IOException {
		return new GenericDomain(name, localUbiBroker, infraUbiBroker, adhocUbiBroker);
	}
}

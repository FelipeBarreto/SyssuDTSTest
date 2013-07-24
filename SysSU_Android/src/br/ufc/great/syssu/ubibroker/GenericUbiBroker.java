package br.ufc.great.syssu.ubibroker;

import java.io.IOException;

import android.content.Context;
import br.ufc.great.syssu.base.TupleSpaceException;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.net.InfraNetworkFinder;

public class GenericUbiBroker {

	private LocalUbiBroker localUbiBroker = null;
	private UbiBroker infraUbiBroker = null;
	private UbiBroker adhocUbiBroker = null;

	private GenericUbiBroker(Context context) throws IOException {

		this.localUbiBroker = LocalUbiBroker.createUbibroker();

		if(context != null) {
			InfraNetworkFinder infraNetworkFinder = new InfraNetworkFinder(context);
			String ubicentreAddress = infraNetworkFinder.getUbicentreAddress();

			this.infraUbiBroker = UbiBroker.createUbibroker(
					ubicentreAddress,
					InfraNetworkFinder.UBICENTRE_PORT,
					InfraNetworkFinder.REACTIONS_PORT
					);

			this.adhocUbiBroker = UbiBroker.createUbibroker(
					ubicentreAddress, 
					InfraNetworkFinder.UBICENTRE_PORT+3, 
					InfraNetworkFinder.REACTIONS_PORT+3,
					context,
					""
					);
		}
	}

	public static GenericUbiBroker createUbibroker(Context context) throws IOException {
		GenericUbiBroker instance = new GenericUbiBroker(context);
		return instance;
	}

	public static GenericUbiBroker createUbibroker() throws IOException {
		return GenericUbiBroker.createUbibroker(null);
	}

	// Returns the UbiBroker associated domain in order to handle with tuple space operations
	public IDomain getDomain(String name) throws TupleSpaceException {
		GenericDomain gdDomain = new GenericDomain(name, localUbiBroker, infraUbiBroker, adhocUbiBroker);

		System.out.println(gdDomain);
		return gdDomain;
	}
}

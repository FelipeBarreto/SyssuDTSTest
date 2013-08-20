package br.ufc.great.syssu.ubibroker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufc.great.syssu.base.Pattern;
import br.ufc.great.syssu.base.Provider;
import br.ufc.great.syssu.base.Scope;
import br.ufc.great.syssu.base.Tuple;
import br.ufc.great.syssu.base.TupleSpaceException;
import br.ufc.great.syssu.base.TupleSpaceSecurityException;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.base.interfaces.IReaction;

public class GenericDomain implements IDomain {

	private String name;
	private LocalUbiBroker localUbiBroker;
	private UbiBroker infraUbiBroker;
	private UbiBroker adhocUbiBroker;

	private IDomain localDomain;
	private IDomain infraDomain;
	private IDomain adhocDomain;


	GenericDomain(String domainName, LocalUbiBroker localUbiBroker, UbiBroker infraUbiBroker, UbiBroker adhocUbiBroker) throws TupleSpaceException, IOException {
		this.name = domainName;
		this.localUbiBroker = localUbiBroker;
		this.infraUbiBroker = infraUbiBroker;
		this.adhocUbiBroker = adhocUbiBroker;

		if (localUbiBroker != null) {
			localDomain = localUbiBroker.getDomain(domainName);
		}
		if (findServerConnection()) {
			infraDomain = infraUbiBroker.getDomain(domainName, Provider.INFRA);
		}
		if (adhocUbiBroker != null) {
			adhocDomain = adhocUbiBroker.getDomain(domainName, Provider.ADHOC);
		}
	}

	@Override
	public IDomain getDomain(String name) throws TupleSpaceException {
		try {
			return new GenericDomain(this.name + "." + name, localUbiBroker, infraUbiBroker, adhocUbiBroker);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}


	@Override
	public void put(Tuple tuple, String key) throws TupleSpaceException,
	TupleSpaceSecurityException {
		put(tuple, key, Provider.LOCAL);
	}

	public void put(Tuple tuple) throws TupleSpaceException,
	TupleSpaceSecurityException {
		put(tuple, "", Provider.LOCAL);
	}

	public void put(Tuple tuple, Provider provider) throws TupleSpaceException,
	TupleSpaceSecurityException {
		put(tuple, "", provider);
	}

	public void put(Tuple tuple, String key, Provider provider) throws TupleSpaceException,
	TupleSpaceSecurityException {

		switch (provider) {
		case INFRA:
			if(infraDomain != null && findServerConnection()){
				System.out.println("***Put INFRA");			
				infraDomain.put(tuple, key);
			}
			break;
		case ALL:
			System.out.println("***Put ALL");
			if(localDomain != null){
				System.out.println("***Put LOCAL");
				localDomain.put(tuple, key);
			}
			if(infraDomain != null && findServerConnection()){
				System.out.println("***Put INFRA");
				infraDomain.put(tuple, key);
			}
			break;
		default:
			if(localDomain != null){
				System.out.println("***Put LOCAL");
				localDomain.put(tuple, key);
			}
			break;
		}		
	}

	@Override
	public List<Tuple> read(Pattern pattern, String restriction, String key, Scope scope)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return read(pattern, restriction, key, scope, Provider.ANY);
	}

	public List<Tuple> read(Pattern pattern, String restriction, Scope scope)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return read(pattern, restriction, "", scope, Provider.ANY);
	}

	public List<Tuple> read(Pattern pattern, String restriction, Scope scope, Provider provider)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return read(pattern, restriction, "", scope, provider);
	}


	public List<Tuple> read(Pattern pattern, String restriction, String key, Scope scope, Provider provider)
			throws TupleSpaceException, TupleSpaceSecurityException {

		List<Tuple> tuples = new ArrayList<Tuple>();

		if (scope == null){
			scope = new Scope();
		}
		
		switch (provider) {
		case LOCAL:
			if(localDomain != null){
				System.out.println("***Buscando LOCAL");
				tuples = localDomain.read(pattern, restriction, key, scope);
			}
			break;
		case INFRA:
			if(findServerConnection() && infraDomain != null){
				System.out.println("***Buscando INFRA");			
				tuples = infraDomain.read(pattern, restriction, key, scope);
			}
			break;
		case ADHOC:
			if(adhocDomain != null){
				System.out.println("***Buscando ADHOC");
				tuples = adhocDomain.read(pattern, restriction, key, scope);
			}
			break;
		case ALL:
			System.out.println("***Buscando ALL");
			if(localDomain != null){
				System.out.println("***Buscando LOCAL");
				tuples = localDomain.read(pattern, restriction, key, scope);
			}
			if(infraDomain != null && findServerConnection()){
				System.out.println("***Buscando INFRA");
				tuples.addAll(infraDomain.read(pattern, restriction, key, scope));
			}
			if(adhocDomain != null){
				System.out.println("***Buscando ADHOC");
				tuples.addAll(adhocDomain.read(pattern, restriction, key, scope));
			}
			break;
		default: // ANY provider
			System.out.println("***Buscando ANY");
			if(localDomain != null){
				System.out.println("***Buscando LOCAL");
				tuples = localDomain.read(pattern, restriction, key, scope);
			}
			if((tuples == null || tuples.size() == 0)){
				System.out.println("***Buscando INFRA");
				if (infraDomain != null && findServerConnection()){
					tuples = infraDomain.read(pattern, restriction, key, scope);
				}
				if((tuples == null || tuples.size() == 0) && adhocDomain != null) {
					System.out.println("***Buscando ADHOC");
					tuples = adhocDomain.read(pattern, restriction, key, scope);
				}
			}
			break;
		}
		return tuples;
	}

	@Override
	public List<Tuple> readSync(Pattern pattern, String restriction,
			String key, long timeout) throws TupleSpaceException,
			TupleSpaceSecurityException {
		return readSync(pattern, restriction, key, timeout, Provider.ANY);
	}

	public List<Tuple> readSync(Pattern pattern, String restriction,
			String key, long timeout, Provider provider) throws TupleSpaceException,
			TupleSpaceSecurityException {

		List<Tuple> tuples = new ArrayList<Tuple>();

		switch (provider) {
		case LOCAL:
			tuples = localDomain.readSync(pattern, restriction, key, timeout);
			break;
		case INFRA:
			tuples = infraDomain.readSync(pattern, restriction, key, timeout);
			break;
		case ADHOC:
			tuples = adhocDomain.readSync(pattern, restriction, key, timeout);
			break;
		case ALL:
			tuples = localDomain.readSync(pattern, restriction, key, timeout);
			tuples.addAll(infraDomain.readSync(pattern, restriction, key, timeout));
			tuples.addAll(adhocDomain.readSync(pattern, restriction, key, timeout));
			break;
		default: // ANY provider

			tuples = localDomain.readSync(pattern, restriction, key, timeout);

			if(tuples == null || tuples.size() == 0) {
				tuples = infraDomain.readSync(pattern, restriction, key, timeout);

				if(tuples == null || tuples.size() == 0) {
					tuples = adhocDomain.readSync(pattern, restriction, key, timeout);
				}
			}
			break;
		}
		return tuples;
	}

	@Override
	public Tuple readOne(Pattern pattern, String restriction, String key, Scope scope)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return readOne(pattern, restriction, key, scope, Provider.ANY);	
	}

	public Tuple readOne(Pattern pattern, String restriction, String key, Scope scope, Provider provider)
			throws TupleSpaceException, TupleSpaceSecurityException {

		Tuple tuple = null;

		switch (provider) {
		case LOCAL:
			tuple = localDomain.readOne(pattern, restriction, key, scope);
			break;
		case INFRA:
			tuple = infraDomain.readOne(pattern, restriction, key, scope);
			break;
		case ADHOC:
			tuple = adhocDomain.readOne(pattern, restriction, key, scope);
			break;
		default: // ANY provider
			tuple = localDomain.readOne(pattern, restriction, key, scope);

			if(tuple == null || tuple.size() == 0) {
				tuple = infraDomain.readOne(pattern, restriction, key, scope);

				if(tuple == null || tuple.size() == 0) {
					tuple = adhocDomain.readOne(pattern, restriction, key, scope);
				}
			}
			break;
		}
		return tuple;
	}

	@Override
	public Tuple readOneSync(Pattern pattern, String restriction, String key, long timeout, Scope scope)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return readOneSync(pattern, restriction, key, timeout, scope, Provider.ANY);	
	}

	public Tuple readOneSync(Pattern pattern, String restriction, String key,
			long timeout, Scope scope, Provider provider) throws TupleSpaceException,
			TupleSpaceSecurityException {

		Tuple tuple = null;

		switch (provider) {
		case LOCAL:
			tuple = localDomain.readOneSync(pattern, restriction, key, timeout, scope);
			break;
		case INFRA:
			tuple = infraDomain.readOneSync(pattern, restriction, key, timeout, scope);
			break;
		case ADHOC:
			tuple = adhocDomain.readOneSync(pattern, restriction, key, timeout, scope);
			break;
		default: // ANY provider
			tuple = localDomain.readOneSync(pattern, restriction, key, timeout, scope);

			if(tuple == null || tuple.size() == 0) {
				tuple = infraDomain.readOneSync(pattern, restriction, key, timeout, scope);

				if(tuple == null || tuple.size() == 0) {
					tuple = adhocDomain.readOneSync(pattern, restriction, key, timeout, scope);
				}
			}
			break;
		}
		return tuple;
	}

	@Override
	public List<Tuple> take(Pattern pattern, String restriction, String key)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return take(pattern, restriction, key, Provider.LOCAL);	
	}

	public List<Tuple> take(Pattern pattern, String restriction, String key, Provider provider)
			throws TupleSpaceException, TupleSpaceSecurityException {

		if(provider == Provider.INFRA)
			return infraDomain.take(pattern, restriction, key);

		// Default Provider. It's not permitted take tuple from ad hoc providers.
		return localDomain.take(pattern, restriction, key);
	}

	@Override
	public List<Tuple> takeSync(Pattern pattern, String restriction, String key, long timeout)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return takeSync(pattern, restriction, key, timeout, Provider.LOCAL);	
	}

	public List<Tuple> takeSync(Pattern pattern, String restriction,
			String key, long timeout, Provider provider) throws TupleSpaceException,
			TupleSpaceSecurityException {

		if(provider == Provider.INFRA)
			return infraDomain.takeSync(pattern, restriction, key, timeout);

		// Default Provider. It's not permitted take tuple from ad hoc providers.
		return localDomain.takeSync(pattern, restriction, key, timeout);


	}

	@Override
	public Tuple takeOne(Pattern pattern, String restriction, String key)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return takeOne(pattern, restriction, key, Provider.LOCAL);
	}

	public Tuple takeOne(Pattern pattern, String restriction, String key, Provider provider)
			throws TupleSpaceException, TupleSpaceSecurityException {

		if(provider == Provider.INFRA)
			return infraDomain.takeOne(pattern, restriction, key);

		// Default Provider. It's not permitted take tuple from ad hoc providers.
		return localDomain.takeOne(pattern, restriction, key);
	}

	@Override
	public Tuple takeOneSync(Pattern pattern, String restriction, String key,
			long timeout) throws TupleSpaceException,
			TupleSpaceSecurityException {
		return takeOneSync(pattern, restriction, key, timeout, Provider.LOCAL);
	}


	public Tuple takeOneSync(Pattern pattern, String restriction, String key,
			long timeout, Provider provider) throws TupleSpaceException,
			TupleSpaceSecurityException {

		if(provider == Provider.INFRA)
			return infraDomain.takeOneSync(pattern, restriction, key, timeout);

		// Default Provider. It's not permitted take tuple from ad hoc providers.
		return localDomain.takeOneSync(pattern, restriction, key, timeout);
	}

	@Override
	public Object subscribe(IReaction reaction, String event, String key)
			throws TupleSpaceException, TupleSpaceSecurityException {
		return subscribe(reaction, event, key, Provider.ALL);
	}

	public Object subscribe(IReaction reaction, String event, String key, Provider provider)
			throws TupleSpaceException, TupleSpaceSecurityException {

		Object obj = null;

		switch (provider) {
		case LOCAL:
			obj = localDomain.subscribe(reaction, event, key);
			break;

		case INFRA:
			obj = infraDomain.subscribe(reaction, event, key);
			break;

		case ADHOC:
			obj = adhocDomain.subscribe(reaction, event, key);
			break;
		default: // ALL providers

			List<Object> objList = new ArrayList<Object>();
			objList.add(localDomain.subscribe(reaction, event, key));
			objList.add(infraDomain.subscribe(reaction, event, key));
			objList.add(adhocDomain.subscribe(reaction, event, key));

			obj = objList;

			break;
		}

		return obj;
	}

	@Override
	public void unsubscribe(Object reactionId, String key)
			throws TupleSpaceException, TupleSpaceSecurityException {

		localDomain.unsubscribe(reactionId, key);
		infraDomain.unsubscribe(reactionId, key);
		adhocDomain.unsubscribe(reactionId, key);
	}

	private boolean findServerConnection(){
		
		if (infraUbiBroker.hasInfraConnection()){
			return true;
		}
		
		try {
			//Finding a server connection
			System.out.println("***Finding SERVER Connection");
			infraUbiBroker.updateInfraConnection();
			
			if(infraUbiBroker.hasInfraConnection()){
				infraDomain = infraUbiBroker.getDomain(getName(), Provider.INFRA);
				return true;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TupleSpaceException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}

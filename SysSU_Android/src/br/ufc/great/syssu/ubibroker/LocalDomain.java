package br.ufc.great.syssu.ubibroker;

import java.util.List;

import br.ufc.great.syssu.base.Pattern;
import br.ufc.great.syssu.base.Provider;
import br.ufc.great.syssu.base.Scope;
import br.ufc.great.syssu.base.Tuple;
import br.ufc.great.syssu.base.TupleSpaceException;
import br.ufc.great.syssu.base.TupleSpaceSecurityException;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.base.interfaces.IReaction;
import br.ufc.great.syssu.coordubi.TupleSpace;

public class LocalDomain implements IDomain {

	private IDomain centreDomain;

    private String name;
    private LocalUbiBroker ubiBroker;

    LocalDomain(String domainName, LocalUbiBroker ubiBroker) throws TupleSpaceException {
        this.name = domainName;
        this.ubiBroker = ubiBroker;
        // returns a tuple space related to the specified Domain
    	centreDomain = TupleSpace.getInstance().getDomain(getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void put(Tuple tuple, String key) throws TupleSpaceException {
		try {
			tuple.setTimeToLive(tuple.getTimeToLive());
			centreDomain.put(tuple, key);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
    }

    @Override
    public List<Tuple> read(Pattern pattern, String restriction, String key, Scope scope) throws TupleSpaceException {
        List<Tuple> tuples = null;
        try {
        	tuples = centreDomain.read(pattern, restriction, key, scope);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
		return tuples;
    }
    
    public List<Tuple> read(Pattern pattern, String restriction, String key) throws TupleSpaceException {
        return read(pattern, restriction, key, null);
    }

    @Override
    public List<Tuple> readSync(Pattern pattern, String restriction, String key, long timeout)
    	throws TupleSpaceException {
        List<Tuple> tuples = null;
        try {
        	tuples = centreDomain.readSync(pattern, restriction, key, timeout);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
		return tuples;
    }

    @Override
    public Tuple readOne(Pattern pattern, String restriction, String key, Scope scope) throws TupleSpaceException {
        Tuple tuple = null;
        try {
        	tuple = centreDomain.readOne(pattern, restriction, key, scope);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
		return tuple;

    }

    @Override
    public Tuple readOneSync(Pattern pattern, String restriction, String key, long timeout, Scope scope)
    	throws TupleSpaceException {
    	Tuple tuple = null;
        try {
        	tuple = centreDomain.readOneSync(pattern, restriction, key, timeout, scope);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
		return tuple;
    }

    @Override
    public List<Tuple> take(Pattern pattern, String restriction, String key) throws TupleSpaceException {
    	List<Tuple> tuples = null;
        try {
        	tuples = centreDomain.take(pattern, restriction, key);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
		return tuples;
    }

    @Override
    public List<Tuple> takeSync(Pattern pattern, String restriction, String key, long timeout)
    	throws TupleSpaceException {
    	List<Tuple> tuples = null;
        try {
        	tuples = centreDomain.takeSync(pattern, restriction, key, timeout);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
		return tuples;
    }

    @Override
    public Tuple takeOne(Pattern pattern, String restriction, String key) throws TupleSpaceException {
    	Tuple tuple = null;
        try {
        	tuple = centreDomain.takeOne(pattern, restriction, key);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
		return tuple;
    }

    @Override
    public Tuple takeOneSync(Pattern pattern, String restriction, String key, long timeout)
    	throws TupleSpaceException {
    	Tuple tuple = null;
        try {
        	tuple = centreDomain.takeOneSync(pattern, restriction, key, timeout);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
		return tuple;
    }

    public IDomain getDomain(String name) throws TupleSpaceException {
        return new LocalDomain(this.name + "." + name, ubiBroker);
    }

    @Override
    public Object subscribe(IReaction reaction, String event, String key) throws TupleSpaceException {
    	Object o = null;
    	try {
			 o = centreDomain.subscribe(reaction, event, key);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
          return o;
    }

    @Override
	public void unsubscribe(Object reactionId, String key) throws TupleSpaceException {
    	try {
			centreDomain.unsubscribe(reactionId, key);
		} catch (TupleSpaceSecurityException e) {
			e.printStackTrace();
		}
	}
}

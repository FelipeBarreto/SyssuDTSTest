package br.ufc.great.syssu.ubibroker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufc.great.syssu.base.TupleSpaceException;
import br.ufc.great.syssu.base.interfaces.IDomain;
import br.ufc.great.syssu.base.interfaces.IReaction;

public class LocalUbiBroker {

    private List<IReaction> reactions;

    private LocalUbiBroker() throws IOException {
        this.reactions = new ArrayList<IReaction>();
    }

    public static LocalUbiBroker createUbibroker() 
    		throws IOException {
        LocalUbiBroker instance = new LocalUbiBroker();
        return instance;
    }
    // Returns the UbiBroker associated domain in order to handle with tuple space operations
    public IDomain getDomain(String name) throws TupleSpaceException {
        return new LocalDomain(name, this);
    }

    void addReaction(IReaction reaction) throws TupleSpaceException {
        reactions.add(reaction);
    }
}

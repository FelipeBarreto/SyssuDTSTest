package br.ufc.great.syssu.base;

import java.security.InvalidParameterException;

import br.ufc.great.syssu.base.interfaces.IMatcheable;

public class Tuple extends AbstractFieldCollection<TupleField> implements IMatcheable {

	private long timeToLive;
	private long putTime;
	private Scope scope;

	public Tuple() {
		super();
	}
	
	public Tuple(long timeToLive) {
		super();
		setTimeToLive(timeToLive);
	}

	@Override
	public TupleField createField(String name, Object value) {
		return new TupleField(name, value);
	}

	@Override
	public boolean matches(Query query) throws FilterException {
		if(query == null || query.getPattern() == null  || query.getPattern().isEmpty() || this.isEmpty()) {
			return true;
		}
		return associatesAll(query.getPattern()) && TupleFilter.doFilter(this, query.getFilter());
	}

	public long getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(long timeToLive) {
		if(timeToLive < 0) throw new InvalidParameterException("Negative time");
		this.timeToLive = timeToLive;
	}

	public long getPutTime() {
		return putTime;
	}

	public void setPutTime(long putTime) {
		if(putTime < 0) throw new InvalidParameterException("Negative time");
		this.putTime = putTime;
	}
	
	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	// If timeToLive is not defined in the constructor, its value is zero and the tuple will always be alive. 
	// Otherwise, is calculated how much time the tuple was inserted.
	public boolean isAlive() {
		return timeToLive == 0 || System.currentTimeMillis() - putTime < timeToLive;
	}
	
	// Check if tuple has a scope match
	public boolean isInScope(Scope s) {	
		if(this.scope == null || this.scope.isEmpty()) {
			return true;
		}
		
		if(s == null || s.isEmpty() || s.size() < this.scope.size()) {
			return false;
		}	
		
		return scopedAll(s);
	}

	private boolean associatesAll(Pattern pattern) {
		boolean matches = true;

		for (PatternField pField : pattern) {
			matches = (matches) ? associatesOne(pField) : false;
		}
		return matches;
	}

	private boolean associatesOne(PatternField pField) {
		for (TupleField tField : this) {
			if (tField.associates(pField)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean scopedAll(Scope s) {
		boolean scoped = true;

		for (PatternField pField : this.scope) {
			scoped = (scoped) ? scopedOne(pField, s) : false;
		}
		return scoped;
	}

	private boolean scopedOne(PatternField pField, Scope s) {
		for (PatternField pf : s) {
			if (pField.associates(pf)) {
				return true;
			}
		}
		return false;
	}
}

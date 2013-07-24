package br.ufc.great.syssu.coordubi;

import java.util.HashMap;
import java.util.Map;

import br.ufc.great.syssu.base.Scope;

public class ScopeEngine {
	
	private static ScopeEngine instance;
	private Map<String, Scope> scopes;
	
	private ScopeEngine() {
		scopes = new HashMap<String, Scope>();
	}
	
	public static ScopeEngine getInstance() {
		if(instance == null) {
			instance = new ScopeEngine();
		}
		return instance;
	}
	
	public void addScope(String domainName, Scope scope) {
		scopes.put(domainName, scope);
	}
	
	public void removeScope(String domainName) {
		scopes.remove(domainName);
	}
	
	public boolean isScoped(Scope scope) {
		return scope == null
			|| scopes.containsValue(scope);
	}

}

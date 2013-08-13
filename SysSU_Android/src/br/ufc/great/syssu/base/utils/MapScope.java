package br.ufc.great.syssu.base.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.ufc.great.syssu.base.PatternField;
import br.ufc.great.syssu.base.Scope;

public class MapScope implements IMappable<Scope> {

	private Scope scope;

	public MapScope(Map<String, Object> map) {
		this.scope = fromMap(map);
	}

	public MapScope(Scope scope) {
		this.scope = scope;
	}

	@Override
	public Map<String, Object> getMap() {
		return (scope != null) ? toMap(scope) : null;
	}

	@Override
	public Scope getObject() {
		return scope;
	}

	private Scope fromMap(Map<String, Object> map) {
		Scope newScope = new Scope();
		if (map != null){
			for (Entry<String, Object> entry : map.entrySet()) {
				newScope.addField(entry.getKey(), fromObject(entry.getValue()));
			}
		}
		return newScope;
	}

	private List<Object> fromList(List<Object> list) {
		List<Object> newList = new ArrayList<Object>();
		for (Object obj : list) {
			newList.add(fromObject(obj));
		}
		return newList;
	}

	@SuppressWarnings("unchecked")
	private Object fromObject(Object object) {
		if (object != null) {
			if (object instanceof Boolean || object instanceof Number || object instanceof String) {
				return object;
			} else if (object instanceof List) {
				return fromList((List<Object>) object);
			} else if (object instanceof Map) {
				return fromMap((Map<String, Object>) object);
			}
		}
		throw new IllegalArgumentException(
				"Invalid value type. Only Boolean, Number, String, List and Tuple are accepted.");
	}

	private Map<String, Object> toMap(Scope scope) {
		Map<String, Object> newMap = new LinkedHashMap<String, Object>();
		for (int i = 0; i < scope.size(); i++) {
			PatternField field = scope.getField(i);
			newMap.put(field.getName(), toObject(field.getValue()));
		}
		return newMap;
	}

	private List<Object> toList(List<Object> list) {
		List<Object> newList = new ArrayList<Object>();
		for (Object object : list) {
			newList.add(toObject(object));
		}
		return newList;
	}

	@SuppressWarnings("unchecked")
	private Object toObject(Object object) {
		if (object != null) {
			if (object instanceof Boolean || object instanceof Number || object instanceof String) {
				return object;
			} else if (object instanceof List) {
				return toList((List<Object>) object);
			} else if (object instanceof Scope) {
				return toMap((Scope) object);
			}
		}
		throw new IllegalArgumentException(
				"Invalid value type. Only Boolean, Number, String, List and Tuple are accepted.");
	}
}

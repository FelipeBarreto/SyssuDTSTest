package br.ufc.great.syssu.base;

public class Scope extends AbstractFieldCollection<PatternField> {

	public Scope() {
		super();
	}

	@Override
	public PatternField createField(String name, Object value) {
		return new PatternField(name, value);
	}
	
}

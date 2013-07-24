package br.ufc.great.syssu.base;

public class Pattern extends AbstractFieldCollection<PatternField> {

	public Pattern() {
		super();
	}

	@Override
	public PatternField createField(String name, Object value) {
		return new PatternField(name, value);
	}
}

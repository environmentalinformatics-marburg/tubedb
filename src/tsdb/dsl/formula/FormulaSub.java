package tsdb.dsl.formula;

import tsdb.dsl.FormulaVisitor1;

public class FormulaSub extends FormulaBinary {
	public FormulaSub(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitSub(this);
	}
}

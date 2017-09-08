package tsdb.dsl.formula;

import tsdb.dsl.FormulaVisitor1;

public class FormulaPow extends FormulaBinary {
	public FormulaPow(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitPow(this);
	}
}

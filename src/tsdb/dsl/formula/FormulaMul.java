package tsdb.dsl.formula;

import tsdb.dsl.FormulaVisitor1;

public class FormulaMul extends FormulaBinary {
	public FormulaMul(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitMul(this);
	}
	
	@Override
	public String toString() {
		return "(" + a + " * " + b + ")";
	}
}

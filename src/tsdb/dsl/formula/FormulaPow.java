package tsdb.dsl.formula;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
import tsdb.dsl.computation.Computation;

public class FormulaPow extends FormulaBinary {
	public FormulaPow(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitPow(this);
	}
}

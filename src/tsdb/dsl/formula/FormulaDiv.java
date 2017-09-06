package tsdb.dsl.formula;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
import tsdb.dsl.computation.Computation;

public class FormulaDiv extends FormulaBinary {
	public FormulaDiv(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitDiv(this);
	}
}

package tsdb.dsl.formula;

import java.util.Map;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
import tsdb.dsl.computation.Computation;

public class FormulaSub extends FormulaBinary {
	public FormulaSub(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitSub(this);
	}
}

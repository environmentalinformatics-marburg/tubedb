package tsdb.dsl.formula;

import java.util.Map;

import tsdb.dsl.BooleanFormulaVisitor1;
import tsdb.dsl.Environment;
import tsdb.dsl.computation.BooleanComputation;
import tsdb.dsl.computation.Computation;

public class BooleanFormulaLessEqual extends BooleanFormulaAtomicBinary {
	public BooleanFormulaLessEqual(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public BooleanFormula not() {
		return new BooleanFormulaLess(b, a);
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitLessEqual(this);
	}
}

package tsdb.dsl.formula;

import java.util.Map;

import tsdb.dsl.BooleanFormulaVisitor1;
import tsdb.dsl.Environment;
import tsdb.dsl.computation.BooleanComputation;
import tsdb.dsl.computation.Computation;

public class BooleanFormulaNotEqual extends BooleanFormulaAtomicBinary {
	public BooleanFormulaNotEqual(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public BooleanFormula not() {
		return new BooleanFormulaEqual(a, b);
	}
	@Override
	public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
		return visitor.visitNotEqual(this);
	}
}

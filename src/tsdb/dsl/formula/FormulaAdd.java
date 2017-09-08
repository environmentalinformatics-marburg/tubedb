package tsdb.dsl.formula;

import tsdb.dsl.FormulaVisitor1;

public class FormulaAdd extends FormulaBinary {
	public FormulaAdd(Formula a, Formula b) {
		super(a, b);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitAdd(this);		
	}	
}

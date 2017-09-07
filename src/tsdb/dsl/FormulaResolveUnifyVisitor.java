package tsdb.dsl;

import tsdb.dsl.computation.ComputationOfTime;
import tsdb.dsl.formula.Formula;
import tsdb.dsl.formula.FormulaNonDataVar;
import tsdb.dsl.formula.FormulaNum;
import tsdb.dsl.formula.FormulaVar;

public class FormulaResolveUnifyVisitor extends FormulaUnifyVisitor {	
	private final Environment env;

	public FormulaResolveUnifyVisitor(Environment env) {
		this.env = env;
	}

	@Override
	public Formula visitVar(FormulaVar formulaVar) {
		String name = formulaVar.name;
		if(name.equalsIgnoreCase("NaN") || name.equalsIgnoreCase("NA")) {
			return FormulaNum.NA;
		}
		if(ComputationOfTime.NON_DATA_VARIABLES_SET.contains(name)) {
			return new FormulaNonDataVar(name, formulaVar.positive).accept(this);
		}
		if(env.containsResolver(name)) {
			return env.resolve(name).withSign(formulaVar.positive).accept(this);
		}
		return formulaVar;
	}
}

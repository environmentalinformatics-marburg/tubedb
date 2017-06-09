package tsdb.dsl;

import tsdb.dsl.computation.ComputationOfTime;
import tsdb.dsl.formula.Formula;
import tsdb.dsl.formula.FormulaNoDataVar;
import tsdb.dsl.formula.FormulaVar;

public class FormulaResolveUnifyVisitor extends FormulaUnifyVisitor {	
	private final Environment env;

	public FormulaResolveUnifyVisitor(Environment env) {
		this.env = env;
	}

	@Override
	public Formula visitVar(FormulaVar formulaVar) {
		if(ComputationOfTime.NON_DATA_VARIABLES_SET.contains(formulaVar.name)) {
			return new FormulaNoDataVar(formulaVar.name, formulaVar.positive).accept(this);
		}
		if(env.containsResolver(formulaVar.name)) {
			return env.resolve(formulaVar.name).withSign(formulaVar.positive).accept(this);
		}
		return formulaVar;
	}
}

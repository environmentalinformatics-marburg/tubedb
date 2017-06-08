package tsdb.dsl;

public class FormulaResolveVisitor extends FormulaUnifyVisitor {	
	private final Environment env;

	public FormulaResolveVisitor(Environment env) {
		this.env = env;
	}

	@Override
	public Formula visitVar(FormulaVar formulaVar) {
		if(env.containsResolver(formulaVar.name)) {
			return env.resolve(formulaVar.name).withSign(formulaVar.positive).accept(this);
		}
		return formulaVar;
	}
}

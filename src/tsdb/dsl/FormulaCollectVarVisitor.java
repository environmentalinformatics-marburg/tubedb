package tsdb.dsl;

import java.util.LinkedHashSet;

import tsdb.dsl.formula.FormulaVar;

public class FormulaCollectVarVisitor extends FormulaTraverseVisitor<LinkedHashSet<String>> {
	
	private LinkedHashSet<String> vars = new LinkedHashSet<String>();

	@Override
	public LinkedHashSet<String> visitVar(FormulaVar f) {
		vars.add(f.name);
		return super.visitVar(f);		
	}

	@Override
	protected LinkedHashSet<String> result() {
		return vars;
	}
}

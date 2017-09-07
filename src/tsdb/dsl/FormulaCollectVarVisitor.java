package tsdb.dsl;

import java.util.LinkedHashSet;

import tsdb.dsl.formula.FormulaVar;

public class FormulaCollectVarVisitor extends FormulaTraverseVisitor<FormulaCollectVarVisitor> {
	
	private LinkedHashSet<String> vars = new LinkedHashSet<String>();

	@Override
	public FormulaCollectVarVisitor visitVar(FormulaVar f) {
		vars.add(f.name);
		return super.visitVar(f);		
	}

	@Override
	protected FormulaCollectVarVisitor result() {
		return this;
	}
	
	public String[] getVars() {
		return vars.toArray(new String[0]);
	}
	
	public int[] getDataVarIndices(Environment env) {
		return vars.stream().mapToInt(name -> env.getSensorIndex(name)).toArray();
	}
}

package tsdb.dsl;

import java.util.LinkedHashSet;

import tsdb.dsl.formula.FormulaConditional;
import tsdb.dsl.formula.FormulaVar;

public class FormulaCollectUnsafeVarVisitor extends FormulaTraverseVisitor<FormulaCollectUnsafeVarVisitor> {
	
	private UnsafeSectionVisitor unsafeSectionVisitor = new UnsafeSectionVisitor();
	
	private static class UnsafeSectionVisitor extends FormulaTraverseVisitor<Void> {
		public LinkedHashSet<String> vars = new LinkedHashSet<String>();
		@Override
		public Void visitVar(FormulaVar f) {
			vars.add(f.name);
			return null;		
		}
	}

	@Override
	public FormulaCollectUnsafeVarVisitor visitConditional(FormulaConditional f) {
		f.p.accept(unsafeSectionVisitor);
		f.a.accept(this);
		f.b.accept(this);		
		return this;
	}

	@Override
	protected FormulaCollectUnsafeVarVisitor result() {
		return this;
	}
	
	public String[] getVars() {
		return unsafeSectionVisitor.vars.toArray(new String[0]);
	}
	
	public int[] getDataVarIndices(Environment env) {
		return unsafeSectionVisitor.vars.stream().mapToInt(name -> env.getSensorIndex(name)).toArray();
	}
}

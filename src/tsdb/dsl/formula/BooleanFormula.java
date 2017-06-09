package tsdb.dsl.formula;

import java.util.Set;

import tsdb.dsl.BooleanFormulaVisitor1;
import tsdb.dsl.Environment;
import tsdb.dsl.computation.BooleanComputation;

public abstract class BooleanFormula {
	public abstract BooleanComputation compile(Environment env);
	public abstract String compileToString(Environment env);
	public abstract BooleanFormula not();
	public abstract void collectVariables(Set<String> collector, Environment env);
	public abstract <T> T accept(BooleanFormulaVisitor1<T> visitor);
	
	public static BooleanFormula of(boolean b) {
		return b?TRUE:FALSE;
	}
	
	public static final BooleanFormula TRUE = new BooleanFormula() {		
		@Override
		public BooleanFormula not() {
			return FALSE;
		}		
		@Override
		public String compileToString(Environment env) {
			return "(true)";
		}		
		@Override
		public BooleanComputation compile(Environment env) {
			return new BooleanComputation() {				
				@Override
				public boolean eval(long timestamp, float[] data) {
					return true;
				}
			};
		}		
		@Override
		public void collectVariables(Set<String> collector, Environment env) {
		}		
		@Override
		public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
			return visitor.visitTRUE(this);
		}
	};
	
	public static final BooleanFormula FALSE = new BooleanFormula() {		
		@Override
		public BooleanFormula not() {
			return TRUE;
		}
		@Override
		public String compileToString(Environment env) {
			return "(false)";
		}		
		@Override
		public BooleanComputation compile(Environment env) {
			return new BooleanComputation() {				
				@Override
				public boolean eval(long timestamp, float[] data) {
					return false;
				}
			};
		}		
		@Override
		public void collectVariables(Set<String> collector, Environment env) {
		}		
		@Override
		public <T> T accept(BooleanFormulaVisitor1<T> visitor) {
			return visitor.visitFALSE(this);
		}
	};
}

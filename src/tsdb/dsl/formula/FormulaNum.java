package tsdb.dsl.formula;

import java.util.Set;

import tsdb.dsl.Environment;
import tsdb.dsl.FormulaVisitor1;
import tsdb.dsl.computation.Computation;

public class FormulaNum extends Formula {
	public final float value;

	public static final FormulaNum ZERO = new FormulaNum(0f);
	public static final FormulaNum ONE = new FormulaNum(1f);
	public static final FormulaNum TWO = new FormulaNum(2f);

	private FormulaNum(float value) {
		this.value = value;
	}

	public static FormulaNum of(float value) {
		if(value == 0f) {
			return ZERO;
		}
		if(value == 1f) {
			return ONE;
		}
		if(value == 2f) {
			return TWO;
		}
		return new FormulaNum(value);
	}

	public static FormulaNum parse(String s) {
		return of(Float.parseFloat(s));
	}

	@Override
	public Computation compile(Environment env) {		
		return new Computation(){
			float v = value;
			@Override
			public float eval(long timestamp, float[] data) {
				return v;				
			}
		};

	}

	@Override
	public String compileToString(Environment env) {
		return Float.toString(value)+"f";
	}

	@Override
	public void collectDataVariables(Set<String> collector, Environment env) {
		// nothing		
	}	

	@Override
	public FormulaNum negative() {
		return FormulaNum.of(-value);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitNum(this);
	}
}

package tsdb.dsl.formula;

import ch.randelshofer.fastdoubleparser.JavaFloatParser;
import tsdb.dsl.FormulaVisitor1;

public class FormulaNum extends Formula {
	public final float value;

	public static final FormulaNum NA = new FormulaNum(Float.NaN);
	public static final FormulaNum ZERO = new FormulaNum(0f);
	public static final FormulaNum ONE = new FormulaNum(1f);
	public static final FormulaNum TWO = new FormulaNum(2f);
	public static final FormulaNum THREE = new FormulaNum(3f);
	public static final FormulaNum FOUR = new FormulaNum(4f);	
	public static final FormulaNum ONE_HALF = new FormulaNum(1f/2f);
	public static final FormulaNum ONE_THIRD = new FormulaNum(1f/3f);
	public static final FormulaNum ONE_QUARTER = new FormulaNum(1f/4f);

	private FormulaNum(float value) {
		this.value = value;
	}

	public static FormulaNum of(float value) {
		if(Float.isNaN(value)) {
			return NA;
		}
		if(value == 0f) {
			return ZERO;
		}
		if(value == 1f) {
			return ONE;
		}
		if(value == 2f) {
			return TWO;
		}
		if(value == 3f) {
			return THREE;
		}
		if(value == 4f) {
			return FOUR;
		}
		if(value == (1f/2f)) {
			return ONE_HALF;
		}
		if(value == (1f/3f)) {
			return ONE_THIRD;
		}
		if(value == (1f/4f)) {
			return ONE_QUARTER;
		}
		return new FormulaNum(value);
	}

	public static FormulaNum parse(String s) {
		//return of(Float.parseFloat(s));
		return of(JavaFloatParser.parseFloat(s));
	}

	@Override
	public FormulaNum negative() {
		return FormulaNum.of(-value);
	}

	@Override
	public <T> T accept(FormulaVisitor1<T> visitor) {
		return visitor.visitNum(this);
	}
	
	@Override
	public String toString() {
		return Float.toString(value);
	}	
}

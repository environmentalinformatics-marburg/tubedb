package tsdb.dsl;

import tsdb.dsl.formula.BooleanFormula;
import tsdb.dsl.formula.BooleanFormulaAND;
import tsdb.dsl.formula.BooleanFormulaEqual;
import tsdb.dsl.formula.BooleanFormulaLess;
import tsdb.dsl.formula.BooleanFormulaLessEqual;
import tsdb.dsl.formula.BooleanFormulaNotEqual;
import tsdb.dsl.formula.BooleanFormulaOR;
import tsdb.dsl.formula.Formula;
import tsdb.dsl.formula.FormulaAdd;
import tsdb.dsl.formula.FormulaConditional;
import tsdb.dsl.formula.FormulaDiv;
import tsdb.dsl.formula.FormulaFunc;
import tsdb.dsl.formula.FormulaMul;
import tsdb.dsl.formula.FormulaNoDataVar;
import tsdb.dsl.formula.FormulaNum;
import tsdb.dsl.formula.FormulaPow;
import tsdb.dsl.formula.FormulaSub;
import tsdb.dsl.formula.FormulaVar;

public class FormulaUnifyVisitor implements FormulaVisitor1<Formula>, BooleanFormulaVisitor1<BooleanFormula> {

	@Override
	public BooleanFormula visitTRUE(BooleanFormula booleanFormulaTRUE) {
		return booleanFormulaTRUE;
	}

	@Override
	public BooleanFormula visitFALSE(BooleanFormula booleanFormulaFALSE) {
		return booleanFormulaFALSE;
	}

	@Override
	public BooleanFormula visitAND(BooleanFormulaAND booleanFormulaAND) {
		BooleanFormula a = booleanFormulaAND.a.accept(this);
		BooleanFormula b = booleanFormulaAND.b.accept(this);
		if(a == BooleanFormula.FALSE || b == BooleanFormula.FALSE) {
			return BooleanFormula.FALSE.accept(this);
		}
		if(a == BooleanFormula.TRUE) {
			return b;
		}
		if(b == BooleanFormula.TRUE) {
			return a;
		}
		if(a != booleanFormulaAND.a || b != booleanFormulaAND.b) {
			return new BooleanFormulaAND(a, b);
		}
		return booleanFormulaAND;
	}

	@Override
	public BooleanFormula visitOR(BooleanFormulaOR booleanFormulaOR) {
		BooleanFormula a = booleanFormulaOR.a.accept(this);
		BooleanFormula b = booleanFormulaOR.b.accept(this);
		if(a == BooleanFormula.TRUE || b == BooleanFormula.TRUE) {
			return BooleanFormula.TRUE.accept(this);
		}
		if(a == BooleanFormula.FALSE) {
			return b;
		}
		if(b == BooleanFormula.FALSE) {
			return a;
		}
		if(a != booleanFormulaOR.a || b != booleanFormulaOR.b) {
			return new BooleanFormulaOR(a, b);
		}
		return booleanFormulaOR;
	}

	@Override
	public BooleanFormula visitEqual(BooleanFormulaEqual booleanFormulaEqual) {
		Formula a = booleanFormulaEqual.a.accept(this);
		Formula b = booleanFormulaEqual.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			return BooleanFormula.of(((FormulaNum)a).value == ((FormulaNum)b).value).accept(this);
		}
		if(a != booleanFormulaEqual.a || b != booleanFormulaEqual.b) {
			return new BooleanFormulaEqual(a, b);
		}
		return booleanFormulaEqual;
	}

	@Override
	public BooleanFormula visitLess(BooleanFormulaLess booleanFormulaLess) {
		Formula a = booleanFormulaLess.a.accept(this);
		Formula b = booleanFormulaLess.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			return BooleanFormula.of(((FormulaNum)a).value < ((FormulaNum)b).value).accept(this);
		}
		if(a != booleanFormulaLess.a || b != booleanFormulaLess.b) {
			return new BooleanFormulaLess(a, b);
		}
		return booleanFormulaLess;
	}

	@Override
	public BooleanFormula visitLessEqual(BooleanFormulaLessEqual booleanFormulaLessEqual) {
		Formula a = booleanFormulaLessEqual.a.accept(this);
		Formula b = booleanFormulaLessEqual.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			return BooleanFormula.of(((FormulaNum)a).value <= ((FormulaNum)b).value).accept(this);
		}
		if(a != booleanFormulaLessEqual.a || b != booleanFormulaLessEqual.b) {
			return new BooleanFormulaLessEqual(a, b);
		}
		return booleanFormulaLessEqual;
	}

	@Override
	public BooleanFormula visitNotEqual(BooleanFormulaNotEqual booleanFormulaNotEqual) {
		Formula a = booleanFormulaNotEqual.a.accept(this);
		Formula b = booleanFormulaNotEqual.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			return BooleanFormula.of(((FormulaNum)a).value != ((FormulaNum)b).value).accept(this);
		}
		if(a != booleanFormulaNotEqual.a || b != booleanFormulaNotEqual.b) {
			return new BooleanFormulaNotEqual(a, b);
		}
		return booleanFormulaNotEqual;
	}

	@Override
	public Formula visitAdd(FormulaAdd formulaAdd) {
		Formula a = formulaAdd.a.accept(this);
		Formula b = formulaAdd.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			float x = ((FormulaNum)a).value;
			float y = ((FormulaNum)b).value;
			return FormulaNum.of(x + y).accept(this);
		}
		if(a == FormulaNum.ZERO) {
			return b;
		}
		if(b == FormulaNum.ZERO) {
			return a;
		}
		if(a != formulaAdd.a || b != formulaAdd.b) {
			return new FormulaAdd(a, b);
		}
		return formulaAdd;
	}

	@Override
	public Formula visitSub(FormulaSub formulaSub) {
		Formula a = formulaSub.a.accept(this);
		Formula b = formulaSub.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			float x = ((FormulaNum)a).value;
			float y = ((FormulaNum)b).value;
			return FormulaNum.of(x - y).accept(this);
		}
		if(a == FormulaNum.ZERO) {
			// TODO
		}
		if(b == FormulaNum.ZERO) {
			return a;
		}
		if(a != formulaSub.a || b != formulaSub.b) {
			return new FormulaSub(a, b);
		}
		return formulaSub;
	}

	@Override
	public Formula visitMul(FormulaMul formulaMul) {
		Formula a = formulaMul.a.accept(this);
		Formula b = formulaMul.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			float x = ((FormulaNum)a).value;
			float y = ((FormulaNum)b).value;
			return FormulaNum.of(x * y).accept(this);
		}
		if(a == FormulaNum.ZERO) {
			return FormulaNum.ZERO.accept(this);
		}
		if(b == FormulaNum.ZERO) {
			return FormulaNum.ZERO.accept(this);
		}
		if(a == FormulaNum.ONE) {
			return b;
		}
		if(b == FormulaNum.ONE) {
			return a;
		}
		if(a != formulaMul.a || b != formulaMul.b) {
			return new FormulaMul(a, b);
		}
		return formulaMul;
	}

	@Override
	public Formula visitDiv(FormulaDiv formulaDiv) {
		Formula a = formulaDiv.a.accept(this);
		Formula b = formulaDiv.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			float x = ((FormulaNum)a).value;
			float y = ((FormulaNum)b).value;
			return FormulaNum.of(x / y).accept(this);
		}
		if(a == FormulaNum.ZERO) { // 0 / b  ==> 0 , 0 / 0 ==> NaN
			// TODO
		}
		if(b == FormulaNum.ZERO) { // a / 0  ==> +-Infinity , 0 / 0 ==> NaN
			// TODO
		}
		if(a == FormulaNum.ONE) { // 1 / b
			// TODO
		}
		if(b == FormulaNum.ONE) { // a / 1 ==> a
			return a;
		}
		if(a != formulaDiv.a || b != formulaDiv.b) {
			return new FormulaDiv(a, b);
		}
		return formulaDiv;
	}

	@Override
	public Formula visitPow(FormulaPow formulaPow) {
		Formula a = formulaPow.a.accept(this);
		Formula b = formulaPow.b.accept(this);
		if(a instanceof FormulaNum && b instanceof FormulaNum) {
			float x = ((FormulaNum)a).value;
			float y = ((FormulaNum)b).value;
			return FormulaNum.of((float) Math.pow(x, y)).accept(this);
		}
		if(a == FormulaNum.ZERO) { // 0 ^ a
			// TODO
		}
		if(b == FormulaNum.ZERO) { // a ^ 0  ==> 1
			return FormulaNum.ONE.accept(this);
		}
		if(a == FormulaNum.ONE) { // 1 ^ b ==> 1
			return FormulaNum.ONE.accept(this);
		}
		if(b == FormulaNum.ONE) { // a ^ 1 ==> a
			return a;
		}
		if(a != formulaPow.a || b != formulaPow.b) {
			return new FormulaPow(a, b);
		}
		return formulaPow;
	}

	@Override
	public Formula visitFunc(FormulaFunc formulaFunc) {
		Formula parameter = formulaFunc.parameter.accept(this);
		if(parameter instanceof FormulaNum) {
			float x = ((FormulaNum)parameter).value;
			switch(formulaFunc.name) {
			case "exp":
				return FormulaNum.of((float) Math.exp(x)).withSign(formulaFunc.positive).accept(this);
			case "ln":
				return FormulaNum.of((float) Math.log(x)).withSign(formulaFunc.positive).accept(this);
			}
		}
		if(parameter != formulaFunc.parameter) {
			return new FormulaFunc(formulaFunc.name, parameter, formulaFunc.positive);
		}
		return formulaFunc;
	}

	@Override
	public Formula visitConditional(FormulaConditional formulaConditional) {
		BooleanFormula p = formulaConditional.p.accept(this);
		Formula a = formulaConditional.a.accept(this);
		Formula b = formulaConditional.b.accept(this);
		if(p == BooleanFormula.TRUE) {
			return a;
		}
		if(p == BooleanFormula.FALSE) {
			return b;
		}
		if(p != formulaConditional.p || a != formulaConditional.a || b != formulaConditional.b) {
			return new FormulaConditional(p, a, b);
		}
		return formulaConditional;
	}

	@Override
	public Formula visitVar(FormulaVar formulaVar) {
		return formulaVar;
	}
	
	@Override
	public Formula visitNoDataVar(FormulaNoDataVar formulaNoDataVar) {
		return formulaNoDataVar;
	}

	@Override
	public Formula visitNum(FormulaNum formulaNum) {
		return formulaNum;
	}
}

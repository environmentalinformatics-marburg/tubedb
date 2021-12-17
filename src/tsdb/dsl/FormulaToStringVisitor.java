package tsdb.dsl;


import org.tinylog.Logger;

import tsdb.dsl.formula.BooleanFormula;
import tsdb.dsl.formula.BooleanFormulaAND;
import tsdb.dsl.formula.BooleanFormulaEqual;
import tsdb.dsl.formula.BooleanFormulaLess;
import tsdb.dsl.formula.BooleanFormulaLessEqual;
import tsdb.dsl.formula.BooleanFormulaNotEqual;
import tsdb.dsl.formula.BooleanFormulaOR;
import tsdb.dsl.formula.FormulaAdd;
import tsdb.dsl.formula.FormulaConditional;
import tsdb.dsl.formula.FormulaDiv;
import tsdb.dsl.formula.FormulaFunc;
import tsdb.dsl.formula.FormulaMul;
import tsdb.dsl.formula.FormulaNonDataVar;
import tsdb.dsl.formula.FormulaNum;
import tsdb.dsl.formula.FormulaPow;
import tsdb.dsl.formula.FormulaSub;
import tsdb.dsl.formula.FormulaVar;

public class FormulaToStringVisitor implements FormulaVisitor1<String>, BooleanFormulaVisitor1<String>  {
	public final static FormulaToStringVisitor DEFAULT = new FormulaToStringVisitor();
	
	

	@Override
	public String visitAND(BooleanFormulaAND booleanFormulaAND) {
		String ja = booleanFormulaAND.a.accept(this);
		String jb = booleanFormulaAND.b.accept(this);
		return "("+ja+" && "+jb+")";
	}

	@Override
	public String visitOR(BooleanFormulaOR booleanFormulaOR) {
		String ja = booleanFormulaOR.a.accept(this);
		String jb = booleanFormulaOR.b.accept(this);
		return "("+ja+" || "+jb+")";
	}

	@Override
	public String visitEqual(BooleanFormulaEqual booleanFormulaEqual) {
		String ja = booleanFormulaEqual.a.accept(this);
		String jb = booleanFormulaEqual.b.accept(this);
		return "("+ja+" == "+jb+")";
	}

	@Override
	public String visitLess(BooleanFormulaLess booleanFormulaLess) {
		String ja = booleanFormulaLess.a.accept(this);
		String jb = booleanFormulaLess.b.accept(this);
		return "("+ja+" < "+jb+")";
	}

	@Override
	public String visitLessEqual(BooleanFormulaLessEqual booleanFormulaLessEqual) {
		String ja = booleanFormulaLessEqual.a.accept(this);
		String jb = booleanFormulaLessEqual.b.accept(this);
		return "("+ja+" <= "+jb+")";
	}

	@Override
	public String visitNotEqual(BooleanFormulaNotEqual booleanFormulaNotEqual) {
		String ja = booleanFormulaNotEqual.a.accept(this);
		String jb = booleanFormulaNotEqual.b.accept(this);
		return "("+ja+" != "+jb+")";
	}

	@Override
	public String visitTRUE(BooleanFormula booleanFormula) {
		return "true";
	}

	@Override
	public String visitFALSE(BooleanFormula booleanFormula) {
		return "false";
	}

	@Override
	public String visitAdd(FormulaAdd formulaAdd) {
		String ja = formulaAdd.a.accept(this);
		String jb = formulaAdd.b.accept(this);
		return "("+ja+" + "+jb+")";
	}

	@Override
	public String visitSub(FormulaSub formulaSub) {
		String ja = formulaSub.a.accept(this);
		String jb = formulaSub.b.accept(this);
		return "("+ja+" - "+jb+")";
	}

	@Override
	public String visitMul(FormulaMul formulaMul) {
		String ja = formulaMul.a.accept(this);
		String jb = formulaMul.b.accept(this);
		return "("+ja+" * "+jb+")";
	}

	@Override
	public String visitDiv(FormulaDiv formulaDiv) {
		String ja = formulaDiv.a.accept(this);
		String jb = formulaDiv.b.accept(this);
		return "("+ja+" / "+jb+")";
	}

	@Override
	public String visitPow(FormulaPow formulaPow) {
		String ja = formulaPow.a.accept(this);
		String jb = formulaPow.b.accept(this);
		return "("+ja+" ^ "+jb+")";
	}

	@Override
	public String visitFunc(FormulaFunc formulaFunc) {
		String p = formulaFunc.parameter.accept(this);
		if(formulaFunc.positive) {
			return formulaFunc.name + "(" + p + ")";
		} else {
			return "-" + formulaFunc.name + "(" + p + ")";
		}
	}

	@Override
	public String visitConditional(FormulaConditional formulaConditional) {
		String jp = formulaConditional.p.accept(this);
		String ja = formulaConditional.a.accept(this);
		String jb = formulaConditional.b.accept(this);
		return "("+jp+" ? "+ja+" : "+jb+")";
	}

	@Override
	public String visitVar(FormulaVar formulaVar) {
		if(formulaVar.positive) {
			return formulaVar.name;
		} else {
			return "-" + formulaVar.name;
		}
	}

	@Override
	public String visitNum(FormulaNum formulaNum) {
		return Float.toString(formulaNum.value);
	}

	@Override
	public String visitNonDataVar(FormulaNonDataVar formulaNoDataVar) {
		if(formulaNoDataVar.positive) {
			return formulaNoDataVar.name;
		} else {
			return "-" + formulaNoDataVar.name;
		}
	}
}

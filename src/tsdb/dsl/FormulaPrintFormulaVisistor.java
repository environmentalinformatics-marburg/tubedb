package tsdb.dsl;

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
import tsdb.dsl.printformula.PrintFormula;
import tsdb.dsl.printformula.PrintFormulaAdd;
import tsdb.dsl.printformula.PrintFormulaAddOp;
import tsdb.dsl.printformula.PrintFormulaConst;
import tsdb.dsl.printformula.PrintFormulaDiv;
import tsdb.dsl.printformula.PrintFormulaFunc;
import tsdb.dsl.printformula.PrintFormulaIf;
import tsdb.dsl.printformula.PrintFormulaMul;
import tsdb.dsl.printformula.PrintFormulaPow;
import tsdb.dsl.printformula.PrintFormulaVar;
import tsdb.dsl.printformula.PrintPredFormula;
import tsdb.dsl.printformula.PrintPredFormulaAnd;
import tsdb.dsl.printformula.PrintPredFormulaConst;
import tsdb.dsl.printformula.PrintPredFormulaOr;
import tsdb.dsl.printformula.PrintPredFormulaRel;

public class FormulaPrintFormulaVisistor implements FormulaVisitor1<PrintFormula>, BooleanFormulaVisitor1<PrintPredFormula> {
	public static final FormulaPrintFormulaVisistor DEFAULT = new FormulaPrintFormulaVisistor();

	@Override
	public PrintFormula visitAdd(FormulaAdd formulaAdd) {
		PrintFormula a = formulaAdd.a.accept(this);
		PrintFormula b = formulaAdd.b.accept(this);
		if(a instanceof PrintFormulaAdd) {
			PrintFormulaAdd pa = (PrintFormulaAdd) a;
			if(b instanceof PrintFormulaAdd) {
				PrintFormulaAdd pb = (PrintFormulaAdd) b;
				return new PrintFormulaAdd(PrintFormulaAddOp.concat(pa.terms, pb.terms));
			} else {
				return new PrintFormulaAdd(PrintFormulaAddOp.concat(pa.terms, new PrintFormulaAddOp(true, b)));
			}
		} else if(b instanceof PrintFormulaAdd) {
			PrintFormulaAdd pb = (PrintFormulaAdd) b;
			return new PrintFormulaAdd(PrintFormulaAddOp.concat(new PrintFormulaAddOp(true, a), pb.terms));
		} else {
			return new PrintFormulaAdd(PrintFormulaAddOp.concat(new PrintFormulaAddOp(true, a), new PrintFormulaAddOp(true, b)));
		}
	}

	@Override
	public PrintFormula visitSub(FormulaSub formulaSub) {
		PrintFormula a = formulaSub.a.accept(this);
		PrintFormula b = formulaSub.b.accept(this);
		if(a instanceof PrintFormulaAdd) {
			PrintFormulaAdd pa = (PrintFormulaAdd) a;
			if(b instanceof PrintFormulaAdd) {
				PrintFormulaAdd pb = (PrintFormulaAdd) b;
				return new PrintFormulaAdd(PrintFormulaAddOp.concat(pa.terms, PrintFormulaAddOp.negate(pb.terms)));
			} else {
				return new PrintFormulaAdd(PrintFormulaAddOp.concat(pa.terms, new PrintFormulaAddOp(false, b)));
			}
		} else if(b instanceof PrintFormulaAdd) {
			PrintFormulaAdd pb = (PrintFormulaAdd) b;
			return new PrintFormulaAdd(PrintFormulaAddOp.concat(new PrintFormulaAddOp(true, a), PrintFormulaAddOp.negate(pb.terms)));
		} else {
			return new PrintFormulaAdd(PrintFormulaAddOp.concat(new PrintFormulaAddOp(true, a), new PrintFormulaAddOp(false, b)));
		}
	}

	@Override
	public PrintFormula visitMul(FormulaMul formulaMul) {
		PrintFormula a = formulaMul.a.accept(this);
		PrintFormula b = formulaMul.b.accept(this);
		if(a instanceof PrintFormulaMul) {
			PrintFormulaMul pa = (PrintFormulaMul) a;
			if(b instanceof PrintFormulaMul) {
				PrintFormulaMul pb = (PrintFormulaMul) b;
				return new PrintFormulaMul(PrintFormula.concat(pa.factors, pb.factors));
			} else {
				return new PrintFormulaMul(PrintFormula.concat(pa.factors, b));
			}
		} else if(b instanceof PrintFormulaMul) {
			PrintFormulaMul pb = (PrintFormulaMul) b;
			return new PrintFormulaMul(PrintFormula.concat(a, pb.factors));
		} else {
			return new PrintFormulaMul(PrintFormula.concat(a, b));
		}
	}

	@Override
	public PrintFormula visitDiv(FormulaDiv formulaDiv) {
		PrintFormula a = formulaDiv.a.accept(this);
		PrintFormula b = formulaDiv.b.accept(this);
		return new PrintFormulaDiv(a, b);
	}

	@Override
	public PrintFormula visitPow(FormulaPow formulaPow) {
		PrintFormula a = formulaPow.a.accept(this);
		PrintFormula b = formulaPow.b.accept(this);
		return new PrintFormulaPow(a, b);
	}

	@Override
	public PrintFormula visitFunc(FormulaFunc formulaFunc) {
		PrintFormula parameter = formulaFunc.parameter.accept(this);
		return new PrintFormulaFunc(formulaFunc.name, parameter, formulaFunc.positive);
	}

	@Override
	public PrintFormula visitConditional(FormulaConditional formulaConditional) {		
		PrintPredFormula p = formulaConditional.p.accept(this);
		PrintFormula a = formulaConditional.a.accept(this);
		PrintFormula b = formulaConditional.b.accept(this);
		return new PrintFormulaIf(p, a, b);
	}

	@Override
	public PrintFormula visitVar(FormulaVar formulaVar) {
		return new PrintFormulaVar(formulaVar.positive, formulaVar.name);
	}

	@Override
	public PrintFormula visitNum(FormulaNum formulaNum) {
		return new PrintFormulaConst(formulaNum.value);
	}

	@Override
	public PrintFormula visitNonDataVar(FormulaNonDataVar formulaNoDataVar) {
		return new PrintFormulaVar(formulaNoDataVar.positive, formulaNoDataVar.name);
	}

	@Override
	public PrintPredFormula visitAND(BooleanFormulaAND booleanFormulaAND) {
		PrintPredFormula a = booleanFormulaAND.a.accept(this);
		PrintPredFormula b = booleanFormulaAND.b.accept(this);
		if(a instanceof PrintPredFormulaAnd) {
			PrintPredFormulaAnd pa = (PrintPredFormulaAnd) a;
			if(b instanceof PrintPredFormulaAnd) {
				PrintPredFormulaAnd pb = (PrintPredFormulaAnd) b;
				return new PrintPredFormulaAnd(PrintPredFormula.concat(pa.preds, pb.preds));
			} else {
				return new PrintPredFormulaAnd(PrintPredFormula.concat(pa.preds, b));
			}
		} else if(b instanceof PrintPredFormulaAnd) {
			PrintPredFormulaAnd pb = (PrintPredFormulaAnd) b;
			return new PrintPredFormulaAnd(PrintPredFormula.concat(a, pb.preds));
		} else {
			return new PrintPredFormulaAnd(PrintPredFormula.concat(a, b));
		}
	}

	public PrintPredFormula visitOR(BooleanFormulaOR booleanFormulaOR) {
		PrintPredFormula a = booleanFormulaOR.a.accept(this);
		PrintPredFormula b = booleanFormulaOR.b.accept(this);
		if(a instanceof PrintPredFormulaOr) {
			PrintPredFormulaOr pa = (PrintPredFormulaOr) a;
			if(b instanceof PrintPredFormulaOr) {
				PrintPredFormulaOr pb = (PrintPredFormulaOr) b;
				return new PrintPredFormulaOr(PrintPredFormula.concat(pa.preds, pb.preds));
			} else {
				return new PrintPredFormulaOr(PrintPredFormula.concat(pa.preds, b));
			}
		} else if(b instanceof PrintPredFormulaOr) {
			PrintPredFormulaOr pb = (PrintPredFormulaOr) b;
			return new PrintPredFormulaOr(PrintPredFormula.concat(a, pb.preds));
		} else {
			return new PrintPredFormulaOr(PrintPredFormula.concat(a, b));
		}
	}

	@Override
	public PrintPredFormula visitEqual(BooleanFormulaEqual booleanFormulaEqual) {
		PrintFormula a = booleanFormulaEqual.a.accept(this);
		PrintFormula b = booleanFormulaEqual.b.accept(this);
		return new PrintPredFormulaRel("=", a, b);
	}

	@Override
	public PrintPredFormula visitLess(BooleanFormulaLess booleanFormulaLess) {
		PrintFormula a = booleanFormulaLess.a.accept(this);
		PrintFormula b = booleanFormulaLess.b.accept(this);
		return new PrintPredFormulaRel("<", a, b);
	}

	@Override
	public PrintPredFormula visitLessEqual(BooleanFormulaLessEqual booleanFormulaLessEqual) {
		PrintFormula a = booleanFormulaLessEqual.a.accept(this);
		PrintFormula b = booleanFormulaLessEqual.b.accept(this);
		return new PrintPredFormulaRel("<=", a, b);
	}

	@Override
	public PrintPredFormula visitNotEqual(BooleanFormulaNotEqual booleanFormulaNotEqual) {
		PrintFormula a = booleanFormulaNotEqual.a.accept(this);
		PrintFormula b = booleanFormulaNotEqual.b.accept(this);
		return new PrintPredFormulaRel("!=", a, b);
	}

	@Override
	public PrintPredFormula visitTRUE(BooleanFormula booleanFormula) {
		return new PrintPredFormulaConst(true);
	}

	@Override
	public PrintPredFormula visitFALSE(BooleanFormula booleanFormula) {
		return new PrintPredFormulaConst(false);
	}
}

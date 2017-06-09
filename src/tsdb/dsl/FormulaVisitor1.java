package tsdb.dsl;

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

public interface FormulaVisitor1<T> {

	T visitAdd(FormulaAdd formulaAdd);
	T visitSub(FormulaSub formulaSub);
	T visitMul(FormulaMul formulaMul);
	T visitDiv(FormulaDiv formulaDiv);
	T visitPow(FormulaPow formulaPow);
	T visitFunc(FormulaFunc formulaFunc);
	T visitConditional(FormulaConditional formulaConditional);
	T visitVar(FormulaVar formulaVar);
	T visitNum(FormulaNum formulaNum);
	T visitNoDataVar(FormulaNoDataVar formulaNoDataVar);
	
}

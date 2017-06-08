package tsdb.dsl;

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

}

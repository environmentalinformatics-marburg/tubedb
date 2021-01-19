package tsdb.dsl.printformula;

public interface PrintFormulaVisitor<T> {

	T visitAdd(PrintFormulaAdd printFormulaAdd);
	T visitMul(PrintFormulaMul printFormulaMul);
	T visitDiv(PrintFormulaDiv printFormulaDiv);
	T visitPow(PrintFormulaPow printFormulaPow);
	T visitFunc(PrintFormulaFunc printFormulaFunc);
	T visitIf(PrintFormulaIf printFormulaIf);
	T visitVar(PrintFormulaVar printFormulaVar);
	T visitConst(PrintFormulaConst printFormulaNum);
	T visitPredAnd(PrintPredFormulaAnd printPredFormulaAnd);
	T visitPredOr(PrintPredFormulaOr printPredFormulaOr);
	T visitPredRel(PrintPredFormulaRel printPredFormulaRel);
	T visitPredConst(PrintPredFormulaConst printPredFormulaConst);
	
}

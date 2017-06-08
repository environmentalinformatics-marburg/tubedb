package tsdb.dsl;

public interface BooleanFormulaVisitor1<T> {

	T visitAND(BooleanFormulaAND booleanFormulaAND);
	T visitOR(BooleanFormulaOR booleanFormulaOR);
	T visitEqual(BooleanFormulaEqual booleanFormulaEqual);
	T visitLess(BooleanFormulaLess booleanFormulaLess);
	T visitLessEqual(BooleanFormulaLessEqual booleanFormulaLessEqual);
	T visitNotEqual(BooleanFormulaNotEqual booleanFormulaNotEqual);
	T visitTRUE(BooleanFormula booleanFormula);
	T visitFALSE(BooleanFormula booleanFormula);

}

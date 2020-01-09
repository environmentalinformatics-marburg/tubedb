package tsdb.dsl;

import tsdb.dsl.formula.BooleanFormula;
import tsdb.dsl.formula.BooleanFormulaAND;
import tsdb.dsl.formula.BooleanFormulaEqual;
import tsdb.dsl.formula.BooleanFormulaLess;
import tsdb.dsl.formula.BooleanFormulaLessEqual;
import tsdb.dsl.formula.BooleanFormulaNotEqual;
import tsdb.dsl.formula.BooleanFormulaOR;

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

package tsdb.dsl;

import tsdb.dsl.formula.BooleanFormula;
import tsdb.dsl.formula.BooleanFormulaAND;
import tsdb.dsl.formula.BooleanFormulaAtomicBinary;
import tsdb.dsl.formula.BooleanFormulaBinary;
import tsdb.dsl.formula.BooleanFormulaEqual;
import tsdb.dsl.formula.BooleanFormulaLess;
import tsdb.dsl.formula.BooleanFormulaLessEqual;
import tsdb.dsl.formula.BooleanFormulaNotEqual;
import tsdb.dsl.formula.BooleanFormulaOR;
import tsdb.dsl.formula.FormulaAdd;
import tsdb.dsl.formula.FormulaBinary;
import tsdb.dsl.formula.FormulaConditional;
import tsdb.dsl.formula.FormulaDiv;
import tsdb.dsl.formula.FormulaFunc;
import tsdb.dsl.formula.FormulaMul;
import tsdb.dsl.formula.FormulaNonDataVar;
import tsdb.dsl.formula.FormulaNum;
import tsdb.dsl.formula.FormulaPow;
import tsdb.dsl.formula.FormulaSub;
import tsdb.dsl.formula.FormulaVar;

public class FormulaTraverseVisitor<T> implements FormulaVisitor1<T>, BooleanFormulaVisitor1<T> {
	
	protected T result() {
		return null;
	}
	
	public T traverse(BooleanFormulaBinary f) {
		f.a.accept(this);
		f.b.accept(this);
		return result();
	}
	
	public T traverse(BooleanFormulaAtomicBinary f) {
		f.a.accept(this);
		f.b.accept(this);
		return result();
	}
	
	public T traverse(FormulaBinary f) {
		f.a.accept(this);
		f.b.accept(this);
		return result();
	}
	
	public T traverse(FormulaFunc f) {
		f.parameter.accept(this);
		return result();
	}
	
	public T traverse(FormulaConditional f) {
		f.p.accept(this);
		f.a.accept(this);
		f.b.accept(this);
		return result();
	}
	
	@Override
	public T visitTRUE(BooleanFormula f) {
		return result();
	}
	
	@Override
	public T visitFALSE(BooleanFormula f) {
		return result();
	}
	
	@Override
	public T visitNum(FormulaNum f) {
		return result();
	}
	
	@Override
	public T visitVar(FormulaVar f) {
		return result();
	}
	
	@Override
	public T visitNonDataVar(FormulaNonDataVar f) {
		return result();
	}
	

	@Override
	public T visitAND(BooleanFormulaAND f) {
		return traverse(f);
	}	
	@Override
	public T visitOR(BooleanFormulaOR f) {
		return traverse(f);
	}
	@Override
	public T visitEqual(BooleanFormulaEqual f) {
		return traverse(f);
	}
	@Override
	public T visitLess(BooleanFormulaLess f) {
		return traverse(f);
	}
	@Override
	public T visitLessEqual(BooleanFormulaLessEqual f) {
		return traverse(f);
	}
	@Override
	public T visitNotEqual(BooleanFormulaNotEqual f) {
		return traverse(f);
	}
	@Override
	public T visitAdd(FormulaAdd f) {
		return traverse(f);
	}
	@Override
	public T visitSub(FormulaSub f) {
		return traverse(f);
	}
	@Override
	public T visitMul(FormulaMul f) {
		return traverse(f);
	}
	@Override
	public T visitDiv(FormulaDiv f) {
		return traverse(f);
	}
	@Override
	public T visitPow(FormulaPow f) {
		return traverse(f);
	}
	@Override
	public T visitFunc(FormulaFunc f) {
		return traverse(f);
	}
	@Override
	public T visitConditional(FormulaConditional f) {
		return traverse(f);
	}
}

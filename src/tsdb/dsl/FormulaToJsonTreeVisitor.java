package tsdb.dsl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONWriter;

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

public class FormulaToJsonTreeVisitor implements FormulaVisitor1<Void>, BooleanFormulaVisitor1<Void>  {
	private static final Logger log = LogManager.getLogger();
	
	private final JSONWriter json;

	public FormulaToJsonTreeVisitor(JSONWriter json) {
		this.json = json;
	}

	@Override
	public Void visitAND(BooleanFormulaAND booleanFormulaAND) {
		json.object();
		json.key("&&");
		json.array();
		booleanFormulaAND.a.accept(this);
		booleanFormulaAND.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitOR(BooleanFormulaOR booleanFormulaOR) {
		json.object();
		json.key("||");
		json.array();
		booleanFormulaOR.a.accept(this);
		booleanFormulaOR.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitEqual(BooleanFormulaEqual booleanFormulaEqual) {
		json.object();
		json.key("==");
		json.array();
		booleanFormulaEqual.a.accept(this);
		booleanFormulaEqual.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitLess(BooleanFormulaLess booleanFormulaLess) {
		json.object();
		json.key("<");
		json.array();
		booleanFormulaLess.a.accept(this);
		booleanFormulaLess.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitLessEqual(BooleanFormulaLessEqual booleanFormulaLessEqual) {
		json.object();
		json.key("<=");
		json.array();
		booleanFormulaLessEqual.a.accept(this);
		booleanFormulaLessEqual.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitNotEqual(BooleanFormulaNotEqual booleanFormulaNotEqual) {
		json.object();
		json.key("!=");
		json.array();
		booleanFormulaNotEqual.a.accept(this);
		booleanFormulaNotEqual.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitTRUE(BooleanFormula booleanFormula) {
		json.value("true");
		return null;
	}

	@Override
	public Void visitFALSE(BooleanFormula booleanFormula) {
		json.value("false");
		return null;
	}

	@Override
	public Void visitAdd(FormulaAdd formulaAdd) {
		json.object();
		json.key("+");
		json.array();
		formulaAdd.a.accept(this);
		formulaAdd.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitSub(FormulaSub formulaSub) {
		json.object();
		json.key("-");
		json.array();
		formulaSub.a.accept(this);
		formulaSub.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitMul(FormulaMul formulaMul) {
		json.object();
		json.key("*");
		json.array();
		formulaMul.a.accept(this);
		formulaMul.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitDiv(FormulaDiv formulaDiv) {
		json.object();
		json.key("/");
		json.array();
		formulaDiv.a.accept(this);
		formulaDiv.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitPow(FormulaPow formulaPow) {
		json.object();
		json.key("^");
		json.array();
		formulaPow.a.accept(this);
		formulaPow.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitFunc(FormulaFunc formulaFunc) {
		json.object();
		json.key(formulaFunc.name);
		json.array();
		formulaFunc.parameter.accept(this);
		json.endArray();
		json.endObject();
		return null;		
	}

	@Override
	public Void visitConditional(FormulaConditional formulaConditional) {
		json.object();
		json.key("?");
		json.array();
		formulaConditional.p.accept(this);
		formulaConditional.a.accept(this);
		formulaConditional.b.accept(this);
		json.endArray();
		json.endObject();
		return null;
	}

	@Override
	public Void visitVar(FormulaVar formulaVar) {
		if(formulaVar.positive) {
			json.value(formulaVar.name);
		} else {
			json.value("-" + formulaVar.name);
		}
		return null;
	}

	@Override
	public Void visitNum(FormulaNum formulaNum) {
		json.value(formulaNum.value);
		return null;
	}

	@Override
	public Void visitNonDataVar(FormulaNonDataVar formulaNoDataVar) {
		if(formulaNoDataVar.positive) {
			json.value(formulaNoDataVar.name);
		} else {
			json.value("-" + formulaNoDataVar.name);
		}
		return null;
	}
}

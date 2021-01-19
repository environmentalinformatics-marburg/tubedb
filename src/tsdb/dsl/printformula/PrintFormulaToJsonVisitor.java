package tsdb.dsl.printformula;

import org.json.JSONWriter;

public class PrintFormulaToJsonVisitor implements PrintFormulaVisitor<Void>, PrintPredFormulaVisitor<Void> {
	
	private final JSONWriter json;

	public PrintFormulaToJsonVisitor(JSONWriter json) {
		this.json = json;
	}

	@Override
	public Void visitAdd(PrintFormulaAdd printFormulaAdd) {
		json.object();
		json.key("op");
		json.value("add");
		json.key("terms");
		json.array();
		for(PrintFormulaAddOp term:printFormulaAdd.terms) {
			json.object();
			json.key("term");
			term.a.accept(this);
			json.key("positive");
			json.value(term.positive);
			json.endObject();
		}
		json.endArray();
		json.key("depth");
		json.value(printFormulaAdd.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitMul(PrintFormulaMul printFormulaMul) {
		json.object();
		json.key("op");
		json.value("mul");
		json.key("factors");
		json.array();
		for(PrintFormula factor:printFormulaMul.factors) {
			factor.accept(this);
		}
		json.endArray();
		json.key("depth");
		json.value(printFormulaMul.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitDiv(PrintFormulaDiv printFormulaDiv) {
		json.object();
		json.key("op");
		json.value("div");
		json.key("a");
		printFormulaDiv.a.accept(this);
		json.key("b");
		printFormulaDiv.b.accept(this);
		json.key("depth");
		json.value(printFormulaDiv.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitPow(PrintFormulaPow printFormulaPow) {
		json.object();
		json.key("op");
		json.value("pow");
		json.key("a");
		printFormulaPow.a.accept(this);
		json.key("b");
		printFormulaPow.b.accept(this);
		json.key("depth");
		json.value(printFormulaPow.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitFunc(PrintFormulaFunc printFormulaFunc) {
		json.object();
		json.key("op");
		json.value("func");
		json.key("name");
		json.value(printFormulaFunc.name);
		json.key("param");
		printFormulaFunc.param.accept(this);
		json.key("depth");
		json.value(printFormulaFunc.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitIf(PrintFormulaIf printFormulaIf) {
		json.object();
		json.key("op");
		json.value("if");
		json.key("p");
		printFormulaIf.p.accept(this);
		json.key("a");
		printFormulaIf.a.accept(this);
		json.key("b");
		printFormulaIf.b.accept(this);		
		json.key("depth");
		json.value(printFormulaIf.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitVar(PrintFormulaVar printFormulaVar) {
		json.object();
		json.key("op");
		json.value("var");
		json.key("name");
		json.value(printFormulaVar.name);		
		json.key("depth");
		json.value(printFormulaVar.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitConst(PrintFormulaConst printFormulaConst) {
		json.object();
		json.key("op");
		json.value("const");
		json.key("value");
		String s = Float.toString(printFormulaConst.value);
		if(s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		json.value(s);		
		json.key("depth");
		json.value(printFormulaConst.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitPredAnd(PrintPredFormulaAnd printPredFormulaAnd) {
		json.object();
		json.key("pred_op");
		json.value("and");
		json.key("preds");
		json.array();
		for(PrintPredFormula pred:printPredFormulaAnd.preds) {
			pred.accept(this);
		}
		json.endArray();
		json.key("depth");
		json.value(printPredFormulaAnd.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitPredOr(PrintPredFormulaOr printPredFormulaOr) {
		json.object();
		json.key("pred_op");
		json.value("or");
		json.key("preds");
		json.array();
		for(PrintPredFormula pred:printPredFormulaOr.preds) {
			pred.accept(this);
		}
		json.endArray();
		json.key("depth");
		json.value(printPredFormulaOr.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitPredRel(PrintPredFormulaRel printPredFormulaRel) {
		json.object();
		json.key("pred_op");
		json.value("rel");
		json.key("name");
		json.value(printPredFormulaRel.name);
		json.key("a");
		printPredFormulaRel.a.accept(this);
		json.key("b");
		printPredFormulaRel.b.accept(this);
		json.key("depth");
		json.value(printPredFormulaRel.depth);
		json.endObject();
		return null;
	}

	@Override
	public Void visitPredConst(PrintPredFormulaConst printPredFormulaConst) {
		json.object();
		json.key("pred_op");
		json.value("const");
		json.key("value");
		json.value(printPredFormulaConst.value);		
		json.key("depth");
		json.value(printPredFormulaConst.depth);
		json.endObject();
		return null;
	}

}

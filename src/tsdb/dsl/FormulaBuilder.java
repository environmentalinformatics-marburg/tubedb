package tsdb.dsl;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import org.tinylog.Logger;

import tsdb.dsl.formula.Formula;

public class FormulaBuilder {
	
	
	public static Formula parseFormula(String formulaText) {
		if(formulaText==null || formulaText.trim().isEmpty()) {
			Logger.error("missing formula");
			return null;
		}		
		CodePointCharStream stream = CharStreams.fromString(formulaText, "formula");
		FormulaLexer lexer = new FormulaLexer(stream);	
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		FormulaParser parser = new FormulaParser(tokens);
		Formula formula = parser.expression().accept(FormulaASTVisitor.DEFAULT);
		return formula;
	}

}

package tsdb.component.labeledproperty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.dsl.FormulaBaseVisitor;
import tsdb.dsl.FormulaParser.EqualContext;
import tsdb.dsl.FormulaParser.GreaterContext;
import tsdb.dsl.FormulaParser.Greater_equalContext;
import tsdb.dsl.FormulaParser.LessContext;
import tsdb.dsl.FormulaParser.Less_equalContext;
import tsdb.dsl.FormulaParser.NotContext;
import tsdb.dsl.FormulaParser.Not_equalContext;

public class BooleanFormulaCompileVisitor extends FormulaBaseVisitor<BooleanFormula> {
	private static final Logger log = LogManager.getLogger();
	
	public static final BooleanFormulaCompileVisitor DEFAULT = new BooleanFormulaCompileVisitor();

	@Override
	public BooleanFormula visitLess(LessContext ctx) {
		Formula a = ctx.a.accept(FormulaCompileVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaCompileVisitor.DEFAULT);
		return new BooleanFormulaLess(a, b);
	}

	@Override
	public BooleanFormula visitGreater(GreaterContext ctx) {
		Formula a = ctx.a.accept(FormulaCompileVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaCompileVisitor.DEFAULT);
		return new BooleanFormulaLess(b, a);
	}

	@Override
	public BooleanFormula visitLess_equal(Less_equalContext ctx) {
		Formula a = ctx.a.accept(FormulaCompileVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaCompileVisitor.DEFAULT);
		return new BooleanFormulaLessEqual(a, b);
	}

	@Override
	public BooleanFormula visitGreater_equal(Greater_equalContext ctx) {
		Formula a = ctx.a.accept(FormulaCompileVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaCompileVisitor.DEFAULT);
		return new BooleanFormulaLessEqual(b, a);
	}

	@Override
	public BooleanFormula visitEqual(EqualContext ctx) {
		Formula a = ctx.a.accept(FormulaCompileVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaCompileVisitor.DEFAULT);
		return new BooleanFormulaEqual(a, b);
	}

	@Override
	public BooleanFormula visitNot_equal(Not_equalContext ctx) {
		Formula a = ctx.a.accept(FormulaCompileVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaCompileVisitor.DEFAULT);
		return new BooleanFormulaNotEqual(a, b);
	}

	@Override
	public BooleanFormula visitNot(NotContext ctx) {
		BooleanFormula a = ctx.a.accept(DEFAULT);
		return a.not();
	}
}

package tsdb.dsl.formula;

import org.antlr.v4.runtime.tree.ParseTree;

import org.tinylog.Logger;

import tsdb.dsl.FormulaASTVisitor;
import tsdb.dsl.FormulaBaseVisitor;
import tsdb.dsl.FormulaParser.EqualContext;
import tsdb.dsl.FormulaParser.GreaterContext;
import tsdb.dsl.FormulaParser.Greater_equalContext;
import tsdb.dsl.FormulaParser.LessContext;
import tsdb.dsl.FormulaParser.Less_equalContext;
import tsdb.dsl.FormulaParser.Not_equalContext;
import tsdb.dsl.FormulaParser.Predicate_atomContext;
import tsdb.dsl.FormulaParser.Predicate_expressionContext;
import tsdb.dsl.FormulaParser.Predicate_factorContext;
import tsdb.dsl.FormulaParser.Predicate_termContext;

public class BooleanFormulaCompileVisitor extends FormulaBaseVisitor<BooleanFormula> {
	
	
	public static final BooleanFormulaCompileVisitor DEFAULT = new BooleanFormulaCompileVisitor();
	
	@Override
	public BooleanFormula visitPredicate_expression(Predicate_expressionContext ctx) {
		if(ctx.getChildCount()==1) {
			return ctx.getChild(0).accept(DEFAULT);
		}
		ParseTree[] parseTrees = ctx.children.toArray(new ParseTree[0]);
		return create_OR_Expression(parseTrees.length-1, parseTrees);
	}

	private BooleanFormula create_OR_Expression(int pos, ParseTree[] parseTrees) {
		BooleanFormula f1 = parseTrees[pos].accept(DEFAULT);
		if(pos == 0) {
			return f1;
		}
		ParseTree op = parseTrees[pos-1];
		BooleanFormula f0 = create_OR_Expression(pos-2, parseTrees);
		return new BooleanFormulaOR(f0, f1);
	}
	
	@Override
	public BooleanFormula visitPredicate_term(Predicate_termContext ctx) {
		if(ctx.getChildCount()==1) {
			return ctx.getChild(0).accept(DEFAULT);
		}
		ParseTree[] parseTrees = ctx.children.toArray(new ParseTree[0]);
		return create_AND_Expression(parseTrees.length-1, parseTrees);
	}
	
	private BooleanFormula create_AND_Expression(int pos, ParseTree[] parseTrees) {
		BooleanFormula f1 = parseTrees[pos].accept(DEFAULT);
		if(pos == 0) {
			return f1;
		}
		ParseTree op = parseTrees[pos-1];
		BooleanFormula f0 = create_AND_Expression(pos-2, parseTrees);
		return new BooleanFormulaAND(f0, f1);
	}
	
	@Override
	public BooleanFormula visitPredicate_factor(Predicate_factorContext ctx) {
		BooleanFormula a = ctx.predicate_atom().accept(DEFAULT);
		if(ctx.not == null) {
			return a;
		} else {
			return a.not();
		}
	}
	
	@Override
	public BooleanFormula visitPredicate_atom(Predicate_atomContext ctx) {
		return ctx.getChild(0).accept(DEFAULT);
	}

	@Override
	public BooleanFormula visitLess(LessContext ctx) {
		Formula a = ctx.a.accept(FormulaASTVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaASTVisitor.DEFAULT);
		return new BooleanFormulaLess(a, b);
	}

	@Override
	public BooleanFormula visitGreater(GreaterContext ctx) {
		Formula a = ctx.a.accept(FormulaASTVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaASTVisitor.DEFAULT);
		return new BooleanFormulaLess(b, a);
	}

	@Override
	public BooleanFormula visitLess_equal(Less_equalContext ctx) {
		Formula a = ctx.a.accept(FormulaASTVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaASTVisitor.DEFAULT);
		return new BooleanFormulaLessEqual(a, b);
	}

	@Override
	public BooleanFormula visitGreater_equal(Greater_equalContext ctx) {
		Formula a = ctx.a.accept(FormulaASTVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaASTVisitor.DEFAULT);
		return new BooleanFormulaLessEqual(b, a);
	}

	@Override
	public BooleanFormula visitEqual(EqualContext ctx) {
		Formula a = ctx.a.accept(FormulaASTVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaASTVisitor.DEFAULT);
		return new BooleanFormulaEqual(a, b);
	}

	@Override
	public BooleanFormula visitNot_equal(Not_equalContext ctx) {
		Formula a = ctx.a.accept(FormulaASTVisitor.DEFAULT);
		Formula b = ctx.b.accept(FormulaASTVisitor.DEFAULT);
		return new BooleanFormulaNotEqual(a, b);
	}
}

package tsdb.dsl;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.dsl.FormulaParser.AtomContext;
import tsdb.dsl.FormulaParser.ConditionalContext;
import tsdb.dsl.FormulaParser.ExpressionContext;
import tsdb.dsl.FormulaParser.Expression_opContext;
import tsdb.dsl.FormulaParser.FactorContext;
import tsdb.dsl.FormulaParser.NumberContext;
import tsdb.dsl.FormulaParser.ScientificContext;
import tsdb.dsl.FormulaParser.TermContext;
import tsdb.dsl.FormulaParser.Term_opContext;
import tsdb.dsl.FormulaParser.VariableContext;
import tsdb.dsl.formula.BooleanFormula;
import tsdb.dsl.formula.BooleanFormulaCompileVisitor;
import tsdb.dsl.formula.Formula;
import tsdb.dsl.formula.FormulaAdd;
import tsdb.dsl.formula.FormulaConditional;
import tsdb.dsl.formula.FormulaDiv;
import tsdb.dsl.formula.FormulaFunc;
import tsdb.dsl.formula.FormulaMul;
import tsdb.dsl.formula.FormulaNum;
import tsdb.dsl.formula.FormulaPow;
import tsdb.dsl.formula.FormulaSub;
import tsdb.dsl.formula.FormulaVar;

public class FormulaASTVisitor extends FormulaBaseVisitor<Formula> {
	private static final Logger log = LogManager.getLogger();

	public static final FormulaASTVisitor DEFAULT = new FormulaASTVisitor();

	@Override
	public Formula visitVariable(VariableContext ctx) {
		String name = ctx.identifier().getText();
		if( ctx.parameter() == null) {
			return new FormulaVar(name, ctx.SUB() == null);
		} else {
			Formula parameter = ctx.parameter().expression().accept(DEFAULT);
			return new FormulaFunc(name, parameter, ctx.SUB() == null);
		}
	}

	@Override
	public Formula visitNumber(NumberContext ctx) {
		return FormulaNum.parse(ctx.getText());
	}

	@Override
	public Formula visitScientific(ScientificContext ctx) {
		return FormulaNum.parse(ctx.getText());
	}

	@Override
	public Formula visitAtom(AtomContext ctx) {
		if(ctx.getChildCount() == 1) {
			return ctx.getChild(0).accept(DEFAULT);
		}
		if(ctx.getChildCount() == 3) {
			ParseTree leftP = ctx.getChild(0);
			ParseTree content = ctx.getChild(1);
			ParseTree rightP = ctx.getChild(2);
			if(leftP.getText().equals("(") && rightP.getText().equals(")")) {
				return content.accept(DEFAULT);
			}
		}
		throw new RuntimeException("unknown input: "+ctx.getText());
	}	

	@Override
	public Formula visitFactor(FactorContext ctx) {
		if(ctx.POW() == null && ctx.getChildCount() == 1) {
			return ctx.getChild(0).accept(DEFAULT);
		}
		if(ctx.getChildCount() == 3) {
			Formula a = ctx.getChild(0).accept(DEFAULT);
			Formula b = ctx.getChild(2).accept(DEFAULT);
			return new FormulaPow(a, b);
		}
		throw new RuntimeException("unknown input: "+ctx.getText());
	}

	@Override
	public Formula visitTerm(TermContext ctx) {
		if(ctx.getChildCount()==1) {
			return ctx.getChild(0).accept(DEFAULT);
		}
		ParseTree[] parseTrees = ctx.children.toArray(new ParseTree[0]);
		return createTerm(parseTrees.length-1, parseTrees);
	}

	private class FormulaTermVisitor extends FormulaBaseVisitor<Formula> {
		public final Formula a;
		public final Formula b;
		public FormulaTermVisitor(Formula a, Formula b) {
			this.a = a;
			this.b = b;
		}
		@Override
		public Formula visitTerm_op(Term_opContext ctx) {
			if(ctx.MUL() != null) {
				return new FormulaMul(a, b);
			}
			if(ctx.DIV() != null) {
				return new FormulaDiv(a, b);
			}
			throw new RuntimeException("unknown input: "+ctx.getText());
		}
	}

	public Formula createTerm(int pos, ParseTree[] parseTrees) {
		Formula f1 = parseTrees[pos].accept(DEFAULT);
		if(pos == 0) {
			return f1;
		}
		ParseTree op = parseTrees[pos-1];
		Formula f0 = createTerm(pos-2, parseTrees);
		return op.accept(new FormulaTermVisitor(f0, f1));
	}

	@Override
	public Formula visitExpression(ExpressionContext ctx) {
		if(ctx.getChildCount()==1) {
			return ctx.getChild(0).accept(DEFAULT);
		}
		ParseTree[] parseTrees = ctx.children.toArray(new ParseTree[0]);
		return createExpression(parseTrees.length-1, parseTrees);
	}

	private class FormulaExpressionVisitor extends FormulaBaseVisitor<Formula> {
		public final Formula a;
		public final Formula b;
		public FormulaExpressionVisitor(Formula a, Formula b) {
			this.a = a;
			this.b = b;
		}
		@Override
		public Formula visitExpression_op(Expression_opContext ctx) {
			if(ctx.ADD() != null) {
				return new FormulaAdd(a, b);
			}
			if(ctx.SUB() != null) {
				return new FormulaSub(a, b);
			}
			throw new RuntimeException("unknown input: "+ctx.getText());
		}
	}

	public Formula createExpression(int pos, ParseTree[] parseTrees) {
		Formula f1 = parseTrees[pos].accept(DEFAULT);
		if(pos == 0) {
			return f1;
		}
		ParseTree op = parseTrees[pos-1];
		Formula f0 = createExpression(pos-2, parseTrees);
		return op.accept(new FormulaExpressionVisitor(f0, f1));
	}

	@Override
	public Formula visitConditional(ConditionalContext ctx) {
		BooleanFormula p = ctx.p.accept(BooleanFormulaCompileVisitor.DEFAULT);
		Formula a = ctx.a.accept(DEFAULT);
		Formula b = ctx.b.accept(DEFAULT);
		return new FormulaConditional(p, a, b);
	}
}

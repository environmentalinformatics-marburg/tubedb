// Generated from Formula.g4 by ANTLR 4.7
package tsdb.dsl;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FormulaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FormulaVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FormulaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(FormulaParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#expression_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_op(FormulaParser.Expression_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#conditional}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional(FormulaParser.ConditionalContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(FormulaParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#less}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLess(FormulaParser.LessContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#greater}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGreater(FormulaParser.GreaterContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#less_equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLess_equal(FormulaParser.Less_equalContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#greater_equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGreater_equal(FormulaParser.Greater_equalContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqual(FormulaParser.EqualContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#not_equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot_equal(FormulaParser.Not_equalContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot(FormulaParser.NotContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(FormulaParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#term_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_op(FormulaParser.Term_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(FormulaParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(FormulaParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#scientific}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScientific(FormulaParser.ScientificContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(FormulaParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(FormulaParser.VariableContext ctx);
}
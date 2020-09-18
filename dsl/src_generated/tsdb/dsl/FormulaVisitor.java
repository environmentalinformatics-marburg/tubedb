// Generated from Formula.g4 by ANTLR 4.4
package tsdb.dsl;
import org.antlr.v4.runtime.misc.NotNull;
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
	 * Visit a parse tree produced by {@link FormulaParser#expression_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_op(@NotNull FormulaParser.Expression_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(@NotNull FormulaParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(@NotNull FormulaParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#predicate_factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate_factor(@NotNull FormulaParser.Predicate_factorContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#conditional}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditional(@NotNull FormulaParser.ConditionalContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#predicate_term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate_term(@NotNull FormulaParser.Predicate_termContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#scientific}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScientific(@NotNull FormulaParser.ScientificContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#less}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLess(@NotNull FormulaParser.LessContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#greater_equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGreater_equal(@NotNull FormulaParser.Greater_equalContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqual(@NotNull FormulaParser.EqualContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#term_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_op(@NotNull FormulaParser.Term_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(@NotNull FormulaParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#predicate_atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate_atom(@NotNull FormulaParser.Predicate_atomContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#not_equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot_equal(@NotNull FormulaParser.Not_equalContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(@NotNull FormulaParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(@NotNull FormulaParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(@NotNull FormulaParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#predicate_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate_expression(@NotNull FormulaParser.Predicate_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(@NotNull FormulaParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(@NotNull FormulaParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#greater}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGreater(@NotNull FormulaParser.GreaterContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser#less_equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLess_equal(@NotNull FormulaParser.Less_equalContext ctx);
}
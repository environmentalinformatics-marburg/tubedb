// Generated from Formula.g4 by ANTLR 4.7
package tsdb.dsl;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FormulaParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		LPAREN=10, RPAREN=11, ADD=12, SUB=13, MUL=14, DIV=15, POINT=16, POW=17, 
		UNDERSCORE=18, LETTER=19, DIGIT=20, WS=21;
	public static final int
		RULE_expression = 0, RULE_expression_op = 1, RULE_conditional = 2, RULE_predicate = 3, 
		RULE_less = 4, RULE_greater = 5, RULE_less_equal = 6, RULE_greater_equal = 7, 
		RULE_equal = 8, RULE_not_equal = 9, RULE_not = 10, RULE_term = 11, RULE_term_op = 12, 
		RULE_factor = 13, RULE_atom = 14, RULE_scientific = 15, RULE_number = 16, 
		RULE_variable = 17;
	public static final String[] ruleNames = {
		"expression", "expression_op", "conditional", "predicate", "less", "greater", 
		"less_equal", "greater_equal", "equal", "not_equal", "not", "term", "term_op", 
		"factor", "atom", "scientific", "number", "variable"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'?'", "':'", "'<'", "'<='", "'>='", "'='", "'!='", "'<>'", "'!'", 
		"'('", "')'", "'+'", "'-'", "'*'", "'/'", "'.'", "'^'", "'_'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, "LPAREN", 
		"RPAREN", "ADD", "SUB", "MUL", "DIV", "POINT", "POW", "UNDERSCORE", "LETTER", 
		"DIGIT", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Formula.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public FormulaParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ExpressionContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<Expression_opContext> expression_op() {
			return getRuleContexts(Expression_opContext.class);
		}
		public Expression_opContext expression_op(int i) {
			return getRuleContext(Expression_opContext.class,i);
		}
		public ConditionalContext conditional() {
			return getRuleContext(ConditionalContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expression);
		int _la;
		try {
			setState(46);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(36);
				term();
				setState(42);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ADD || _la==SUB) {
					{
					{
					setState(37);
					expression_op();
					setState(38);
					term();
					}
					}
					setState(44);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(45);
				conditional();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Expression_opContext extends ParserRuleContext {
		public TerminalNode ADD() { return getToken(FormulaParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(FormulaParser.SUB, 0); }
		public Expression_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression_op; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitExpression_op(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_opContext expression_op() throws RecognitionException {
		Expression_opContext _localctx = new Expression_opContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_expression_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			_la = _input.LA(1);
			if ( !(_la==ADD || _la==SUB) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionalContext extends ParserRuleContext {
		public PredicateContext p;
		public ExpressionContext a;
		public ExpressionContext b;
		public TerminalNode LPAREN() { return getToken(FormulaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(FormulaParser.RPAREN, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ConditionalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditional; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitConditional(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionalContext conditional() throws RecognitionException {
		ConditionalContext _localctx = new ConditionalContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_conditional);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			match(LPAREN);
			setState(51);
			((ConditionalContext)_localctx).p = predicate();
			setState(52);
			match(T__0);
			setState(53);
			((ConditionalContext)_localctx).a = expression();
			setState(54);
			match(T__1);
			setState(55);
			((ConditionalContext)_localctx).b = expression();
			setState(56);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public LessContext less() {
			return getRuleContext(LessContext.class,0);
		}
		public GreaterContext greater() {
			return getRuleContext(GreaterContext.class,0);
		}
		public Less_equalContext less_equal() {
			return getRuleContext(Less_equalContext.class,0);
		}
		public Greater_equalContext greater_equal() {
			return getRuleContext(Greater_equalContext.class,0);
		}
		public EqualContext equal() {
			return getRuleContext(EqualContext.class,0);
		}
		public Not_equalContext not_equal() {
			return getRuleContext(Not_equalContext.class,0);
		}
		public NotContext not() {
			return getRuleContext(NotContext.class,0);
		}
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_predicate);
		try {
			setState(65);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				less();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(59);
				greater();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(60);
				less_equal();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(61);
				greater_equal();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(62);
				equal();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(63);
				not_equal();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(64);
				not();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LessContext extends ParserRuleContext {
		public ExpressionContext a;
		public ExpressionContext b;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public LessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_less; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitLess(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LessContext less() throws RecognitionException {
		LessContext _localctx = new LessContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_less);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			((LessContext)_localctx).a = expression();
			setState(68);
			match(T__2);
			setState(69);
			((LessContext)_localctx).b = expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GreaterContext extends ParserRuleContext {
		public ExpressionContext a;
		public ExpressionContext b;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public GreaterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_greater; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitGreater(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GreaterContext greater() throws RecognitionException {
		GreaterContext _localctx = new GreaterContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_greater);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			((GreaterContext)_localctx).a = expression();
			setState(72);
			match(T__2);
			setState(73);
			((GreaterContext)_localctx).b = expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Less_equalContext extends ParserRuleContext {
		public ExpressionContext a;
		public ExpressionContext b;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Less_equalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_less_equal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitLess_equal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Less_equalContext less_equal() throws RecognitionException {
		Less_equalContext _localctx = new Less_equalContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_less_equal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			((Less_equalContext)_localctx).a = expression();
			setState(76);
			match(T__3);
			setState(77);
			((Less_equalContext)_localctx).b = expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Greater_equalContext extends ParserRuleContext {
		public ExpressionContext a;
		public ExpressionContext b;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Greater_equalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_greater_equal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitGreater_equal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Greater_equalContext greater_equal() throws RecognitionException {
		Greater_equalContext _localctx = new Greater_equalContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_greater_equal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			((Greater_equalContext)_localctx).a = expression();
			setState(80);
			match(T__4);
			setState(81);
			((Greater_equalContext)_localctx).b = expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EqualContext extends ParserRuleContext {
		public ExpressionContext a;
		public ExpressionContext b;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public EqualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitEqual(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualContext equal() throws RecognitionException {
		EqualContext _localctx = new EqualContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_equal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			((EqualContext)_localctx).a = expression();
			setState(84);
			match(T__5);
			setState(86);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__5) {
				{
				setState(85);
				match(T__5);
				}
			}

			setState(88);
			((EqualContext)_localctx).b = expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Not_equalContext extends ParserRuleContext {
		public ExpressionContext a;
		public ExpressionContext b;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Not_equalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not_equal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitNot_equal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Not_equalContext not_equal() throws RecognitionException {
		Not_equalContext _localctx = new Not_equalContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_not_equal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			((Not_equalContext)_localctx).a = expression();
			setState(91);
			_la = _input.LA(1);
			if ( !(_la==T__6 || _la==T__7) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(92);
			((Not_equalContext)_localctx).b = expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NotContext extends ParserRuleContext {
		public PredicateContext a;
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public NotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitNot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotContext not() throws RecognitionException {
		NotContext _localctx = new NotContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_not);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			match(T__8);
			setState(95);
			((NotContext)_localctx).a = predicate();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public List<FactorContext> factor() {
			return getRuleContexts(FactorContext.class);
		}
		public FactorContext factor(int i) {
			return getRuleContext(FactorContext.class,i);
		}
		public List<Term_opContext> term_op() {
			return getRuleContexts(Term_opContext.class);
		}
		public Term_opContext term_op(int i) {
			return getRuleContext(Term_opContext.class,i);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			factor();
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MUL || _la==DIV) {
				{
				{
				setState(98);
				term_op();
				setState(99);
				factor();
				}
				}
				setState(105);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Term_opContext extends ParserRuleContext {
		public TerminalNode MUL() { return getToken(FormulaParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(FormulaParser.DIV, 0); }
		public Term_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term_op; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitTerm_op(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Term_opContext term_op() throws RecognitionException {
		Term_opContext _localctx = new Term_opContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_term_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			_la = _input.LA(1);
			if ( !(_la==MUL || _la==DIV) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FactorContext extends ParserRuleContext {
		public List<AtomContext> atom() {
			return getRuleContexts(AtomContext.class);
		}
		public AtomContext atom(int i) {
			return getRuleContext(AtomContext.class,i);
		}
		public TerminalNode POW() { return getToken(FormulaParser.POW, 0); }
		public FactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_factor; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitFactor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FactorContext factor() throws RecognitionException {
		FactorContext _localctx = new FactorContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_factor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			atom();
			setState(111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POW) {
				{
				setState(109);
				match(POW);
				setState(110);
				atom();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AtomContext extends ParserRuleContext {
		public ScientificContext scientific() {
			return getRuleContext(ScientificContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(FormulaParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(FormulaParser.RPAREN, 0); }
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_atom);
		try {
			setState(119);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUB:
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(113);
				scientific();
				}
				break;
			case UNDERSCORE:
			case LETTER:
				enterOuterAlt(_localctx, 2);
				{
				setState(114);
				variable();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 3);
				{
				setState(115);
				match(LPAREN);
				setState(116);
				expression();
				setState(117);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScientificContext extends ParserRuleContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public ScientificContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scientific; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitScientific(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScientificContext scientific() throws RecognitionException {
		ScientificContext _localctx = new ScientificContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_scientific);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121);
			number();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode SUB() { return getToken(FormulaParser.SUB, 0); }
		public List<TerminalNode> DIGIT() { return getTokens(FormulaParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(FormulaParser.DIGIT, i);
		}
		public TerminalNode POINT() { return getToken(FormulaParser.POINT, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SUB) {
				{
				setState(123);
				match(SUB);
				}
			}

			setState(127); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(126);
				match(DIGIT);
				}
				}
				setState(129); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==DIGIT );
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POINT) {
				{
				setState(131);
				match(POINT);
				setState(133); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(132);
					match(DIGIT);
					}
					}
					setState(135); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DIGIT );
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableContext extends ParserRuleContext {
		public List<TerminalNode> LETTER() { return getTokens(FormulaParser.LETTER); }
		public TerminalNode LETTER(int i) {
			return getToken(FormulaParser.LETTER, i);
		}
		public List<TerminalNode> UNDERSCORE() { return getTokens(FormulaParser.UNDERSCORE); }
		public TerminalNode UNDERSCORE(int i) {
			return getToken(FormulaParser.UNDERSCORE, i);
		}
		public List<TerminalNode> DIGIT() { return getTokens(FormulaParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(FormulaParser.DIGIT, i);
		}
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_variable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			_la = _input.LA(1);
			if ( !(_la==UNDERSCORE || _la==LETTER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << UNDERSCORE) | (1L << LETTER) | (1L << DIGIT))) != 0)) {
				{
				{
				setState(140);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << UNDERSCORE) | (1L << LETTER) | (1L << DIGIT))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(145);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\27\u0095\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\2\3\2\7\2+\n\2\f\2\16\2.\13\2\3\2\5\2\61\n\2\3\3"+
		"\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5D"+
		"\n\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3"+
		"\n\3\n\3\n\5\nY\n\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3"+
		"\r\3\r\7\rh\n\r\f\r\16\rk\13\r\3\16\3\16\3\17\3\17\3\17\5\17r\n\17\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\5\20z\n\20\3\21\3\21\3\22\5\22\177\n\22\3\22"+
		"\6\22\u0082\n\22\r\22\16\22\u0083\3\22\3\22\6\22\u0088\n\22\r\22\16\22"+
		"\u0089\5\22\u008c\n\22\3\23\3\23\7\23\u0090\n\23\f\23\16\23\u0093\13\23"+
		"\3\23\2\2\24\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$\2\7\3\2\16\17"+
		"\3\2\t\n\3\2\20\21\3\2\24\25\3\2\24\26\2\u0094\2\60\3\2\2\2\4\62\3\2\2"+
		"\2\6\64\3\2\2\2\bC\3\2\2\2\nE\3\2\2\2\fI\3\2\2\2\16M\3\2\2\2\20Q\3\2\2"+
		"\2\22U\3\2\2\2\24\\\3\2\2\2\26`\3\2\2\2\30c\3\2\2\2\32l\3\2\2\2\34n\3"+
		"\2\2\2\36y\3\2\2\2 {\3\2\2\2\"~\3\2\2\2$\u008d\3\2\2\2&,\5\30\r\2\'(\5"+
		"\4\3\2()\5\30\r\2)+\3\2\2\2*\'\3\2\2\2+.\3\2\2\2,*\3\2\2\2,-\3\2\2\2-"+
		"\61\3\2\2\2.,\3\2\2\2/\61\5\6\4\2\60&\3\2\2\2\60/\3\2\2\2\61\3\3\2\2\2"+
		"\62\63\t\2\2\2\63\5\3\2\2\2\64\65\7\f\2\2\65\66\5\b\5\2\66\67\7\3\2\2"+
		"\678\5\2\2\289\7\4\2\29:\5\2\2\2:;\7\r\2\2;\7\3\2\2\2<D\5\n\6\2=D\5\f"+
		"\7\2>D\5\16\b\2?D\5\20\t\2@D\5\22\n\2AD\5\24\13\2BD\5\26\f\2C<\3\2\2\2"+
		"C=\3\2\2\2C>\3\2\2\2C?\3\2\2\2C@\3\2\2\2CA\3\2\2\2CB\3\2\2\2D\t\3\2\2"+
		"\2EF\5\2\2\2FG\7\5\2\2GH\5\2\2\2H\13\3\2\2\2IJ\5\2\2\2JK\7\5\2\2KL\5\2"+
		"\2\2L\r\3\2\2\2MN\5\2\2\2NO\7\6\2\2OP\5\2\2\2P\17\3\2\2\2QR\5\2\2\2RS"+
		"\7\7\2\2ST\5\2\2\2T\21\3\2\2\2UV\5\2\2\2VX\7\b\2\2WY\7\b\2\2XW\3\2\2\2"+
		"XY\3\2\2\2YZ\3\2\2\2Z[\5\2\2\2[\23\3\2\2\2\\]\5\2\2\2]^\t\3\2\2^_\5\2"+
		"\2\2_\25\3\2\2\2`a\7\13\2\2ab\5\b\5\2b\27\3\2\2\2ci\5\34\17\2de\5\32\16"+
		"\2ef\5\34\17\2fh\3\2\2\2gd\3\2\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2\2\2j\31\3"+
		"\2\2\2ki\3\2\2\2lm\t\4\2\2m\33\3\2\2\2nq\5\36\20\2op\7\23\2\2pr\5\36\20"+
		"\2qo\3\2\2\2qr\3\2\2\2r\35\3\2\2\2sz\5 \21\2tz\5$\23\2uv\7\f\2\2vw\5\2"+
		"\2\2wx\7\r\2\2xz\3\2\2\2ys\3\2\2\2yt\3\2\2\2yu\3\2\2\2z\37\3\2\2\2{|\5"+
		"\"\22\2|!\3\2\2\2}\177\7\17\2\2~}\3\2\2\2~\177\3\2\2\2\177\u0081\3\2\2"+
		"\2\u0080\u0082\7\26\2\2\u0081\u0080\3\2\2\2\u0082\u0083\3\2\2\2\u0083"+
		"\u0081\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u008b\3\2\2\2\u0085\u0087\7\22"+
		"\2\2\u0086\u0088\7\26\2\2\u0087\u0086\3\2\2\2\u0088\u0089\3\2\2\2\u0089"+
		"\u0087\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008c\3\2\2\2\u008b\u0085\3\2"+
		"\2\2\u008b\u008c\3\2\2\2\u008c#\3\2\2\2\u008d\u0091\t\5\2\2\u008e\u0090"+
		"\t\6\2\2\u008f\u008e\3\2\2\2\u0090\u0093\3\2\2\2\u0091\u008f\3\2\2\2\u0091"+
		"\u0092\3\2\2\2\u0092%\3\2\2\2\u0093\u0091\3\2\2\2\16,\60CXiqy~\u0083\u0089"+
		"\u008b\u0091";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
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
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, LPAREN=22, RPAREN=23, ADD=24, 
		SUB=25, MUL=26, DIV=27, POINT=28, POW=29, UNDERSCORE=30, LETTER=31, DIGIT=32, 
		WS=33;
	public static final int
		RULE_expression = 0, RULE_expression_op = 1, RULE_conditional = 2, RULE_predicate_expression = 3, 
		RULE_predicate_term = 4, RULE_predicate_factor = 5, RULE_predicate_atom = 6, 
		RULE_less = 7, RULE_greater = 8, RULE_less_equal = 9, RULE_greater_equal = 10, 
		RULE_equal = 11, RULE_not_equal = 12, RULE_term = 13, RULE_term_op = 14, 
		RULE_factor = 15, RULE_atom = 16, RULE_scientific = 17, RULE_number = 18, 
		RULE_variable = 19, RULE_parameter = 20, RULE_identifier = 21;
	public static final String[] ruleNames = {
		"expression", "expression_op", "conditional", "predicate_expression", 
		"predicate_term", "predicate_factor", "predicate_atom", "less", "greater", 
		"less_equal", "greater_equal", "equal", "not_equal", "term", "term_op", 
		"factor", "atom", "scientific", "number", "variable", "parameter", "identifier"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'IF'", "'THEN'", "'ELSE'", "'?'", "':'", "'OR'", "'||'", "'|'", 
		"'AND'", "'&&'", "'&'", "'NOT'", "'!'", "'<'", "'>'", "'<='", "'>='", 
		"'=='", "'='", "'!='", "'<>'", "'('", "')'", "'+'", "'-'", "'*'", "'/'", 
		"'.'", "'^'", "'_'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
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
			setState(54);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(44);
				term();
				setState(50);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ADD || _la==SUB) {
					{
					{
					setState(45);
					expression_op();
					setState(46);
					term();
					}
					}
					setState(52);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(53);
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
			setState(56);
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
		public Predicate_expressionContext p;
		public ExpressionContext a;
		public ExpressionContext b;
		public Predicate_expressionContext predicate_expression() {
			return getRuleContext(Predicate_expressionContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode LPAREN() { return getToken(FormulaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(FormulaParser.RPAREN, 0); }
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
			setState(73);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				match(T__0);
				setState(59);
				((ConditionalContext)_localctx).p = predicate_expression();
				setState(60);
				match(T__1);
				setState(61);
				((ConditionalContext)_localctx).a = expression();
				setState(62);
				match(T__2);
				setState(63);
				((ConditionalContext)_localctx).b = expression();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(65);
				match(LPAREN);
				setState(66);
				((ConditionalContext)_localctx).p = predicate_expression();
				setState(67);
				match(T__3);
				setState(68);
				((ConditionalContext)_localctx).a = expression();
				setState(69);
				match(T__4);
				setState(70);
				((ConditionalContext)_localctx).b = expression();
				setState(71);
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

	public static class Predicate_expressionContext extends ParserRuleContext {
		public List<Predicate_termContext> predicate_term() {
			return getRuleContexts(Predicate_termContext.class);
		}
		public Predicate_termContext predicate_term(int i) {
			return getRuleContext(Predicate_termContext.class,i);
		}
		public Predicate_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitPredicate_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Predicate_expressionContext predicate_expression() throws RecognitionException {
		Predicate_expressionContext _localctx = new Predicate_expressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_predicate_expression);
		int _la;
		try {
			setState(99);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(75);
				predicate_term();
				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(76);
					match(T__5);
					setState(77);
					predicate_term();
					}
					}
					setState(82);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(83);
				predicate_term();
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__6) {
					{
					{
					setState(84);
					match(T__6);
					setState(85);
					predicate_term();
					}
					}
					setState(90);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(91);
				predicate_term();
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__7) {
					{
					{
					setState(92);
					match(T__7);
					setState(93);
					predicate_term();
					}
					}
					setState(98);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
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

	public static class Predicate_termContext extends ParserRuleContext {
		public List<Predicate_factorContext> predicate_factor() {
			return getRuleContexts(Predicate_factorContext.class);
		}
		public Predicate_factorContext predicate_factor(int i) {
			return getRuleContext(Predicate_factorContext.class,i);
		}
		public Predicate_termContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate_term; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitPredicate_term(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Predicate_termContext predicate_term() throws RecognitionException {
		Predicate_termContext _localctx = new Predicate_termContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_predicate_term);
		int _la;
		try {
			setState(125);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(101);
				predicate_factor();
				setState(106);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__8) {
					{
					{
					setState(102);
					match(T__8);
					setState(103);
					predicate_factor();
					}
					}
					setState(108);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(109);
				predicate_factor();
				setState(114);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__9) {
					{
					{
					setState(110);
					match(T__9);
					setState(111);
					predicate_factor();
					}
					}
					setState(116);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(117);
				predicate_factor();
				setState(122);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__10) {
					{
					{
					setState(118);
					match(T__10);
					setState(119);
					predicate_factor();
					}
					}
					setState(124);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
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

	public static class Predicate_factorContext extends ParserRuleContext {
		public Token not;
		public Predicate_atomContext predicate_atom() {
			return getRuleContext(Predicate_atomContext.class,0);
		}
		public Predicate_factorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate_factor; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitPredicate_factor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Predicate_factorContext predicate_factor() throws RecognitionException {
		Predicate_factorContext _localctx = new Predicate_factorContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_predicate_factor);
		int _la;
		try {
			setState(135);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(128);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__11) {
					{
					setState(127);
					((Predicate_factorContext)_localctx).not = match(T__11);
					}
				}

				setState(130);
				predicate_atom();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(132);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__12) {
					{
					setState(131);
					((Predicate_factorContext)_localctx).not = match(T__12);
					}
				}

				setState(134);
				predicate_atom();
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

	public static class Predicate_atomContext extends ParserRuleContext {
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
		public TerminalNode LPAREN() { return getToken(FormulaParser.LPAREN, 0); }
		public Predicate_expressionContext predicate_expression() {
			return getRuleContext(Predicate_expressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(FormulaParser.RPAREN, 0); }
		public Predicate_atomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate_atom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitPredicate_atom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Predicate_atomContext predicate_atom() throws RecognitionException {
		Predicate_atomContext _localctx = new Predicate_atomContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_predicate_atom);
		try {
			setState(147);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(137);
				less();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(138);
				greater();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(139);
				less_equal();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(140);
				greater_equal();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(141);
				equal();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(142);
				not_equal();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(143);
				match(LPAREN);
				setState(144);
				predicate_expression();
				setState(145);
				match(RPAREN);
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
		enterRule(_localctx, 14, RULE_less);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			((LessContext)_localctx).a = expression();
			setState(150);
			match(T__13);
			setState(151);
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
		enterRule(_localctx, 16, RULE_greater);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			((GreaterContext)_localctx).a = expression();
			setState(154);
			match(T__14);
			setState(155);
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
		enterRule(_localctx, 18, RULE_less_equal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			((Less_equalContext)_localctx).a = expression();
			setState(158);
			match(T__15);
			setState(159);
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
		enterRule(_localctx, 20, RULE_greater_equal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161);
			((Greater_equalContext)_localctx).a = expression();
			setState(162);
			match(T__16);
			setState(163);
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
		enterRule(_localctx, 22, RULE_equal);
		try {
			setState(173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(165);
				((EqualContext)_localctx).a = expression();
				setState(166);
				match(T__17);
				setState(167);
				((EqualContext)_localctx).b = expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(169);
				((EqualContext)_localctx).a = expression();
				setState(170);
				match(T__18);
				setState(171);
				((EqualContext)_localctx).b = expression();
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
		enterRule(_localctx, 24, RULE_not_equal);
		try {
			setState(183);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(175);
				((Not_equalContext)_localctx).a = expression();
				setState(176);
				match(T__19);
				setState(177);
				((Not_equalContext)_localctx).b = expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(179);
				((Not_equalContext)_localctx).a = expression();
				setState(180);
				match(T__20);
				setState(181);
				((Not_equalContext)_localctx).b = expression();
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
		enterRule(_localctx, 26, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			factor();
			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MUL || _la==DIV) {
				{
				{
				setState(186);
				term_op();
				setState(187);
				factor();
				}
				}
				setState(193);
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
		enterRule(_localctx, 28, RULE_term_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
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
		enterRule(_localctx, 30, RULE_factor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			atom();
			setState(199);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POW) {
				{
				setState(197);
				match(POW);
				setState(198);
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
		enterRule(_localctx, 32, RULE_atom);
		try {
			setState(207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(201);
				scientific();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(202);
				variable();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(203);
				match(LPAREN);
				setState(204);
				expression();
				setState(205);
				match(RPAREN);
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

	public static class ScientificContext extends ParserRuleContext {
		public List<NumberContext> number() {
			return getRuleContexts(NumberContext.class);
		}
		public NumberContext number(int i) {
			return getRuleContext(NumberContext.class,i);
		}
		public TerminalNode LETTER() { return getToken(FormulaParser.LETTER, 0); }
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
		enterRule(_localctx, 34, RULE_scientific);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			number();
			setState(212);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LETTER) {
				{
				setState(210);
				match(LETTER);
				setState(211);
				number();
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
		enterRule(_localctx, 36, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SUB) {
				{
				setState(214);
				match(SUB);
				}
			}

			setState(218); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(217);
				match(DIGIT);
				}
				}
				setState(220); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==DIGIT );
			setState(228);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POINT) {
				{
				setState(222);
				match(POINT);
				setState(224); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(223);
					match(DIGIT);
					}
					}
					setState(226); 
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
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode SUB() { return getToken(FormulaParser.SUB, 0); }
		public ParameterContext parameter() {
			return getRuleContext(ParameterContext.class,0);
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
		enterRule(_localctx, 38, RULE_variable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SUB) {
				{
				setState(230);
				match(SUB);
				}
			}

			setState(233);
			identifier();
			setState(235);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(234);
				parameter();
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

	public static class ParameterContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(FormulaParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(FormulaParser.RPAREN, 0); }
		public ParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameter; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterContext parameter() throws RecognitionException {
		ParameterContext _localctx = new ParameterContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_parameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(237);
			match(LPAREN);
			setState(238);
			expression();
			setState(239);
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

	public static class IdentifierContext extends ParserRuleContext {
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
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FormulaVisitor ) return ((FormulaVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(241);
			_la = _input.LA(1);
			if ( !(_la==UNDERSCORE || _la==LETTER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << UNDERSCORE) | (1L << LETTER) | (1L << DIGIT))) != 0)) {
				{
				{
				setState(242);
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
				setState(247);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3#\u00fb\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\2\3\2\7\2"+
		"\63\n\2\f\2\16\2\66\13\2\3\2\5\29\n\2\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4L\n\4\3\5\3\5\3\5\7\5Q\n\5\f\5"+
		"\16\5T\13\5\3\5\3\5\3\5\7\5Y\n\5\f\5\16\5\\\13\5\3\5\3\5\3\5\7\5a\n\5"+
		"\f\5\16\5d\13\5\5\5f\n\5\3\6\3\6\3\6\7\6k\n\6\f\6\16\6n\13\6\3\6\3\6\3"+
		"\6\7\6s\n\6\f\6\16\6v\13\6\3\6\3\6\3\6\7\6{\n\6\f\6\16\6~\13\6\5\6\u0080"+
		"\n\6\3\7\5\7\u0083\n\7\3\7\3\7\5\7\u0087\n\7\3\7\5\7\u008a\n\7\3\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u0096\n\b\3\t\3\t\3\t\3\t\3\n\3\n"+
		"\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\5\r\u00b0\n\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u00ba"+
		"\n\16\3\17\3\17\3\17\3\17\7\17\u00c0\n\17\f\17\16\17\u00c3\13\17\3\20"+
		"\3\20\3\21\3\21\3\21\5\21\u00ca\n\21\3\22\3\22\3\22\3\22\3\22\3\22\5\22"+
		"\u00d2\n\22\3\23\3\23\3\23\5\23\u00d7\n\23\3\24\5\24\u00da\n\24\3\24\6"+
		"\24\u00dd\n\24\r\24\16\24\u00de\3\24\3\24\6\24\u00e3\n\24\r\24\16\24\u00e4"+
		"\5\24\u00e7\n\24\3\25\5\25\u00ea\n\25\3\25\3\25\5\25\u00ee\n\25\3\26\3"+
		"\26\3\26\3\26\3\27\3\27\7\27\u00f6\n\27\f\27\16\27\u00f9\13\27\3\27\2"+
		"\2\30\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,\2\6\3\2\32\33\3\2"+
		"\34\35\3\2 !\3\2 \"\2\u0108\28\3\2\2\2\4:\3\2\2\2\6K\3\2\2\2\be\3\2\2"+
		"\2\n\177\3\2\2\2\f\u0089\3\2\2\2\16\u0095\3\2\2\2\20\u0097\3\2\2\2\22"+
		"\u009b\3\2\2\2\24\u009f\3\2\2\2\26\u00a3\3\2\2\2\30\u00af\3\2\2\2\32\u00b9"+
		"\3\2\2\2\34\u00bb\3\2\2\2\36\u00c4\3\2\2\2 \u00c6\3\2\2\2\"\u00d1\3\2"+
		"\2\2$\u00d3\3\2\2\2&\u00d9\3\2\2\2(\u00e9\3\2\2\2*\u00ef\3\2\2\2,\u00f3"+
		"\3\2\2\2.\64\5\34\17\2/\60\5\4\3\2\60\61\5\34\17\2\61\63\3\2\2\2\62/\3"+
		"\2\2\2\63\66\3\2\2\2\64\62\3\2\2\2\64\65\3\2\2\2\659\3\2\2\2\66\64\3\2"+
		"\2\2\679\5\6\4\28.\3\2\2\28\67\3\2\2\29\3\3\2\2\2:;\t\2\2\2;\5\3\2\2\2"+
		"<=\7\3\2\2=>\5\b\5\2>?\7\4\2\2?@\5\2\2\2@A\7\5\2\2AB\5\2\2\2BL\3\2\2\2"+
		"CD\7\30\2\2DE\5\b\5\2EF\7\6\2\2FG\5\2\2\2GH\7\7\2\2HI\5\2\2\2IJ\7\31\2"+
		"\2JL\3\2\2\2K<\3\2\2\2KC\3\2\2\2L\7\3\2\2\2MR\5\n\6\2NO\7\b\2\2OQ\5\n"+
		"\6\2PN\3\2\2\2QT\3\2\2\2RP\3\2\2\2RS\3\2\2\2Sf\3\2\2\2TR\3\2\2\2UZ\5\n"+
		"\6\2VW\7\t\2\2WY\5\n\6\2XV\3\2\2\2Y\\\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2[f\3"+
		"\2\2\2\\Z\3\2\2\2]b\5\n\6\2^_\7\n\2\2_a\5\n\6\2`^\3\2\2\2ad\3\2\2\2b`"+
		"\3\2\2\2bc\3\2\2\2cf\3\2\2\2db\3\2\2\2eM\3\2\2\2eU\3\2\2\2e]\3\2\2\2f"+
		"\t\3\2\2\2gl\5\f\7\2hi\7\13\2\2ik\5\f\7\2jh\3\2\2\2kn\3\2\2\2lj\3\2\2"+
		"\2lm\3\2\2\2m\u0080\3\2\2\2nl\3\2\2\2ot\5\f\7\2pq\7\f\2\2qs\5\f\7\2rp"+
		"\3\2\2\2sv\3\2\2\2tr\3\2\2\2tu\3\2\2\2u\u0080\3\2\2\2vt\3\2\2\2w|\5\f"+
		"\7\2xy\7\r\2\2y{\5\f\7\2zx\3\2\2\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\u0080"+
		"\3\2\2\2~|\3\2\2\2\177g\3\2\2\2\177o\3\2\2\2\177w\3\2\2\2\u0080\13\3\2"+
		"\2\2\u0081\u0083\7\16\2\2\u0082\u0081\3\2\2\2\u0082\u0083\3\2\2\2\u0083"+
		"\u0084\3\2\2\2\u0084\u008a\5\16\b\2\u0085\u0087\7\17\2\2\u0086\u0085\3"+
		"\2\2\2\u0086\u0087\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u008a\5\16\b\2\u0089"+
		"\u0082\3\2\2\2\u0089\u0086\3\2\2\2\u008a\r\3\2\2\2\u008b\u0096\5\20\t"+
		"\2\u008c\u0096\5\22\n\2\u008d\u0096\5\24\13\2\u008e\u0096\5\26\f\2\u008f"+
		"\u0096\5\30\r\2\u0090\u0096\5\32\16\2\u0091\u0092\7\30\2\2\u0092\u0093"+
		"\5\b\5\2\u0093\u0094\7\31\2\2\u0094\u0096\3\2\2\2\u0095\u008b\3\2\2\2"+
		"\u0095\u008c\3\2\2\2\u0095\u008d\3\2\2\2\u0095\u008e\3\2\2\2\u0095\u008f"+
		"\3\2\2\2\u0095\u0090\3\2\2\2\u0095\u0091\3\2\2\2\u0096\17\3\2\2\2\u0097"+
		"\u0098\5\2\2\2\u0098\u0099\7\20\2\2\u0099\u009a\5\2\2\2\u009a\21\3\2\2"+
		"\2\u009b\u009c\5\2\2\2\u009c\u009d\7\21\2\2\u009d\u009e\5\2\2\2\u009e"+
		"\23\3\2\2\2\u009f\u00a0\5\2\2\2\u00a0\u00a1\7\22\2\2\u00a1\u00a2\5\2\2"+
		"\2\u00a2\25\3\2\2\2\u00a3\u00a4\5\2\2\2\u00a4\u00a5\7\23\2\2\u00a5\u00a6"+
		"\5\2\2\2\u00a6\27\3\2\2\2\u00a7\u00a8\5\2\2\2\u00a8\u00a9\7\24\2\2\u00a9"+
		"\u00aa\5\2\2\2\u00aa\u00b0\3\2\2\2\u00ab\u00ac\5\2\2\2\u00ac\u00ad\7\25"+
		"\2\2\u00ad\u00ae\5\2\2\2\u00ae\u00b0\3\2\2\2\u00af\u00a7\3\2\2\2\u00af"+
		"\u00ab\3\2\2\2\u00b0\31\3\2\2\2\u00b1\u00b2\5\2\2\2\u00b2\u00b3\7\26\2"+
		"\2\u00b3\u00b4\5\2\2\2\u00b4\u00ba\3\2\2\2\u00b5\u00b6\5\2\2\2\u00b6\u00b7"+
		"\7\27\2\2\u00b7\u00b8\5\2\2\2\u00b8\u00ba\3\2\2\2\u00b9\u00b1\3\2\2\2"+
		"\u00b9\u00b5\3\2\2\2\u00ba\33\3\2\2\2\u00bb\u00c1\5 \21\2\u00bc\u00bd"+
		"\5\36\20\2\u00bd\u00be\5 \21\2\u00be\u00c0\3\2\2\2\u00bf\u00bc\3\2\2\2"+
		"\u00c0\u00c3\3\2\2\2\u00c1\u00bf\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\35"+
		"\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c4\u00c5\t\3\2\2\u00c5\37\3\2\2\2\u00c6"+
		"\u00c9\5\"\22\2\u00c7\u00c8\7\37\2\2\u00c8\u00ca\5\"\22\2\u00c9\u00c7"+
		"\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca!\3\2\2\2\u00cb\u00d2\5$\23\2\u00cc"+
		"\u00d2\5(\25\2\u00cd\u00ce\7\30\2\2\u00ce\u00cf\5\2\2\2\u00cf\u00d0\7"+
		"\31\2\2\u00d0\u00d2\3\2\2\2\u00d1\u00cb\3\2\2\2\u00d1\u00cc\3\2\2\2\u00d1"+
		"\u00cd\3\2\2\2\u00d2#\3\2\2\2\u00d3\u00d6\5&\24\2\u00d4\u00d5\7!\2\2\u00d5"+
		"\u00d7\5&\24\2\u00d6\u00d4\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7%\3\2\2\2"+
		"\u00d8\u00da\7\33\2\2\u00d9\u00d8\3\2\2\2\u00d9\u00da\3\2\2\2\u00da\u00dc"+
		"\3\2\2\2\u00db\u00dd\7\"\2\2\u00dc\u00db\3\2\2\2\u00dd\u00de\3\2\2\2\u00de"+
		"\u00dc\3\2\2\2\u00de\u00df\3\2\2\2\u00df\u00e6\3\2\2\2\u00e0\u00e2\7\36"+
		"\2\2\u00e1\u00e3\7\"\2\2\u00e2\u00e1\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4"+
		"\u00e2\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e7\3\2\2\2\u00e6\u00e0\3\2"+
		"\2\2\u00e6\u00e7\3\2\2\2\u00e7\'\3\2\2\2\u00e8\u00ea\7\33\2\2\u00e9\u00e8"+
		"\3\2\2\2\u00e9\u00ea\3\2\2\2\u00ea\u00eb\3\2\2\2\u00eb\u00ed\5,\27\2\u00ec"+
		"\u00ee\5*\26\2\u00ed\u00ec\3\2\2\2\u00ed\u00ee\3\2\2\2\u00ee)\3\2\2\2"+
		"\u00ef\u00f0\7\30\2\2\u00f0\u00f1\5\2\2\2\u00f1\u00f2\7\31\2\2\u00f2+"+
		"\3\2\2\2\u00f3\u00f7\t\4\2\2\u00f4\u00f6\t\5\2\2\u00f5\u00f4\3\2\2\2\u00f6"+
		"\u00f9\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f7\u00f8\3\2\2\2\u00f8-\3\2\2\2"+
		"\u00f9\u00f7\3\2\2\2\36\648KRZbelt|\177\u0082\u0086\u0089\u0095\u00af"+
		"\u00b9\u00c1\u00c9\u00d1\u00d6\u00d9\u00de\u00e4\u00e6\u00e9\u00ed\u00f7";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
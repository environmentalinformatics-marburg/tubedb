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
		LPAREN=1, RPAREN=2, ADD=3, SUB=4, MUL=5, DIV=6, POINT=7, E=8, POW=9, UNDERSCORE=10, 
		LETTER=11, DIGIT=12, WS=13;
	public static final int
		RULE_expression = 0, RULE_expression_op = 1, RULE_term = 2, RULE_term_op = 3, 
		RULE_factor = 4, RULE_atom = 5, RULE_scientific = 6, RULE_number = 7, 
		RULE_variable = 8;
	public static final String[] ruleNames = {
		"expression", "expression_op", "term", "term_op", "factor", "atom", "scientific", 
		"number", "variable"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "'+'", "'-'", "'*'", "'/'", "'.'", null, "'^'", "'_'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LPAREN", "RPAREN", "ADD", "SUB", "MUL", "DIV", "POINT", "E", "POW", 
		"UNDERSCORE", "LETTER", "DIGIT", "WS"
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
			enterOuterAlt(_localctx, 1);
			{
			setState(18);
			term();
			setState(24);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ADD || _la==SUB) {
				{
				{
				setState(19);
				expression_op();
				setState(20);
				term();
				}
				}
				setState(26);
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
			setState(27);
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
		enterRule(_localctx, 4, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(29);
			factor();
			setState(35);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MUL || _la==DIV) {
				{
				{
				setState(30);
				term_op();
				setState(31);
				factor();
				}
				}
				setState(37);
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
		enterRule(_localctx, 6, RULE_term_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
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
		enterRule(_localctx, 8, RULE_factor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			atom();
			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POW) {
				{
				setState(41);
				match(POW);
				setState(42);
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
		enterRule(_localctx, 10, RULE_atom);
		try {
			setState(51);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUB:
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(45);
				scientific();
				}
				break;
			case UNDERSCORE:
			case LETTER:
				enterOuterAlt(_localctx, 2);
				{
				setState(46);
				variable();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 3);
				{
				setState(47);
				match(LPAREN);
				setState(48);
				expression();
				setState(49);
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
		public List<NumberContext> number() {
			return getRuleContexts(NumberContext.class);
		}
		public NumberContext number(int i) {
			return getRuleContext(NumberContext.class,i);
		}
		public TerminalNode E() { return getToken(FormulaParser.E, 0); }
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
		enterRule(_localctx, 12, RULE_scientific);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			number();
			setState(56);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==E) {
				{
				setState(54);
				match(E);
				setState(55);
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
		enterRule(_localctx, 14, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SUB) {
				{
				setState(58);
				match(SUB);
				}
			}

			setState(62); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(61);
				match(DIGIT);
				}
				}
				setState(64); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==DIGIT );
			setState(72);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POINT) {
				{
				setState(66);
				match(POINT);
				setState(68); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(67);
					match(DIGIT);
					}
					}
					setState(70); 
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
		enterRule(_localctx, 16, RULE_variable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			_la = _input.LA(1);
			if ( !(_la==UNDERSCORE || _la==LETTER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << UNDERSCORE) | (1L << LETTER) | (1L << DIGIT))) != 0)) {
				{
				{
				setState(75);
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
				setState(80);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\17T\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2\3\2"+
		"\3\2\7\2\31\n\2\f\2\16\2\34\13\2\3\3\3\3\3\4\3\4\3\4\3\4\7\4$\n\4\f\4"+
		"\16\4\'\13\4\3\5\3\5\3\6\3\6\3\6\5\6.\n\6\3\7\3\7\3\7\3\7\3\7\3\7\5\7"+
		"\66\n\7\3\b\3\b\3\b\5\b;\n\b\3\t\5\t>\n\t\3\t\6\tA\n\t\r\t\16\tB\3\t\3"+
		"\t\6\tG\n\t\r\t\16\tH\5\tK\n\t\3\n\3\n\7\nO\n\n\f\n\16\nR\13\n\3\n\2\2"+
		"\13\2\4\6\b\n\f\16\20\22\2\6\3\2\5\6\3\2\7\b\3\2\f\r\3\2\f\16\2U\2\24"+
		"\3\2\2\2\4\35\3\2\2\2\6\37\3\2\2\2\b(\3\2\2\2\n*\3\2\2\2\f\65\3\2\2\2"+
		"\16\67\3\2\2\2\20=\3\2\2\2\22L\3\2\2\2\24\32\5\6\4\2\25\26\5\4\3\2\26"+
		"\27\5\6\4\2\27\31\3\2\2\2\30\25\3\2\2\2\31\34\3\2\2\2\32\30\3\2\2\2\32"+
		"\33\3\2\2\2\33\3\3\2\2\2\34\32\3\2\2\2\35\36\t\2\2\2\36\5\3\2\2\2\37%"+
		"\5\n\6\2 !\5\b\5\2!\"\5\n\6\2\"$\3\2\2\2# \3\2\2\2$\'\3\2\2\2%#\3\2\2"+
		"\2%&\3\2\2\2&\7\3\2\2\2\'%\3\2\2\2()\t\3\2\2)\t\3\2\2\2*-\5\f\7\2+,\7"+
		"\13\2\2,.\5\f\7\2-+\3\2\2\2-.\3\2\2\2.\13\3\2\2\2/\66\5\16\b\2\60\66\5"+
		"\22\n\2\61\62\7\3\2\2\62\63\5\2\2\2\63\64\7\4\2\2\64\66\3\2\2\2\65/\3"+
		"\2\2\2\65\60\3\2\2\2\65\61\3\2\2\2\66\r\3\2\2\2\67:\5\20\t\289\7\n\2\2"+
		"9;\5\20\t\2:8\3\2\2\2:;\3\2\2\2;\17\3\2\2\2<>\7\6\2\2=<\3\2\2\2=>\3\2"+
		"\2\2>@\3\2\2\2?A\7\16\2\2@?\3\2\2\2AB\3\2\2\2B@\3\2\2\2BC\3\2\2\2CJ\3"+
		"\2\2\2DF\7\t\2\2EG\7\16\2\2FE\3\2\2\2GH\3\2\2\2HF\3\2\2\2HI\3\2\2\2IK"+
		"\3\2\2\2JD\3\2\2\2JK\3\2\2\2K\21\3\2\2\2LP\t\4\2\2MO\t\5\2\2NM\3\2\2\2"+
		"OR\3\2\2\2PN\3\2\2\2PQ\3\2\2\2Q\23\3\2\2\2RP\3\2\2\2\f\32%-\65:=BHJP";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
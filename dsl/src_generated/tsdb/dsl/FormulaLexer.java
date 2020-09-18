// Generated from Formula.g4 by ANTLR 4.4
package tsdb.dsl;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FormulaLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__20=1, T__19=2, T__18=3, T__17=4, T__16=5, T__15=6, T__14=7, T__13=8, 
		T__12=9, T__11=10, T__10=11, T__9=12, T__8=13, T__7=14, T__6=15, T__5=16, 
		T__4=17, T__3=18, T__2=19, T__1=20, T__0=21, LPAREN=22, RPAREN=23, ADD=24, 
		SUB=25, MUL=26, DIV=27, POINT=28, POW=29, UNDERSCORE=30, LETTER=31, DIGIT=32, 
		WS=33;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'", "'\\u0011'", "'\\u0012'", 
		"'\\u0013'", "'\\u0014'", "'\\u0015'", "'\\u0016'", "'\\u0017'", "'\\u0018'", 
		"'\\u0019'", "'\\u001A'", "'\\u001B'", "'\\u001C'", "'\\u001D'", "'\\u001E'", 
		"'\\u001F'", "' '", "'!'"
	};
	public static final String[] ruleNames = {
		"T__20", "T__19", "T__18", "T__17", "T__16", "T__15", "T__14", "T__13", 
		"T__12", "T__11", "T__10", "T__9", "T__8", "T__7", "T__6", "T__5", "T__4", 
		"T__3", "T__2", "T__1", "T__0", "LPAREN", "RPAREN", "ADD", "SUB", "MUL", 
		"DIV", "POINT", "POW", "UNDERSCORE", "LETTER", "DIGIT", "WS"
	};


	public FormulaLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Formula.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2#\u00a0\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3"+
		"\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\f\3"+
		"\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3"+
		"\22\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3"+
		"\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3"+
		"\35\3\36\3\36\3\37\3\37\3 \5 \u0096\n \3!\3!\3\"\6\"\u009b\n\"\r\"\16"+
		"\"\u009c\3\"\3\"\2\2#\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27"+
		"\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33"+
		"\65\34\67\359\36;\37= ?!A\"C#\3\2\4\4\2C\\c|\5\2\13\f\17\17\"\"\u00a0"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2"+
		"\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3"+
		"\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\3E\3\2\2\2\5J\3\2\2"+
		"\2\7M\3\2\2\2\tO\3\2\2\2\13R\3\2\2\2\rV\3\2\2\2\17Y\3\2\2\2\21\\\3\2\2"+
		"\2\23^\3\2\2\2\25a\3\2\2\2\27c\3\2\2\2\31e\3\2\2\2\33i\3\2\2\2\35l\3\2"+
		"\2\2\37n\3\2\2\2!q\3\2\2\2#s\3\2\2\2%v\3\2\2\2\'x\3\2\2\2)}\3\2\2\2+\u0080"+
		"\3\2\2\2-\u0082\3\2\2\2/\u0084\3\2\2\2\61\u0086\3\2\2\2\63\u0088\3\2\2"+
		"\2\65\u008a\3\2\2\2\67\u008c\3\2\2\29\u008e\3\2\2\2;\u0090\3\2\2\2=\u0092"+
		"\3\2\2\2?\u0095\3\2\2\2A\u0097\3\2\2\2C\u009a\3\2\2\2EF\7G\2\2FG\7N\2"+
		"\2GH\7U\2\2HI\7G\2\2I\4\3\2\2\2JK\7#\2\2KL\7?\2\2L\6\3\2\2\2MN\7<\2\2"+
		"N\b\3\2\2\2OP\7@\2\2PQ\7?\2\2Q\n\3\2\2\2RS\7C\2\2ST\7P\2\2TU\7F\2\2U\f"+
		"\3\2\2\2VW\7~\2\2WX\7~\2\2X\16\3\2\2\2YZ\7?\2\2Z[\7?\2\2[\20\3\2\2\2\\"+
		"]\7>\2\2]\22\3\2\2\2^_\7(\2\2_`\7(\2\2`\24\3\2\2\2ab\7~\2\2b\26\3\2\2"+
		"\2cd\7?\2\2d\30\3\2\2\2ef\7P\2\2fg\7Q\2\2gh\7V\2\2h\32\3\2\2\2ij\7K\2"+
		"\2jk\7H\2\2k\34\3\2\2\2lm\7@\2\2m\36\3\2\2\2no\7Q\2\2op\7T\2\2p \3\2\2"+
		"\2qr\7A\2\2r\"\3\2\2\2st\7>\2\2tu\7?\2\2u$\3\2\2\2vw\7#\2\2w&\3\2\2\2"+
		"xy\7V\2\2yz\7J\2\2z{\7G\2\2{|\7P\2\2|(\3\2\2\2}~\7>\2\2~\177\7@\2\2\177"+
		"*\3\2\2\2\u0080\u0081\7(\2\2\u0081,\3\2\2\2\u0082\u0083\7*\2\2\u0083."+
		"\3\2\2\2\u0084\u0085\7+\2\2\u0085\60\3\2\2\2\u0086\u0087\7-\2\2\u0087"+
		"\62\3\2\2\2\u0088\u0089\7/\2\2\u0089\64\3\2\2\2\u008a\u008b\7,\2\2\u008b"+
		"\66\3\2\2\2\u008c\u008d\7\61\2\2\u008d8\3\2\2\2\u008e\u008f\7\60\2\2\u008f"+
		":\3\2\2\2\u0090\u0091\7`\2\2\u0091<\3\2\2\2\u0092\u0093\7a\2\2\u0093>"+
		"\3\2\2\2\u0094\u0096\t\2\2\2\u0095\u0094\3\2\2\2\u0096@\3\2\2\2\u0097"+
		"\u0098\4\62;\2\u0098B\3\2\2\2\u0099\u009b\t\3\2\2\u009a\u0099\3\2\2\2"+
		"\u009b\u009c\3\2\2\2\u009c\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009e"+
		"\3\2\2\2\u009e\u009f\b\"\2\2\u009fD\3\2\2\2\5\2\u0095\u009c\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
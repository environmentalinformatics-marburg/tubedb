//derived from https://github.com/antlr/grammars-v4/blob/master/arithmetic/arithmetic.g4
grammar Formula;

expression
   : term (expression_op term)*
   ;

expression_op
	: (ADD | SUB)
	;

term
   : factor (term_op factor)*
   ;
   
term_op
	: (MUL | DIV)
	;

factor
   : atom (POW atom)?
   ;

atom
   : scientific
   | variable
   | LPAREN expression RPAREN
   ;

scientific
   : number (E number)?
   ;

number
   : SUB? DIGIT + (POINT DIGIT +)?
   ;

variable
   : /*SUB?*/ (LETTER | UNDERSCORE) (LETTER | DIGIT | UNDERSCORE)*
   ;


LPAREN
   : '('
   ;


RPAREN
   : ')'
   ;


ADD
   : '+'
   ;


SUB
   : '-'
   ;


MUL
   : '*'
   ;


DIV
   : '/'
   ;


POINT
   : '.'
   ;


E
   : 'e' | 'E'
   ;


POW
   : '^'
   ;

UNDERSCORE
	: '_'
	;

LETTER
   : ('a' .. 'z') | ('A' .. 'Z')
   ;


DIGIT
   : ('0' .. '9')
   ;


WS
   : [ \r\n\t] + -> channel (HIDDEN)
;
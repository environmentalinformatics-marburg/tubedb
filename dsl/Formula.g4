/*
derived from https://github.com/antlr/grammars-v4/blob/master/arithmetic/arithmetic.g4  
 
BSD License
Copyright (c) 2013, Tom Everett
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of Tom Everett nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

grammar Formula;
  
expression
   : term (expression_op term)*
   | conditional
   ;
   
expression_op
	: (ADD | SUB)
	;
	
conditional
	: LPAREN p=predicate '?' a=expression ':' b=expression RPAREN
	;
	
predicate
    : less
    | greater
    | less_equal
    | greater_equal
    | equal
    | not_equal
    | not
    ;
    
less
    : a=expression '<' b=expression
    ;
    
greater
    : a=expression '<' b=expression
    ;    

less_equal
    : a=expression '<=' b=expression
    ;
    
greater_equal
    : a=expression '>=' b=expression
    ;
    
equal
    : a=expression '=''='? b=expression
    ;
    
not_equal
    : a=expression ('!=' | '<>') b=expression
    ;
    
not
    : '!' a=predicate
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
   : number /*(E number)?*/
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


/*E
   : 'e' | 'E'
   ;*/


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
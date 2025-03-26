lexer grammar CoolLexer;

@header{
    package cool.lexer;
}

tokens { ERROR }

@members{    
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}

CLASS : 'class' ;
ELSE : 'else' ;
FI : 'fi' ;
IF : 'if' ;
IN : 'in' ;
INHERITS : 'inherits' ;
ISVOID : 'isvoid' ;
LET : 'let' ;
LOOP : 'loop' ;
POOL : 'pool' ;
THEN : 'then' ;
WHILE : 'while' ;
CASE : 'case' ;
ESAC : 'esac' ;
NEW : 'new' ;
OF : 'of' ;
NOT : 'not' ;
fragment TRUE : 'true' ;
fragment FALSE : 'false' ;
BOOL : TRUE | FALSE;
TYPE : [A-Z][a-zA-Z0-9_]* ;
ID : [a-z][a-zA-Z0-9_]* ;
INT : [0-9]+ ;
STRING : '"' ('\\"' | '\u0000'  {
raiseError("String contains null character");
}   | .)*? ('"' {
 if (getText().length() > 1026) raiseError("String constant too long");
}
 |
 EOF  {
raiseError("EOF in string constant");
}| ~'\\' '\n' {
 raiseError("Unterminated string constant");
 });


LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
COLON : ':' ;
SEMICOLON : ';' ;
COMMA : ',' ;
DOT : '.' ;
PLUS : '+' ;
MINUS : '-' ;
STAR : '*' ;
AT : '@' ;
SLASH : '/' ;
TILDE : '~' ;
LE : '<=' ;
LT : '<' ;
EQUALS : '=' ;
ASSIGN : '<-';
CASE_ARROW : '=>' ;

fragment NEW_LINE : '\r'? '\n';

LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip
    ;

BLOCK_COMMENT
    :   '(*' (BLOCK_COMMENT | .)*? ('*)'{skip();} | EOF { raiseError("EOF in comment"); })
    ;

WS
    :   [ \n\f\r\t]+ -> skip
    ; 

//bad
BAD
    : '*)' {raiseError("Unmatched *)");}
    ;

// Rule to match any remaining invalid characters
INVALID_CHAR
    : . { raiseError("Invalid character: " + getText()); }
    ;
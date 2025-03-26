lexer grammar CoolLexer;

tokens { ERROR } 

@header {
package cool.lexer;
}

@members {
private void raiseError(String msg) {
    setText(msg);
    setType(ERROR);
}
}

// For case insensivity
fragment A:[aA];
fragment B:[bB];
fragment C:[cC];
fragment D:[dD];
fragment E:[eE];
fragment F:[fF];
fragment G:[gG];
fragment H:[hH];
fragment I:[iI];
fragment J:[jJ];
fragment K:[kK];
fragment L:[lL];
fragment M:[mM];
fragment N:[nN];
fragment O:[oO];
fragment P:[pP];
fragment Q:[qQ];
fragment R:[rR];
fragment S:[sS];
fragment T:[tT];
fragment U:[uU];
fragment V:[vV];
fragment W:[wW];
fragment X:[xX];
fragment Y:[yY];
fragment Z:[zZ];

fragment ENDL : '\r'? '\n' ;

// Keywords
CLASS : C L A S S ;
INHERITS : I N H E R I T S ;
NEW : N E W ;

IF : I F ;
THEN : T H E N ;
ELSE : E L S E ;
FI : F I ;

LET : L E T ;
IN : I N ;

CASE : C A S E ;
OF : O F ;
ESAC : E S A C ;

LOOP : L O O P ;
POOL : P O O L ;
WHILE : W H I L E ;

ISVOID : I S V O I D ;
NOT : N O T ;

// Other special characters
COMPLEMENT : '~' ;
ADD : '+' ;
SUBTRACT : '-' ;
MULTIPLY : '*' ;
DIVIDE : '/' ;

EQ : '=' ;
LESS : '<' ;
LESS_EQ : '<=' ;

LPAREN : '(' ;
RPAREN : ')' ;
LCURLY : '{' ;
RCURLY : '}' ;

OF_TYPE : ':' ;
AT : '@' ;
DOT : '.' ;
COMMA : ',' ;
CASE_ARROW : '=>' ;
ASSIGN : '<-' ;
SEMICOLON : ';' ;

// Literals
INTEGER : [0-9]+ ;
TRUE : 't' R U E ;
FALSE : 'f' A L S E ;

// Types and IDs
TYPE : [A-Z][a-zA-Z0-9_]* ; // this includes 'SELF_TYPE'
ID : [a-z][a-zA-Z0-9_]* ;   // this includes 'self'

// Whitespace
WS : [ \n\f\r\t]+ -> skip;

// Comments
LINE_COMMENT : '--' ~[\r\n]* -> skip;
BLOCK_COMMENT : '(*' (EOF_BLOCK_COMMENT | BLOCK_COMMENT | '*'+ ~[*)] | ~'*')* '*'+ ')' -> skip;
EOF_BLOCK_COMMENT : '(*' (EOF_BLOCK_COMMENT | BLOCK_COMMENT | '*'+ ~[*)] | ~'*')* EOF { raiseError("EOF in comment"); } ;
UNMATCHED_BLOCK_COMMENT : '*)' { raiseError("Unmatched *)"); } ;

// Strings
STRING : '"' ('\\' (ENDL | .) | ~[\r\n"])* '"' {
var content = getText();
// Remove leading & trailing quote character
content = content.substring(1, content.length() - 1);
// Parse escaped
var builder = new StringBuilder(content.length());
boolean escaping = false;
for (int i = 0; i < content.length(); ++i) {
    char c = content.charAt(i);
    if (escaping) {
        switch (c) {
            case 'n':
                builder.append('\n');
                break;
            case 't':
                builder.append('\t');
                break;
            case 'b':
                builder.append('\b');
                break;
            case 'f':
                builder.append('\f');
                break;
            default:
                builder.append(c);
                break;
        }
        escaping = false;
    } else if (c == '\\') escaping = true;
    else builder.append(c);
}
content = builder.toString();
// Verify string literal constraints
if (content.indexOf('\0') != -1) raiseError("String contains null character");
else if (content.length() > 1024) raiseError("String constant too long");
else setText(content);
};

UNTERMINATED_STRING : '"' ('\\' (ENDL | .) | ~[\r\n"])* [\r\n] { raiseError("Unterminated string constant"); } ;

EOF_STRING : '"' ('\\' . | ~[\r\n"])* EOF { raiseError("EOF in string constant"); } ;

// Unknown character

UNKNOWN_CHARACTER : . { raiseError("Invalid character: " + getText()); };
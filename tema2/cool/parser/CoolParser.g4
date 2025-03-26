parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

tokens { NONE }

@header{
    package cool.parser;
}

program : class+ EOF ;

class : CLASS name=TYPE (INHERITS parent=TYPE)? LCURLY feature* RCURLY SEMICOLON;

feature
    : ID LPAREN (args+=formal (COMMA args+=formal)*)? RPAREN OF_TYPE TYPE
            LCURLY expr RCURLY SEMICOLON                                    # method
    | ID OF_TYPE TYPE (ASSIGN expr)? SEMICOLON                              # attribute ;

formal : ID OF_TYPE TYPE ;

local : ID OF_TYPE TYPE (ASSIGN expr)? ;

case_branch : ID OF_TYPE TYPE CASE_ARROW expr SEMICOLON;

expr
    : method=ID LPAREN (args+=expr (COMMA args+=expr)*)? RPAREN                     # selfMethodCall
    | obj=expr (AT type=TYPE)? DOT method=ID LPAREN
            (args+=expr (COMMA args+=expr)*)? RPAREN                                # methodCall
    | op=COMPLEMENT expr                                                            # unary
    | ISVOID expr                                                                   # isvoid
    | left=expr op=(MULTIPLY | DIVIDE) right=expr                                   # arithmetic
    | left=expr op=(ADD | SUBTRACT) right=expr                                      # arithmetic
    | left=expr op=(LESS | LESS_EQ | EQ) right=expr                                 # comparison
    | NOT expr                                                                      # negate
    | ID ASSIGN expr                                                                # varAssign

    | IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI                     # if
    | WHILE cond=expr LOOP body=expr POOL                                           # while
    | LET vars+=local (COMMA vars+=local)* IN body=expr                             # let
    | CASE expr OF case_branch+ ESAC                                                # case
    | LCURLY (body+=expr SEMICOLON)+ RCURLY                                         # block
    | NEW TYPE                                                                      # instantiation
    | LPAREN expr RPAREN                                                            # expression
    | ID                                                                            # var
    | INTEGER                                                                       # literalInteger
    | STRING                                                                        # literalString
    | TRUE                                                                          # literalTrue
    | FALSE                                                                         # literalFalse ;


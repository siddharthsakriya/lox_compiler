package lox;

import java.util.List;

import static lox.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    private static class ParseError extends RuntimeException {}

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while(match(BANG_EQUAL) || match(EQUAL_EQUAL)){
            Token operator = consume();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while(match(GREATER_EQUAL) || match(LESS_EQUAL) || match(LESS) || match(GREATER)){
            Token operator = consume();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term(){
        Expr expr = factor();
        while(match(MINUS) || match(PLUS)){
            Token operator = consume();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor(){
        Expr expr = unary();
        while (match(SLASH) || match(STAR)){
            Token operator = consume();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary(){
        if (match(BANG) || match(MINUS)){
            Token operator = consume();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary(){

        if (match(FALSE)) {
            consume();
            return new Expr.Literal(false);
        };
        if (match(TRUE)) {
            consume();
            return new Expr.Literal(true);
        };
        if (match(NIL)) {
            consume();
            return new Expr.Literal(null);
        };

        if (match(NUMBER) || match(STRING)) {
            return new Expr.Literal(consume().literal);
        };

        if (match(LEFT_PAREN)){
            consume();
            Expr expr = expression();
            if (match(RIGHT_PAREN)){
                consume();
            }
            else {
                throw error(peak(0), "Expected ')' after expression. ");
            }
            return new Expr.Grouping(expr);
        }

        System.out.println(match(NUMBER));
        throw error(peak(0), "Expect expression.");

    }


    private Token consume() {
        return tokens.get(current ++);
    }

    private Token peak(int lookahead){
        return tokens.get(current  + lookahead);
    }

    private boolean atEnd(){
        return peak(0).type == EOF;
    }

    private boolean match(TokenType type){
        if (atEnd()) return false;
        return type == peak(0).type;
    }

    private ParseError error (Token token, String errorMsg){
        Lox.error(token, errorMsg);
        return new ParseError();
    }

    private void synchronize() {
        consume();

        while (!atEnd()) {
            if (peak(-1).type == SEMICOLON) return;

            switch (peak(0).type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            consume();
        }
    }

}

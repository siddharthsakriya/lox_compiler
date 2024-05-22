package lox;

import java.util.List;

import static lox.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
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
                System.err.println("Missing right parenthesis");
            }
            return new Expr.Grouping(expr);
        }
        return null;
    }


    private Token consume() {
        return tokens.get(current ++);
    }

    private Token peak(int lookahead){
        return tokens.get(current  + lookahead);
    }

    private boolean atEnd(){
        return current < tokens.size();
    }

    private boolean match(TokenType type){
        if (atEnd()) return false;
        return type == peak(0).type;
    }
    
}

package lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import static lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }
    private int start = 0;
    private int curr = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!atEnd()){
            start = curr;
            scanToken();
        }
        Token end_token = new Token(EOF, "", null, line);
        tokens.add(end_token);
        return tokens;
    }

    private boolean atEnd() {
        return curr >= source.length();
    }

    private void scanToken() {
        char c = consume();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': if (matchAhead('=')) addToken(BANG_EQUAL); else addToken(BANG); break;
            case '=': if (matchAhead('=')) addToken(EQUAL_EQUAL); else addToken(EQUAL); break;
            case '<': if (matchAhead('=')) addToken(LESS_EQUAL); else addToken(LESS); break;
            case '>': if (matchAhead('=')) addToken(GREATER_EQUAL); else addToken(GREATER); break;
            case '/': if (matchAhead('/')) comment(); else addToken(SLASH); break;
            case ' ':
            case '\r':
            case '\t':
            case '"': string(); addToken(STRING, source.substring(start + 1, curr - 1)); break;
            default:
                if (isDigit(c)){
                    num();
                    addToken(STRING, Double.parseDouble(source.substring(start, curr)));
                    break;
                }
                else if (isAlpha(c)){
                    identifier();
                    break;
                }
                Lox.error(line, "Unexpected character.");
                break;
        }
    }

    private Boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private Boolean isAlpha(char c) {
        return c >= 'a' && c <= 'z'||
                c >= 'A' && c <= 'Z'||
                c == '_';
    }

    private char consume() {
        return source.charAt(curr++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, curr);
        tokens.add(new Token(type, text, literal, line));
    }


    //peak & match ahead cases//

    private Boolean matchAhead(char s) {
        if (!atEnd()){
            if (source.charAt(curr) == s) {
                curr++;
                return true;
            }
        }
        return false;
    }

    //peak but with as much lookahead as we like
    private char peak(int lookahead){
        int val = curr + lookahead;
        if (val >= source.length()) return '\0';
        return source.charAt(val);
    }

    private void comment(){
        while (peak(0) != '\n' && !atEnd()){
            consume();
        }
    }

    private void string() {
        while (peak(0) != '"' && !atEnd()){
            if (source.charAt(curr) == '\n') line++;
            consume();
        }
        if (atEnd()){
            Lox.error(line, "Unexpected end of string");
            return;
        }
        consume(); //final " needs to be closed
    }

    private void num(){
        while (isDigit(peak(0)) ){
            consume();
        }
        if(peak(0) == '.' && isDigit(peak(1))){
            //consume the "."
            do {
                consume();
            } while (isDigit(peak(0)));
        }
    }

    private void identifier(){
        while (isAlpha(peak(0)) || isDigit(peak(0))){
            consume();
        }
        String identifier_string = source.substring(start, curr);
        if (keywords.containsKey(identifier_string)){
            addToken(keywords.get(identifier_string));
            return;
        }
        addToken(IDENTIFIER);
        return;
    }

}

package com.github.plugatarev.bencode.lexer;
import com.github.plugatarev.bencode.error.ErrorReporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final BufferedReader br;
    private final List<Token> tokens = new ArrayList<>();
    private final int nLine = 1;
    private final ErrorReporter reporter;

    private Lexer(BufferedReader br, ErrorReporter reporter) {
        this.br = br;
        this.reporter = reporter;
    }

    public static List<Token> scan(BufferedReader br, ErrorReporter reporter) {
        Lexer lexer = new Lexer(br, reporter);
        return lexer.scan();
    }

    private List<Token> scan() {
        String line;
        if ((line = getLine()) == null || line.isBlank()) {
            tokens.add(new Token(TokenType.EOF, nLine, -1, nLine));
            return tokens;
        }
        int lastNumber = 0;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            TokenType type = getTokenType(c);
            if (type == TokenType.INTEGER_BEGIN) {
                tokens.add(new Token(TokenType.INTEGER_BEGIN, nLine, i, 'i'));
                if ((i = number(++i, line, TokenType.INTEGER)) == -1) return null;
                continue;
            }
            if (type == TokenType.STRING) {
                if ((i = string(i, line, lastNumber)) == -1) return null;
                continue;
            }
            if (isDigit(c)){
                if ((i = number(i, line, TokenType.STRING_BEGIN)) == -1) return null;
                lastNumber = getLastToken() == null ? 0 : (Integer)getLastToken().value();
                continue;
            }
            if (type == null) {
                if (!reporter.report(LexerError.UNKNOWN_CHAR.getErrorMessage(line, i, c))) return null;
            } else {
                tokens.add(new Token(type, nLine, i, c));
            }
            i++;
        }
        tokens.add(new Token(TokenType.EOF, nLine, -1, nLine));
        return reporter.hasError() ? null : tokens;
    }

    private static boolean isSeparator(Token token){
        return token != null && token.tokenType() == TokenType.SEPARATOR;
    }

    private static boolean isAscii(char c){
        return (int)c <= 127;
    }

    private TokenType getTokenType(char c){
        Token lastToken = getLastToken();
        if (isSeparator(lastToken) && isAscii(c)) {
            return TokenType.STRING;
        }
        return switch (c){
            case ':' -> TokenType.SEPARATOR;
            case 'i' -> TokenType.INTEGER_BEGIN;
            case 'd' -> TokenType.DICTIONARY;
            case 'l' -> TokenType.LIST;
            case 'e' -> TokenType.END_TYPE;
            default -> null;
        };
    }

    private Token getLastToken() {
        return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
    }

    private String getLine() {
        try {
            return br.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int number(int i, String line, TokenType type) {
        int start = i;
        do {
            i++;
        } while (i < line.length() && Character.isDigit(line.charAt(i)));
        int value;
        String number = line.substring(start, i);
        try{
            if (number.length() > 1 && (number.charAt(0) == '0' || number.charAt(0) == '-' && number.charAt(1) == '0'))
                throw new NumberFormatException("zeros");
            value = Integer.parseInt(number);
        }catch(NumberFormatException e){
           LexerError error;
           if (e.getMessage().equals("zeros")) error = LexerError.NUMBER_WITH_DEAD_ZEROS;
           else error = LexerError.INCORRECT_NUMBER;
           if (!reporter.report(error.getErrorMessage(line, start, number))) return -1;
           return number.length();
        }
        tokens.add(new Token(type, nLine, start, value));
        return i;
    }

    private int string(int i, String line, int length){
        if (length == 0 || i + length > line.length()){
            if (!reporter.report(LexerError.INCORRECT_STRING_LENGTH.getErrorMessage(line, i, length))) return -1;
            return length == 0 ? -1 : i + length;
        }
        for (int pos = i; pos < i + length; pos++){
            if (!isAscii(line.charAt(pos))){
                if (!reporter.report(LexerError.UNKNOWN_CHAR.getErrorMessage(line, i, length))) return -1;
                return ++i;
            }
        }
        String value = line.substring(i, i + length);
        tokens.add(new Token(TokenType.STRING, 1, i - length, value));
        return i + length;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}

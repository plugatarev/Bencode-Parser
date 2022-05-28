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
    private boolean hasErrors = false;

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
                String error = getErrorMessage(ErrorType.UNKNOWN_CHAR, line, i, c);
                if (!reporter.report(error)) {
                    return null;
                }
                hasErrors = true;
            } else {
                tokens.add(new Token(type, nLine, i, c));
            }
            i++;
        }
        tokens.add(new Token(TokenType.EOF, nLine, -1, nLine));
        return hasErrors ? null : tokens;
    }

    //OK CR: only difference is the first line, merge errors into one method
    private String getErrorMessage(ErrorType type, String line, int pos, Object obj) {
        String errorMessage = null;
        switch (type) {
            case UNKNOWN_CHAR -> errorMessage = "Unknown char '%c' at line %d:\n%s\n%s^--- here\n";
            case INCORRECT_NUMBER -> errorMessage = "Incorrect number %s at line %d:\n%s\n%s^--- here\n";
            case INCORRECT_STRING_LENGTH -> errorMessage = "Expected string of length %d at line %d,\n%s\n%s^--- here\n";
            case NUMBER_WITH_DEAD_ZEROS -> errorMessage = "Number %s cannot have leading zeros at line %d:\n%s\n%s^--- here\n";
        }
        return errorMessage.formatted(obj, nLine, line, " ".repeat(pos));
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

    private int reportAndGetPosition(ErrorType type, String line, int startPos, Object value){
        if (!reporter.report(getErrorMessage(type, line, startPos, value))) return -1;
        hasErrors = true;
        if (type == ErrorType.INCORRECT_STRING_LENGTH || type == ErrorType.UNKNOWN_CHAR) return startPos + (Integer) value;
        return ((String) value).length();
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
           ErrorType errorType;
           if (e.getMessage().equals("zeros")) errorType = ErrorType.NUMBER_WITH_DEAD_ZEROS;
           else errorType = ErrorType.INCORRECT_NUMBER;
           return reportAndGetPosition(errorType, line, start, number);
        }
        tokens.add(new Token(type, nLine, start, value));
        return i;
    }

    private int string(int i, String line, int length){
        if (length == 0 || i + length > line.length())
            return reportAndGetPosition(ErrorType.INCORRECT_STRING_LENGTH, line, i, length);
        for (int pos = i; pos < i + length; pos++){
            if (!isAscii(line.charAt(pos))) return reportAndGetPosition(ErrorType.UNKNOWN_CHAR, line, i, length);
        }
        //OK CR: forgot to check that all the chars are ascii
        String value = line.substring(i, i + length);
        tokens.add(new Token(TokenType.STRING, 1, i - length, value));
        return i + length;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}

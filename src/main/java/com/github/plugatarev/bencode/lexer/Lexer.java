package com.github.plugatarev.bencode.lexer;
import com.github.plugatarev.bencode.error.ErrorReporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final BufferedReader br;
    private final List<Token> tokens = new ArrayList<>();
    private int nLine;
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
        int lastNumber = 0;
        while ((line = getLine()) != null) {
            nLine++;
            int i = 0;
            while (i < line.length()) {
                char c = line.charAt(i);
                TokenType type = getTokenType(c);
                if (type == TokenType.INTEGER_BEGIN) {
                    tokens.add(new Token(TokenType.INTEGER_BEGIN, nLine, i, 'i'));
                    //FIX CR: no need to pass both 'i' and 'start', it is enough to have only one of them
                    //FIX CR: add test that verifies that negative number is handled as expected
                    if ((i = number(++i, line, TokenType.INTEGER)) == -1) return null;
                    continue;
                }
                if (type == TokenType.STRING) {
                    if ((i = string(i, line, lastNumber, nLine)) == -1) return null;
                    continue;
                }
                if (isDigit(c)){
                    // OK CR: add test that verifies that negative length is handled as expected
                    if ((i = number(i, line, TokenType.STRING_BEGIN)) == -1) return null;
                    lastNumber = getLastToken() == null ? 0 : (Integer)getLastToken().value();
                    continue;
                }
                if (type == null) {
                    String error = unknownChar(line, i, c);
                    if (!reporter.report(error)) {
                        return null;
                    }
                    hasErrors = true;
                } else {
                    tokens.add(new Token(type, nLine, i,c));
                }
                i++;
            }
            tokens.add(new Token(TokenType.EOL, nLine, -1, nLine));
        }
        tokens.add(new Token(TokenType.EOF, nLine, -1, nLine));
        return hasErrors ? null : tokens;
    }

    private String unknownChar(String line, int pos, char c) {
        return """
                Unknown char '%c' at line %d:
                %s
                %s^--- here
                """.formatted(c, nLine, line, " ".repeat(pos));
    }

    private String incorrectNumber(String line, int pos, String number){
        return """
                Incorrect number %s at line %d:
                %s
                %s^--- here
                """.formatted(number, nLine, line, " ".repeat(pos));
    }

    private String incorrectStringLength(String line, int pos, int length){
        return """
                Expected string of length %d at line %d,
                %s
                %s^--- here"""
                .formatted(length, nLine, line, " ".repeat(pos));
    }

    boolean isString(Token lastToken, char start){
        return (lastToken != null && lastToken.tokenType() == TokenType.SEPARATOR && (int)start <= 127);
    }

    private TokenType getTokenType(char c){
        Token lastToken = getLastToken();
        //FIX CR: c <= 127, also move this check to separate method for clarity
        if (isString(lastToken, c)) {
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
            throw new RuntimeException(e);
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
            value = Integer.parseInt(number);
            if ((value == 0 || number.charAt(0) == '0') && number.length() > 1) throw new NumberFormatException();
        }catch(NumberFormatException e){
            //FIX CR: it is not an unknown char, it is another type of error
            if (!reporter.report(incorrectNumber(line, start, number))) {
                return -1;
            }
            hasErrors = true;
            return i;
        }
        tokens.add(new Token(type, nLine, start, value));
        return i;
    }

    private int string(int i, String line, int length, int nLine){
        final String value;
        if (length != 0 && i + length <= line.length()) {
            value = line.substring(i, i + length);
        }
        else {
            //FIX CR: another type of error
            if (!reporter.report(incorrectStringLength(line, i, length))) {
                return -1;
            }
            hasErrors = true;
            return i + length;
        }
        tokens.add(new Token(TokenType.STRING, nLine, i - length, value));
        return i + length;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}

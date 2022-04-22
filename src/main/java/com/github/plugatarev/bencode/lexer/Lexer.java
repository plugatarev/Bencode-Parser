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
                    int start = ++i;
                    if ((i = number(i, line, start, TokenType.INTEGER)) == -1) return null;
                    continue;
                }
                if (type == TokenType.STRING) {
                    if ((i = string(i, line, lastNumber, nLine)) == -1) return null;
                    continue;
                }
                if (isDigit(c)){
                    int start = i;
                    if ((i = number(i, line, start, TokenType.STRING_BEGIN)) == -1) return null;
                    lastNumber = (Integer)tokens.get(tokens.size() - 1).value();
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



    private TokenType getTokenType(char c){
        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).tokenType() == TokenType.SEPARATOR && (int)c <= 255)
            return TokenType.STRING;
        return switch (c){
            case ':' -> TokenType.SEPARATOR;
            case 'i' -> TokenType.INTEGER_BEGIN;
            case 'd' -> TokenType.DICTIONARY;
            case 'l' -> TokenType.LIST;
            case 'e' -> TokenType.END_TYPE;
            default -> null;
        };
    }

    private String getLine() {
        try {
            return br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int number(int i, String line, int start, TokenType type) throws NumberFormatException{
        do {
            i++;
        } while (i < line.length() && Character.isDigit(line.charAt(i)));
        int value;
        try{
            value = Integer.parseInt(line.substring(start, i));
        }catch(NumberFormatException e){
            if (!reporter.report(unknownChar(line, start - 1, 'i'))) {
                return -1;
            }
            hasErrors = true;
            return i;
        }
        tokens.add(new Token(type, nLine, start, value));
        return i;
    }

    private int string(int i, String line, int length, int nLine){
        String value = "";
        if (length != 0 && i + length <= line.length()) value = line.substring(i, i + length);
        else {
            if (!reporter.report(unknownChar(line, i, line.charAt(i)))) {
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

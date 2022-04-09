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
        boolean hasErrors = false;
        int lastNumber = -1;
        while ((line = getLine()) != null) {
            nLine++;
            int i = 0;
            while (i < line.length()) {
                char c = line.charAt(i);
                TokenType type = getTokenType(c);
                if (type == TokenType.INTEGER_BEGIN) {
                    tokens.add(new Token(TokenType.INTEGER_BEGIN, nLine, i, 'i'));
                    int start = ++i;
                    i = number(i, line, start, TokenType.INTEGER);
                    continue;
                }
                if (isDigit(c)){
                    int start = i;
                    i = number(i, line, start, TokenType.STRING_BEGIN);
                    // CR: double work
                    lastNumber = Integer.parseInt(line.substring(start, i));
                    continue;
                }
                if (Character.isAlphabetic(c) && type == null) {
                    i = string(i, line, lastNumber, nLine);
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
        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).tokenType() == TokenType.SEPARATOR) return null;
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

    private int number(int i, String line, int start, TokenType type) {
        do {
            i++;
        } while (i < line.length() && Character.isDigit(line.charAt(i)));
        // CR: 121212312421423423423523564325243
        tokens.add(new Token(type, nLine, start, Integer.parseInt(line.substring(start, i))));
        return i;
    }

    private int string(int i, String line, int length, int nLine){
        // CR: support 1:$ + test 1:✨ ??
        // CR: substring
        int j = 1;
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(line.charAt(i));
            i++;
        } while (i != line.length() && Character.isAlphabetic(line.charAt(i)) && j++ < length);
        String value = sb.toString();
        tokens.add(new Token(TokenType.STRING, nLine, i - length, value));
        return i;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}

package com.github.plugatarev.bencode.parser;

import com.github.plugatarev.bencode.error.ErrorReporter;
import com.github.plugatarev.bencode.lexer.Token;
import com.github.plugatarev.bencode.lexer.TokenType;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private final ErrorReporter errorReporter;
    private int pos;

    private Parser(List<Token> tokens, ErrorReporter errorReporter) {
        this.tokens = tokens;
        this.errorReporter = errorReporter;
    }

    public static List<Element> parse(List<Token> tokens, ErrorReporter errorReporter) {
        Parser parser = new Parser(tokens, errorReporter);
        return parser.parse();
    }

    private List<Element> parse() {
        boolean hasErrors = false;
        List<Element> members = new ArrayList<>();
        while (!matches(TokenType.EOF)) {
            try {
                // CR: only one root element, check that file has ended
                Element expr = null;
                if (tokens.get(pos).tokenType() != TokenType.EOL) {
                    expr = parseElement(0);
                }
                consume(TokenType.EOL);
                if (expr == null) continue;
                members.add(expr);
            } catch (ParserException e) {
                hasErrors = true;
                if (!errorReporter.report(e.getMessage())) {
                    return null;
                }
            }
        }
        return hasErrors ? null : members;
    }

    private Element parseElement(int nestingLevel) {
        TokenType type = tokens.get(pos).tokenType();
        if (type == TokenType.EOL) consume(TokenType.EOL);
        return switch (type){
            case DICTIONARY -> parseDictionary(nestingLevel + 1);
            case LIST -> parseList(nestingLevel);
            case INTEGER_BEGIN -> parseInteger();
            case STRING_BEGIN -> parseString();
            default -> {
                String error = "Expected one of token types: " + Arrays.toString(TokenType.values()) + ", got " + type;
                throw new IllegalStateException(error);
            }
        };
    }

    private Element parseList(int nestingLevel) {
        List<Element> values = new ArrayList<>();
        advance();
        while (!matches(TokenType.END_TYPE)){
            Element newMember = parseElement(nestingLevel);
            values.add(newMember);
        }
        advance();
        return new Element.BArray(values);
    }

    // di32ei45ee
    private Element parseDictionary(int nestingLevel) {
        Map<Element, Element> dict = new HashMap<>();
        advance();
        while (!matches(TokenType.END_TYPE)){
            Element key = parseElement(nestingLevel);
            Element value = parseElement(nestingLevel);
            dict.put(key, value);
        }
        advance();
        return new Element.BDictionary(dict, nestingLevel);
    }

    private Element parseString() {
        advance();
        consume(TokenType.SEPARATOR);
        Token str = tokens.get(pos);
        if (!matches(TokenType.STRING)){
            throw new ParserException(unexpectedToken(str, TokenType.STRING));
        }
        advance();
        return new Element.BString((String) str.value());
    }

    private Element parseInteger() {
        advance();
        Token token = tokens.get(pos);
        if (!matches(TokenType.INTEGER)){
            throw new ParserException(unexpectedToken(token, TokenType.INTEGER));
        }
        advance();
        consume(TokenType.END_TYPE);
        return new Element.BInteger((Integer) token.value());
    }

    private boolean matches(TokenType first, TokenType... rest) {
        Token token = tokens.get(pos);
        TokenType actual = token.tokenType();
        if (actual == first) return true;
        for (TokenType expected : rest) {
            if (actual == expected) return true;
        }
        return false;
    }

    private Token advance() {
        Token token = tokens.get(pos);
        if (token.tokenType() != TokenType.EOF) pos++;
        return token;
    }

    private void consume(TokenType expected) {
        Token token = advance();
        if (token.tokenType() != expected) {
            throw new ParserException(unexpectedToken(token, expected));
        }
    }

    private static String unexpectedToken(Token token, TokenType... expected) {
        int pos = token.pos();
        String position = pos == -1 ?
                "End of line " + token.nLine() :
                "Line " + token.nLine() + ", position: " + pos;
        return """
                %s
                Expected tokens: %s,
                Actual: %s
                """.formatted(position, Arrays.toString(expected), token.tokenType());
    }
}
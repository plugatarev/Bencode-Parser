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

    public static Element parse(List<Token> tokens, ErrorReporter errorReporter) {
        Parser parser = new Parser(tokens, errorReporter);
        return parser.parse();
    }

    private Element parse() {
        Element element = null;
        while (!matches(TokenType.EOF)) {
            try {
                element = parseElement();
            } catch (ParserException e) {
                //OK CR: use ErrorReporter#hasError instead
                if (!errorReporter.report(e.getMessage())) {
                    return null;
                }
            }
        }
        return errorReporter.hasError() ? null : element;
    }

    private Element parseElement() {
        TokenType type = tokens.get(pos).tokenType();
        return switch (type) {
            case DICTIONARY -> parseDictionary();
            case LIST -> parseList();
            case INTEGER_BEGIN -> parseInteger();
            case STRING_BEGIN -> parseString();
            default -> throw new ParserException(unexpectedToken(tokens.get(pos)));
        };
    }

    private Element.BList parseList() {
        List<Element> values = new ArrayList<>();
        advance();
        while (!matches(TokenType.END_TYPE)) {
            Element newMember = parseElement();
            values.add(newMember);
        }
        advance();
        return new Element.BList(values);
    }

    public static boolean isCorrectOrder(Map<Element.BString, Element> dict){
        //OK CR: what if there're 0 elements in dict? better use regular for and null as first element
        Element.BString prev = null;
        for (Element.BString key : dict.keySet()){
            if (prev != null){
                if (key.str().compareTo(prev.str()) < 0) return false;
            }
            prev = key;
        }
        return true;
    }

    private Element.BDictionary parseDictionary() {
        Map<Element.BString, Element> dict = new LinkedHashMap<>();
        Token start = advance();
        while (!matches(TokenType.END_TYPE)) {
            Element.BString key = parseString();
            Element value = parseElement();
            dict.put(key, value);
        }
        advance();
        if (!isCorrectOrder(dict)) throw new ParserException(lexicographicOrder(start));
        return new Element.BDictionary(dict);
    }

    private Element.BString parseString() {
        consume(TokenType.STRING_BEGIN);
        consume(TokenType.SEPARATOR);
        Token str = consume(TokenType.STRING);
        return new Element.BString((String) str.value());
    }

    private Element.BInteger parseInteger() {
        consume(TokenType.INTEGER_BEGIN);
        Token token = consume(TokenType.INTEGER);
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

    private Token consume(TokenType expected) {
        Token token = advance();
        if (token.tokenType() != expected) {
            throw new ParserException(unexpectedToken(token, expected));
        }
        return token;
    }

    private static String lexicographicOrder(Token token){
        int pos = token.pos();
        String position = pos == -1 ?
                "End of line " + token.nLine() :
                "Line " + token.nLine() + ", position: " + ++pos;
        return """
                %s
                The lexicographic order in the dictionary is broken
                """.formatted(position);
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
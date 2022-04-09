package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.error.ErrorReporter;
import com.github.plugatarev.bencode.lexer.Token;
import com.github.plugatarev.bencode.lexer.TokenType;
import com.github.plugatarev.bencode.parser.Element;
import com.github.plugatarev.bencode.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserTest {
    @Test
    public void empty() {
        List<Element> elements = parse(tokens());
        Assert.assertTrue(elements.isEmpty());
    }

    @Test
    public void number() {
        List<Element> elements = parse(createIntegerNumber(12324));
        System.out.println(elements.get(0).toString());
        Assert.assertEquals(new Element.JInteger(12324), elements.get(0));
    }

    @Test
    public void list(){
        List<Element> elements = parse(tokens(new TokenInfo(TokenType.LIST, "l"),
                                              new TokenInfo(TokenType.STRING_BEGIN, "3"),
                                              new TokenInfo(TokenType.SEPARATOR, ":"),
                                              new TokenInfo(TokenType.STRING, "key"),
                                              new TokenInfo(TokenType.STRING_BEGIN, "7"),
                                              new TokenInfo(TokenType.SEPARATOR, ":"),
                                              new TokenInfo(TokenType.STRING, "generic"),
                                              new TokenInfo(TokenType.END_TYPE, null))
        );
        List<Element> members = new ArrayList<>();
        members.add(new Element.JString("key"));
        members.add(new Element.JString("generic"));
        Element.JArray expected = new Element.JArray(members);
        List<Element> expList = new ArrayList<>();
        expList.add(expected);
        Assert.assertEquals(elements, expList);
    }

    @Test
    public void dictionary(){
        List<Element> elements = parse(tokens(new TokenInfo(TokenType.DICTIONARY, "d"),
                new TokenInfo(TokenType.STRING_BEGIN, "3"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "key"),
                new TokenInfo(TokenType.STRING_BEGIN, "7"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "generic"),
                new TokenInfo(TokenType.END_TYPE, null))
        );
        Map<Element, Element> content = new HashMap<>();
        content.put(new Element.JString("key"), new Element.JString("generic"));
        Element.JDictionary expDictionary = new Element.JDictionary(content, 1);
        List<Element> expList = new ArrayList<>();
        expList.add(expDictionary);
        Assert.assertEquals(elements, expList);
    }

    private List<Token> createIntegerNumber(int value){
        return tokens(new TokenInfo(TokenType.INTEGER_BEGIN, null), new TokenInfo(TokenType.INTEGER, value),
                new TokenInfo(TokenType.END_TYPE, null));
    }

    private static List<Token> tokens(TokenInfo... tokenInfos) {
        List<Token> tokens = new ArrayList<>();
        for (TokenInfo tokenInfo : tokenInfos) {
            tokens.add(new Token(tokenInfo.type, -1, -1, tokenInfo.value));
        }
        tokens.add(new Token(TokenType.EOL, -1, -1, null));
        tokens.add(new Token(TokenType.EOF, -1, -1, null));
        return tokens;
    }

    private static List<Element> parse(List<Token> tokens) {
        return Parser.parse(tokens, ErrorReporter.EMPTY);
    }
    private record TokenInfo(TokenType type, Object value){}
}

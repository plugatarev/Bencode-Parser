package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.error.TestReporter;
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
    private final static ErrorReporter errorReporter = new TestReporter();
    @Test
    public void empty() {
        Element elements = parse(tokens());
        Assert.assertNull(elements);
    }

    @Test
    public void number() {
        Element element = parse(createIntegerNumber(12324));
        Assert.assertEquals(new Element.BInteger(12324), element);
    }

    @Test
    public void string(){
        Element element = parse(tokens(new TokenInfo(TokenType.STRING_BEGIN, 5),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "Hello")));
        Element.BString expected = new Element.BString("Hello");
        Assert.assertEquals(element, expected);
    }

    @Test
    public void list(){
        Element element = parse(tokens(new TokenInfo(TokenType.LIST, "l"),
                                              new TokenInfo(TokenType.STRING_BEGIN, "3"),
                                              new TokenInfo(TokenType.SEPARATOR, ":"),
                                              new TokenInfo(TokenType.STRING, "key"),
                                              new TokenInfo(TokenType.STRING_BEGIN, "7"),
                                              new TokenInfo(TokenType.SEPARATOR, ":"),
                                              new TokenInfo(TokenType.STRING, "generic"),
                                              new TokenInfo(TokenType.END_TYPE, null))
        );

        List<Element> members = new ArrayList<>();
        members.add(new Element.BString("key"));
        members.add(new Element.BString("generic"));
        Element.BList expected = new Element.BList(members);
        Assert.assertEquals(element, expected);
    }

    @Test
    public void listWithoutEndSymbol(){
        Element element = parse(tokens(new TokenInfo(TokenType.LIST, "l"),
                new TokenInfo(TokenType.STRING_BEGIN, "3"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "key"),
                new TokenInfo(TokenType.STRING_BEGIN, "7"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "generic")
        ));
        Assert.assertNull(element);
    }

    @Test
    public void dictionary(){
        Element element = parse(tokens(new TokenInfo(TokenType.DICTIONARY, "d"),
                new TokenInfo(TokenType.STRING_BEGIN, "3"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "key"),
                new TokenInfo(TokenType.STRING_BEGIN, "7"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "generic"),
                new TokenInfo(TokenType.END_TYPE, null))
        );
        Map<Element.BString, Element> dict = new HashMap<>();
        dict.put(new Element.BString("key"), new Element.BString("generic"));
        Element.BDictionary expected = new Element.BDictionary(dict);
        Assert.assertEquals(element, expected);
    }

    @Test
    public void dictionaryWithIncorrectLexicographicOrder(){
        Element element = parse(tokens(new TokenInfo(TokenType.DICTIONARY, "d"),
                new TokenInfo(TokenType.STRING_BEGIN, "3"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "key"),
                new TokenInfo(TokenType.STRING_BEGIN, "7"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "generic"),
                new TokenInfo(TokenType.STRING_BEGIN, "7"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "generic"),
                new TokenInfo(TokenType.STRING_BEGIN, "3"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "key"),
                new TokenInfo(TokenType.END_TYPE, null))
        );
        Assert.assertNull(element);
    }

    @Test
    public void dictionaryKeyNotString(){
        Element element = parse(tokens(new TokenInfo(TokenType.DICTIONARY, "d"),
                new TokenInfo(TokenType.INTEGER_BEGIN, "i"),
                new TokenInfo(TokenType.INTEGER, 123),
                new TokenInfo(TokenType.END_TYPE, null),
                new TokenInfo(TokenType.STRING_BEGIN, "7"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "generic"),
                new TokenInfo(TokenType.END_TYPE, null))
        );
        Assert.assertNull(element);
    }

    @Test
    public void dictionaryWithoutEndSymbol(){
        Element element = parse(tokens(new TokenInfo(TokenType.DICTIONARY, "d"),
                new TokenInfo(TokenType.STRING_BEGIN, "7"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "generic"),
                new TokenInfo(TokenType.STRING_BEGIN, "3"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "key")
        ));
        Assert.assertNull(element);
    }

    @Test
    public void dictionaryWithIncorrectNumbersElements(){
        Element element = parse(tokens(new TokenInfo(TokenType.DICTIONARY, "d"),
                new TokenInfo(TokenType.STRING_BEGIN, "3"),
                new TokenInfo(TokenType.SEPARATOR, ":"),
                new TokenInfo(TokenType.STRING, "key")
        ));
        Assert.assertNull(element);
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
        tokens.add(new Token(TokenType.EOF, -1, -1, null));
        return tokens;
    }

    private static Element parse(List<Token> tokens) {
        return Parser.parse(tokens, errorReporter);
    }

    private record TokenInfo(TokenType type, Object value){}
}

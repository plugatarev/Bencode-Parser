package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.error.ConsoleReporter;
import com.github.plugatarev.bencode.error.ErrorReporter;
import com.github.plugatarev.bencode.lexer.Lexer;
import com.github.plugatarev.bencode.lexer.Token;
import com.github.plugatarev.bencode.parser.Element;
import com.github.plugatarev.bencode.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class IntegrationTest {

    ErrorReporter reporter = new ConsoleReporter();

    void testLexerException(String input, String expected){
        Reader reader = new StringReader(input);
        BufferedReader br = new BufferedReader(reader);
        try {
            Lexer.scan(br, reporter);
        }
        catch (Exception e){
            Assert.assertEquals(e.getMessage(), expected);
        }
    }

    void test(String input, String expected){
        Reader reader = new StringReader(input);
        BufferedReader br = new BufferedReader(reader);
        List<Token> tokens = Lexer.scan(br, reporter);
        Element bTokens = Parser.parse(tokens, new ConsoleReporter());
        JsonConverter converter = new JsonConverter();
        String result = converter.json(bTokens);
        Assert.assertEquals(result, expected);
    }

    @Test
    public void string(){
        String expected = "\"input\"";
        String input = "5:input";
        test(input, expected);
    }

    @Test
    public void number(){
        String input = "i1234e";
        String expected = "1234";
        test(input, expected);
    }

    @Test
    public void list(){
        String input = "l3:1233:key5:he||0e";
        String expected = "[\"123\", \"key\", \"he||0\"]";
        test(input, expected);
    }

    @Test
    public void dictionary(){
        String input = "d3:bar4:spam3:fooi42ee";
        String expected = """
                {
                  "bar": "spam"
                  "foo": 42
                }""";
        test(input, expected);
    }

    @Test
    public void allTypes(){
        String input = "d3:cow3:moo3:keyd2:bb3:hhh2:ffd4:keysl1:i1:d1:l1:eeee4:spaml4:infoi343242e5:6$%!$ee";
        String expected = """
                {
                  "cow": "moo"
                  "key":\s
                  {
                    "bb": "hhh"
                    "ff":\s
                    {
                      "keys": ["i", "d", "l", "e"]
                    }
                  }
                  "spam": ["info", 343242, "6$%!$"]
                }""";
        test(input, expected);
    }

    @Test
    public void strangeSymbol(){
        String input = "5:1e$@e*";
        String expected = """
                             Unknown char '*' at line 1:
                             5:1e$@e*
                                    ^--- here""";
        testLexerException(input, expected);
    }

    @Test
    public void incorrectLengthOfString(){
        String input = "5:1e$@";
        String expected = """
                             Unknown char '1' at line 1:
                             5:1e$@
                               ^--- here""";
        testLexerException(input, expected);
    }
}

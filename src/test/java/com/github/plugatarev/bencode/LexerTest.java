package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.lexer.Lexer;
import com.github.plugatarev.bencode.lexer.Token;
import com.github.plugatarev.bencode.lexer.TokenType;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class LexerTest {
    private static final TestReporter errorReporter = new TestReporter();

    private static List<TokenType> scan(String expressions) {
        List<Token> tokens = scant(expressions);
        errorReporter.clear();
        return tokens == null ? null : tokens.stream().map(Token::tokenType).toList();
    }

    private static List<Token> scant(String expressions) {
        BufferedReader br = new BufferedReader(new StringReader(expressions));
        return Lexer.scan(br, errorReporter);
    }

    private static void assertTypes(List<TokenType> types, TokenType... expected) {
        MatcherAssert.assertThat(types, is(List.of(expected)));
    }

    @Test
    public void empty() {
        assertTypes(scan(""), TokenType.EOF);
    }

    @Test
    public void correctBencodeData(){
        assertTypes(scan("d3:cow3:moo3:keye"),
                TokenType.DICTIONARY,
                TokenType.STRING_BEGIN,  TokenType.SEPARATOR, TokenType.STRING,
                TokenType.STRING_BEGIN,  TokenType.SEPARATOR, TokenType.STRING,
                TokenType.STRING_BEGIN,  TokenType.SEPARATOR, TokenType.STRING,
                TokenType.END_TYPE, TokenType.EOF
        );
    }

    @Test
    public void string(){
        assertTypes(scan("5:12d$@"),
                TokenType.STRING_BEGIN, TokenType.SEPARATOR, TokenType.STRING, TokenType.EOF);
    }

    @Test
    public void number(){
        assertTypes(scan("i432e"),
                TokenType.INTEGER_BEGIN, TokenType.INTEGER, TokenType.END_TYPE, TokenType.EOF);
    }

    @Test
    public void negativeNumber(){
        List<Token> tokens = scant("i-213e");
        assertTypes(tokens.stream().map(Token::tokenType).toList(),
                TokenType.INTEGER_BEGIN, TokenType.INTEGER, TokenType.END_TYPE, TokenType.EOF);
        Assert.assertEquals(-213, tokens.get(1).value());
    }

    @Test
    public void zeroLength(){
        String s = "0:";
        assertTypes(scan(s), TokenType.STRING_BEGIN, TokenType.SEPARATOR, TokenType.EOF);
    }

    @Test
    public void incorrectSymbolInBencodeData(){
        Assert.assertNull(scan("d3@:cow3:moo3:keye"));
    }

    @Test
    public void lineOfSpaces() {
        assertTypes(scan("        "), TokenType.EOF);
    }

    @Test
    public void unknownChar() {
        Assert.assertNull(scan("$"));
    }

    @Test
    public void emojiSymbol() {
        Assert.assertNull(scan("l2:\uD83E\uDDE02:\uD83E\uDD21e"));
    }

    @Test
    public void negativeLength(){
        Assert.assertNull(scan("-2:input"));
    }

    @Test
    public void stringWithoutLength(){
        Assert.assertNull(scan(":12de$@"));
    }

    @Test
    public void stringWithoutSeparator(){
        Assert.assertNull(scan("5rre$@"));
    }

    @Test
    public void stringLengthLessThanDeclared(){
        Assert.assertNull(scan("5:1e$@"));
    }

    @Test
    public void stringLengthMoreThanDeclared(){
        Assert.assertNull(scan("5:12de$@"));
    }

    @Test
    public void numberMoreThanMaxInteger(){
        Assert.assertNull(scan("i234324343243424e"));
    }

    @Test
    public void numberStartingFromZeros(){
        String digit = "i00323e";
        Assert.assertNull(scan(digit));
    }

    @Test
    public void negativeZero(){
        String negativeZero = "i-0e";
        Assert.assertNull(scan(negativeZero));
    }
}

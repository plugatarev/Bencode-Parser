package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.error.ErrorReporter;
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
    private static List<TokenType> scan(String expressions) {
        BufferedReader br = new BufferedReader(new StringReader(expressions));
        ErrorReporter errorReporter = new ErrorReporter() {
            int i = 0;
            @Override
            public boolean report(String message) {
                i++;
                if (i < 20) return true;
                return false;
            }
        };
        List<Token> tokens = Lexer.scan(br, errorReporter);
        return tokens == null ? null : tokens.stream().map(Token::tokenType).toList();
    }

    private static void assertTypes(List<TokenType> types, TokenType... expected) {
        MatcherAssert.assertThat(types, is(List.of(expected)));
    }

    @Test
    public void empty() {
        assertTypes(scan(""), TokenType.EOF);
    }

    @Test
    public void unknownChar() {
        Assert.assertNull(scan("$"));
    }

    @Test
    public void correctBencodeData(){
        assertTypes(scan("d3:cow3:moo3:keye"),
                TokenType.DICTIONARY,
                TokenType.STRING_BEGIN,  TokenType.SEPARATOR, TokenType.STRING,
                TokenType.STRING_BEGIN,  TokenType.SEPARATOR, TokenType.STRING,
                TokenType.STRING_BEGIN,  TokenType.SEPARATOR, TokenType.STRING,
                TokenType.END_TYPE, TokenType.EOL, TokenType.EOF
                );
    }

    @Test
    public void emojiSymbol() {
        Assert.assertNull(scan("l2:\uD83E\uDDE02:\uD83E\uDD21e"));
    }

    @Test
    public void string(){
        assertTypes(scan("5:12d$@"),
                TokenType.STRING_BEGIN, TokenType.SEPARATOR, TokenType.STRING, TokenType.EOL, TokenType.EOF);
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
    public void number(){
        assertTypes(scan("i432e"),
                TokenType.INTEGER_BEGIN, TokenType.INTEGER, TokenType.END_TYPE, TokenType.EOL, TokenType.EOF);
    }

    @Test
    public void numberMoreThanMaxInteger(){
        Assert.assertNull(scan("i234324343243424e"));
    }

    @Test
    public void incorrectSymbolInBencodeData(){
        Assert.assertNull(scan("d3@:cow3:moo3:keye"));
    }

    @Test
    public void eol() {
        assertTypes(scan("\n\n"), TokenType.EOL, TokenType.EOL, TokenType.EOF);
    }

    @Test
    public void strangeDigits() {
        // some strange digits from Character.isDigit() javadoc
        String digits = "\u0660\u06F0\u0966\uFF10";
        for (int i = 0; i < digits.length(); i++) {
            Assert.assertNull(scan(String.valueOf(digits.charAt(i))));
        }
    }
}

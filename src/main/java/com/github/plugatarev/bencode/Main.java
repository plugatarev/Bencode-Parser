package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.lexer.Lexer;
import com.github.plugatarev.bencode.lexer.Token;
import com.github.plugatarev.bencode.error.ConsoleReporter;
import com.github.plugatarev.bencode.error.ErrorReporter;
import com.github.plugatarev.bencode.parser.Element;
import com.github.plugatarev.bencode.parser.Parser;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String expressions = "d3:cow3:moo3:keyd2:bb3:hhh2:ffd4:keysl1:i1:d1:l1:eeee4:spaml4:infoi32eee";
        Reader reader = new StringReader(expressions);
        BufferedReader br = new BufferedReader(reader);
        ErrorReporter reporter = new ConsoleReporter();

        List<Token> tokens = Lexer.scan(br, reporter);
        Element jsonTokens = Parser.parse(tokens, new ConsoleReporter());
        JsonConverter converter = new JsonConverter();
        System.out.println(converter.json(jsonTokens));
    }
}

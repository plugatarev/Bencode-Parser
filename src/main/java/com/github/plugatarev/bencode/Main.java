package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.Lexer.Lexer;
import com.github.plugatarev.bencode.Lexer.Token;
import com.github.plugatarev.bencode.Error.ConsoleReporter;
import com.github.plugatarev.bencode.Error.ErrorReporter;
import com.github.plugatarev.bencode.Parser.Element;
import com.github.plugatarev.bencode.Parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
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
        List<Element> jsonTokens = Parser.parse(tokens, new ConsoleReporter());
        System.out.println(jsonTokens);
    }
}

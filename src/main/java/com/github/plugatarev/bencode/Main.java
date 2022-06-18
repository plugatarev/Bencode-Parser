package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.error.FileReporter;
import com.github.plugatarev.bencode.lexer.Lexer;
import com.github.plugatarev.bencode.lexer.Token;
import com.github.plugatarev.bencode.parser.Element;
import com.github.plugatarev.bencode.parser.Parser;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2){
            System.err.println("I/O files specified incorrectly or not specified\n");
            return;
        }
        Path input = Paths.get(args[0]).toAbsolutePath();
        Path output = Paths.get(args[1]).toAbsolutePath();
        try (BufferedReader br = new BufferedReader(new FileReader(input.toFile()));
             BufferedWriter bw = new BufferedWriter(new FileWriter(output.toFile()));
             FileReporter reporter = new FileReporter(bw)){
                List<Token> tokens = Lexer.scan(br, reporter);
                if (tokens == null){
                    return;
                }
                Element bTokens = Parser.parse(tokens, reporter);
                if (bTokens == null){
                    return;
                }
            JsonConverter converter = new JsonConverter();
            bw.write(converter.json(bTokens));
        }
    }
}

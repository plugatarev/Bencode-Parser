package com.github.plugatarev.bencode.Lexer;

public enum TokenType {
    LIST,
    DICTIONARY,
    STRING,
    LENGTH,
    INTEGER_BEGIN,
    INTEGER,
    END_TYPE,
    SEPARATOR,
    EOL,
    EOF
}

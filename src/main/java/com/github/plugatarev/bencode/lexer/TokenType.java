package com.github.plugatarev.bencode.lexer;

public enum TokenType {
    LIST,
    DICTIONARY,
    STRING,
    STRING_BEGIN,
    INTEGER_BEGIN,
    INTEGER,
    END_TYPE,
    SEPARATOR,
    EOF
}

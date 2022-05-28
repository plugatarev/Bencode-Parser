package com.github.plugatarev.bencode.lexer;

public enum ErrorType {
    UNKNOWN_CHAR,
    INCORRECT_NUMBER,
    INCORRECT_STRING_LENGTH,
    NUMBER_WITH_DEAD_ZEROS
}

package com.github.plugatarev.bencode.lexer;

public enum LexerError {
    UNKNOWN_CHAR ("Unknown char '%c' at line %d:\n%s\n%s^--- here\n"),
    INCORRECT_NUMBER("Incorrect number %s at line %d:\n%s\n%s^--- here\n"),
    INCORRECT_STRING_LENGTH("Expected string of length %d at line %d,\n%s\n%s^--- here\n"),
    NUMBER_WITH_DEAD_ZEROS("Number %s cannot have leading zeros at line %d:\n%s\n%s^--- here\n");

    private final String errorMessage;

    LexerError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage(String line, int pos, Object obj) {
        return errorMessage.formatted(obj, 1, pos, line, " ".repeat(pos));
    }
}

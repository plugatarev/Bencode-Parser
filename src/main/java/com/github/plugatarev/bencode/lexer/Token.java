package com.github.plugatarev.bencode.lexer;

public record Token(TokenType tokenType, int nLine, int pos, Object value) {}

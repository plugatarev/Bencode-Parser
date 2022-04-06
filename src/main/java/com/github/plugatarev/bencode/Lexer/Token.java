package com.github.plugatarev.bencode.Lexer;

import java.util.Objects;

public final class Token {
    private final TokenType tokenType;
    private final int nLine;
    private final int pos;
    private final Object value;

    public Token(TokenType tokenType, int nLine, int pos, Object value) {
        this.tokenType = tokenType;
        this.nLine = nLine;
        this.pos = pos;
        this.value = value;
    }

    public TokenType tokenType() {
        return tokenType;
    }

    public int nLine() {
        return nLine;
    }

    public int pos() {
        return pos;
    }

    public Object value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Token) obj;
        return Objects.equals(this.tokenType, that.tokenType) &&
                this.nLine == that.nLine &&
                this.pos == that.pos &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenType, nLine, pos, value);
    }

    @Override
    public String toString() {
        return "Token[" +
                "tokenType=" + tokenType + ", " +
                "nLine=" + nLine + ", " +
                "pos=" + pos + ", " +
                "value=" + value + ']';
    }
}

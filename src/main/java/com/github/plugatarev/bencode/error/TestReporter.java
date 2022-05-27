package com.github.plugatarev.bencode.error;

public class TestReporter implements ErrorReporter{
    int i = 0;
    @Override
    public boolean report(String message) {
        i++;
        return i < 20;
    }
}

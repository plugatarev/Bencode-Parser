package com.github.plugatarev.bencode.error;

public class TestReporter implements ErrorReporter{
    private int nMessages = 0;

    @Override
    public boolean report(String message) {
        nMessages++;
        return nMessages < 20;
    }

    @Override
    public boolean hasError() {
        return nMessages > 0;
    }

    public void clear(){
        nMessages = 0;
    }
}

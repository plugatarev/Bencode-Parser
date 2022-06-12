package com.github.plugatarev.bencode.error;

public class ConsoleReporter implements ErrorReporter {
    private static final int MAX_MESSAGES = 20;
    private int nMessages;

    @Override
    public boolean report(String message) {
        nMessages++;
        System.err.println(message);
        if (nMessages >= MAX_MESSAGES) {
            System.err.println("Too many errors, stopped...");
            return false;
        }
        return true;
    }

    @Override
    public boolean hasError(){
        return nMessages > 0;
    }
}
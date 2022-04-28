package com.github.plugatarev.bencode.error;

import java.io.BufferedWriter;
import java.io.IOException;

public class FileReporter implements ErrorReporter {
    private static final int MAX_MESSAGES = 20;
    private int nMessages;
    private final BufferedWriter bw;
    public FileReporter(BufferedWriter bw){
        this.bw = bw;
    }

    @Override
    public boolean report(String message){
        nMessages++;
        try {
            bw.write(message);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        if (nMessages >= MAX_MESSAGES) {
            System.err.println("Too many errors, stopped...");
            return false;
        }
        return true;
    }
}

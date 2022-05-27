package com.github.plugatarev.bencode.error;

import java.io.BufferedWriter;
import java.io.IOException;

public class FileReporter implements ErrorReporter, AutoCloseable {
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

    @Override
    public void close() throws Exception {
        //OK CR: close file here, use try catch with resources in main
        bw.close();
    }
}

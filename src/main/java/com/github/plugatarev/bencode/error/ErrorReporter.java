package com.github.plugatarev.bencode.error;
public interface ErrorReporter {
    /**
     * @param message error message
     * @return false if it no more errors can be reported
     */
    boolean report(String message);

    boolean hasError();
}

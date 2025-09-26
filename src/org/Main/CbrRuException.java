package org.Main;

public class CbrRuException extends Exception {
    public CbrRuException(String text) {
        super(text);
    }

    public CbrRuException(String text, Exception e) {
        super(text, e);
    }
}

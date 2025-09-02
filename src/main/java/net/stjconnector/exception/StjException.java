package net.stjconnector.exception;

public class StjException extends Exception {
    
    public StjException(String message) {
        super(message);
    }
    
    public StjException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public StjException(Throwable cause) {
        super(cause);
    }
}
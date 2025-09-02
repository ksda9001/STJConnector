package net.stjconnector.exception;

public class DataTransferException extends StjException {
    
    public DataTransferException(String message) {
        super(message);
    }
    
    public DataTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
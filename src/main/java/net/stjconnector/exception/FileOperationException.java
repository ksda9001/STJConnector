package net.stjconnector.exception;

public class FileOperationException extends StjException {
    
    public FileOperationException(String message) {
        super(message);
    }
    
    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
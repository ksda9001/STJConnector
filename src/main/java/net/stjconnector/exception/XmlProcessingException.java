package net.stjconnector.exception;

public class XmlProcessingException extends StjException {
    
    public XmlProcessingException(String message) {
        super(message);
    }
    
    public XmlProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
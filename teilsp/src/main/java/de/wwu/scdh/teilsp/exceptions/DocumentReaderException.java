package de.wwu.scdh.teilsp.exceptions;

public class DocumentReaderException extends Exception {
    public DocumentReaderException(String msg) {
	super(msg);
    }
    public DocumentReaderException(Throwable cause) {
	super(cause);
    }
}

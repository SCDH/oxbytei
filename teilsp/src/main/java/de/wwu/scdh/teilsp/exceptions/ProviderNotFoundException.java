package de.wwu.scdh.teilsp.exceptions;

public class ProviderNotFoundException extends Exception {
    public ProviderNotFoundException(String msg) {
	super(msg);
    }
    public ProviderNotFoundException(Throwable cause) {
	super(cause);
    }
}

package de.wwu.scdh.teilsp.services.extensions;

public class ExtensionException extends Exception {
    public ExtensionException(String msg) {
	super(msg);
    }
    public ExtensionException(Throwable cause) {
	super(cause);
    }
}

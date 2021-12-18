package de.wwu.scdh.teilsp.exceptions;

public class ConfigurationException extends Exception {
    public ConfigurationException(String msg) {
	super(msg);
    }
    public ConfigurationException(Throwable cause) {
	super(cause);
    }
}

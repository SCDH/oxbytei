package de.wwu.scdh.oxbytei.commons;

public class RollbackException extends Exception {
    public RollbackException(String msg) {
	super(msg);
    }
    public RollbackException(Throwable cause) {
	super(cause);
    }
}

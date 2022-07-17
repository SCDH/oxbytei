package de.wwu.scdh.teilsp.services.extensions;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;

/**
 * A descriptor for Boolean arguments.
 */
public class BooleanArgumentDescriptor extends ArgumentDescriptorImpl<boolean> {

    public BooleanArgumentDescriptor(String name, String description) {
	super(boolean.class, name, description);
    }

    public BooleanArgumentDescriptor(String name, String description, boolean defaultValue) {
	super(boolean.class, name, description, defaultValue);
    }

    @Override
    public boolean fromString(String value)
	throws ConfigurationException {
	return Boolean.parseBoolean(value);
    }
    
}

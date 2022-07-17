package de.wwu.scdh.teilsp.services.extensions;

import java.net.URL;
import java.net.MalformedURLException;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;

/**
 * A descriptor for URL arguments.
 */
public class URLArgumentDescriptor extends ArgumentDescriptorImpl<URL> {

    public URLArgumentDescriptor(String name, String description) {
	super(URL.class, name, description);
    }

    public URLArgumentDescriptor(String name, String description, URL defaultValue) {
	super(URL.class, name, description, defaultValue);
    }

    public URLArgumentDescriptor(String name, String description, URL[] allowedValues, URL defaultValue) {
	super(URL.class, name, description, allowedValues, defaultValue);
    }

    @Override
    public URL fromString(String value)
	throws ConfigurationException {
	URL url;
	try {
	    url = new URL(value);
	    return url;
	} catch (MalformedURLException e) {
	    throw new ConfigurationException("Not a valid URL given for '" + name + "': " + value);
	}
    }
    
}

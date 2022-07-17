/**
 * This needs an implementation.
 *
 */

package de.wwu.scdh.teilsp.services.extensions;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.xml.NamespaceContextImpl;


public class ArgumentDescriptor {

    public static final int TYPE_JAVA_OBJECT = 0;

    public static final int TYPE_XPATH_EXPRESSION = 1;

    public static final int TYPE_SCRIPT = 2;

    public static final int TYPE_STRING = 3;

    public static final int TYPE_NAMESPACE_DECL = 4;

    public static final int TYPE_BOOLEAN = 5;

    public static final int TYPE_URL = 6;

    protected final String name;
    protected final int type;
    protected final String description;
    protected final String defaultValue;  // TODO/FIXME: this should be of type Object

    public ArgumentDescriptor(String name, int type, String description) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.defaultValue = null;
    }

    public ArgumentDescriptor(String name, int type, String description, String defaultValue) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.defaultValue = defaultValue;
    }

    public ArgumentDescriptor(String name, int type, String description, String[] allowedValues, String defaultValue) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.defaultValue = defaultValue;
    }

    public boolean validate(String value)
	throws ConfigurationException {
	if (type == TYPE_JAVA_OBJECT) {
	    // TODO
	    return true;
	} else if (type == TYPE_XPATH_EXPRESSION) {
	    // TODO
	    return true;
	} else if (type == TYPE_STRING) {
	    return true;
	} else if (type == TYPE_SCRIPT) {
	    // TODO: what does this type mean? is it an URL or a file
	    // path or a kind of inline script?
	    return true;
	} else if (type == TYPE_NAMESPACE_DECL) {
	    // We use NamespaceContextImpl to instantiate an catch exceptions
	    try {
		NamespaceContextImpl ns = new NamespaceContextImpl(value);
		return true;
	    } catch (Exception e) {
		throw new ConfigurationException(e);
	    }
	} else if (type == TYPE_BOOLEAN) {
	    return true;
	} else if (type == TYPE_URL) {
	    try {
		URL url = new URL(value);
		// We do not check for existance. If URL does not
		// exist, e.g. a plugin should raise
		// ExtensionException.
		return true;
	    } catch (MalformedURLException e) {
		throw new ConfigurationException(e);
	    }
	}
	return false;
    }

    public boolean validate(Map<String, String> arguments)
	throws ConfigurationException {
	String value;
	// check if attribute is present or default value exists
	if (this.defaultValue == null) {
	    if (arguments.containsKey(name)) {
		value = arguments.get(name);
	    } else {
		throw new  ConfigurationException("Missing argument '" + this.name + "'");
	    }
	} else {
	    if (arguments.containsKey(name)) {
		value = arguments.get(name);
	    } else {
		// We assume, that the default value is valid!
		return true;
	    }
	}
	return validate(value);
    }

    /**
     * A static method that validates the given arguments map with an
     * array of {@link ArgumentDescriptor}s.
     *
     * @param argumentDescriptors  array of {@link ArgumentDescriptor}s
     * @param arguments            arguments map
     * @return                     this returns a boolean
     */
    public static boolean validateAll(ArgumentDescriptor[] argumentDescriptors, Map<String, String> arguments)
	throws ConfigurationException {
	boolean result = true;
	for (ArgumentDescriptor argumentDescriptor : argumentDescriptors) {
	    result = result && argumentDescriptor.validate(arguments);
	}
	return result;
    }

    /**
     * Validate that we have a Java object and get it from arguments or
     * default value. A {@link ConfigurationException} is thrown, if
     * the value is missing.
     *
     * @param arguments   the arguments map
     */
    public String getJavaObject(Map<String, String> arguments)
	throws ConfigurationException {
	if (!arguments.containsKey(name)) {
	    if (defaultValue == null) {
		throw new ConfigurationException("Missing argument '" + this.name + "'");
	    } else {
		return defaultValue;
	    }
	} else {
	    return arguments.get(name);
	}
    }

    /**
     * Validate that we have a string and it get from arguments or
     * default value. A {@link ConfigurationException} is thrown, if
     * the value is missing.
     *
     * @param arguments   the arguments map
     */
    public String getString(Map<String, String> arguments)
	throws ConfigurationException {
	if (!arguments.containsKey(name)) {
	    if (defaultValue == null) {
		throw new ConfigurationException("Missing argument '" + this.name + "'");
	    } else {
		return defaultValue;
	    }
	} else {
	    return arguments.get(name);
	}
    }

    private boolean getBoolean(String value) {
	return Boolean.parseBoolean(value);
    }

    /**
     * Validate if we have an XPath expression and get from arguments
     * or default value. A {@link ConfigurationException} is thrown,
     * if the value is missing.
     *
     * @param arguments   the arguments map
     */
    // TODO: should we return s9api.XPathExcecutable ?
    public String getXPathExpression(Map<String, String> arguments)
	throws ConfigurationException {
	return getString(arguments);
    }

    /**
     * Validate if we have a boolean and get from arguments or default
     * value. A {@link ConfigurationException} is thrown, if the value
     * is missing.
     *
     * @param arguments   the arguments map
     */
    public boolean getBoolean(Map<String, String> arguments)
	throws ConfigurationException {
	if (!arguments.containsKey(name)) {
	    if (defaultValue == null) {
		throw new ConfigurationException("Missing argument '" + this.name + "'");
	    } else {
		return getBoolean(defaultValue);
	    }
	} else {
	    return getBoolean(arguments.get(name));
	}
    }

    // TODO: others
}

package de.wwu.scdh.teilsp.services.extensions;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;

/**
 * {@link ArgumentDescriptorImpl} is an implementation of a
 * polymorphic {@link ArgumentDescriptor}. Usually it will be required
 * to override {@link ArgumentDescriptorImpl#fromString(String)}.
 */
public class ArgumentDescriptorImpl<T> implements ArgumentDescriptor<T> {

    protected final String name;
    protected final Class<T> type;
    protected final String description;
    protected final boolean required;
    protected final T defaultValue;
    protected final T[] allowedValues;

    public ArgumentDescriptorImpl(Class<T> type, String name, String description) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.required = true;
	this.defaultValue = null;
	this.allowedValues = null;
    }

    public ArgumentDescriptorImpl(Class<T> type, String name, String description, T defaultValue) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.required = false;
	this.defaultValue = defaultValue;
	this.allowedValues = null;
    }

    public ArgumentDescriptorImpl(Class<T> type, String name, String description, T[] allowedValues, T defaultValue) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.required = false;
	this.defaultValue = defaultValue;
	this.allowedValues = allowedValues;
    }

    public String getName() {
	return name;
    }

    public String getDescription() {
	return description;
    }

    public String getType() {
	return type.toString();
    }

    public T getDefaultValue() {
	return defaultValue;
    }

    public T[] getAllowedValues() {
	return allowedValues;
    }

    public boolean isRequired() {
	return required;
    }

    public T fromString(String value)
	throws ConfigurationException {
	try {
	    T instance = type.getDeclaredConstructor(String.class).newInstance(value);
	    return instance;
	} catch (InstantiationException e) {
	    throw new ConfigurationException("Failed to create " + type.toString() + " from " + value
					     + "\n\n" + e);
	} catch (IllegalAccessException e) {
	    throw new ConfigurationException("Cannot access constructor " + type.toString() + "(String)"
					     + "\n\n" + e);
	} catch (NoSuchMethodException e) {
	    throw new ConfigurationException("Cannot cast from String to " + type.toString()
					     + "\n\n" + e);
	} catch (InvocationTargetException e) {
	    throw new ConfigurationException(e);
	}
    }

    public boolean isAllowed(T value) {
	if (allowedValues == null) {
	    return true;
	} else if (allowedValues.length == 0) {
	    return true;
	} else {
	    boolean allowed = false;
	    for (T allowedValue : allowedValues) {
		allowed = allowed || allowedValue.equals(value);
	    }
	    return allowed;
	}
    }

    public T getValue(Map<String, String> arguments)
	throws ConfigurationException {
	T value;
	if (!arguments.containsKey(name)) {
	    if (required) {
		throw new ConfigurationException("Required argument '" + name + "' is missing");
	    } else {
		value = defaultValue;
	    }
	} else {
	    value = fromString(arguments.get(name));
	}
	if (isAllowed(value)) {
	    return value;
	} else {
	    throw new ConfigurationException("Value '" + value + "' not allowd for argument " + name);
	}
    }

    public boolean validate(String value) {
	try {
	    T instance = fromString(value);
	    return true;
	} catch (ConfigurationException e) {
	    return false;
	}
    }

    public boolean validate(Map<String, String> arguments) {
	try {
	    T instance = getValue(arguments);
	    return true;
	} catch (ConfigurationException e) {
	    return false;
	}
    }

}

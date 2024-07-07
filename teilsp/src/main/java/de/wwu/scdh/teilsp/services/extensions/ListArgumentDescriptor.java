package de.wwu.scdh.teilsp.services.extensions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;

/**
 * A descriptor for {@link List} arguments.
 */
public class ListArgumentDescriptor<T> implements ArgumentDescriptor<List<T>> {

    protected final String name;
    protected final Class<T> type;
    protected final String description;
    protected final boolean required;
    protected final List<T> defaultValue;
    protected final List<T>[] allowedValues;

    protected final String valueSeparator;
    protected final int minLength;

    public ListArgumentDescriptor(Class<T> type, String name, String description) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.required = true;
	this.defaultValue = null;
	this.allowedValues = null;
	this.minLength = 0;
	this.valueSeparator = ",\\s+";
    }

    public ListArgumentDescriptor(Class<T> type, String name, String description, String valueSeparator, int minLength) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.required = true;
	this.defaultValue = null;
	this.allowedValues = null;
	this.minLength = minLength;
	this.valueSeparator = valueSeparator;
    }

    public ListArgumentDescriptor(Class<T> type, String name, String description, String valueSeparator, int minLength, List<T> defaultValue) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.required = false;
	this.defaultValue = defaultValue;
	this.allowedValues = null;
	this.minLength = minLength;
	this.valueSeparator = valueSeparator;
    }

    public ListArgumentDescriptor(Class<T> type, String name, String description, String valueSeparator, int minLength, List<T>[] allowedValues, List<T> defaultValue) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.required = false;
	this.defaultValue = defaultValue;
	this.allowedValues = allowedValues;
	this.minLength = minLength;
	this.valueSeparator = valueSeparator;
    }

    public ListArgumentDescriptor(Class<T> type, String name, String description, String valueSeparator, int minLength, List<T>[] allowedValues) {
	this.name = name;
	this.type = type;
	this.description = description;
	this.required = false;
	this.defaultValue = null;
	this.allowedValues = allowedValues;
	this.minLength = minLength;
	this.valueSeparator = valueSeparator;
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

    public List<T> getDefaultValue() {
	return defaultValue;
    }

    public List<T>[] getAllowedValues() {
	return allowedValues;
    }

    public boolean isRequired() {
	return required;
    }

    public List<T> getValue(Map<String, String> arguments)
	throws ConfigurationException {
	List<T> value;
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

    public boolean isAllowed(List<T> value) {
	if (allowedValues == null) {
	    return true;
	} else if (allowedValues.length == 0) {
	    return true;
	} else {
	    boolean allowed = false;
	    for (List<T> allowedValue : allowedValues) {
		allowed = allowed || allowedValue.equals(value);
	    }
	    return allowed;
	}
    }

    @Override
    public List<T> fromString(String value)
	throws ConfigurationException {
	List<T> urls = new ArrayList<T>();
	for (String v: value.split(valueSeparator)) {
	    try {
		T instance = type.getDeclaredConstructor(String.class).newInstance(v);
		urls.add(instance);
	    } catch (Exception e) {
		throw new ConfigurationException(e.getMessage());
	    }
	}
	return urls;
    }

    public boolean validate(String value) {
	try {
	    List<T> instance = fromString(value);
	    return instance.size() >= this.minLength;
	} catch (ConfigurationException e) {
	    return false;
	}
    }

    public boolean validate(Map<String, String> arguments) {
	try {
	    List<T> instance = getValue(arguments);
	    return instance.size() >= this.minLength;
	} catch (ConfigurationException e) {
	    return false;
	}
    }


}

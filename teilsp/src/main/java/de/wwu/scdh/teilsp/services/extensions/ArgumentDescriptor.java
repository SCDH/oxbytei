package de.wwu.scdh.teilsp.services.extensions;

import java.util.Map;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;


public interface ArgumentDescriptor<T> {

    /**
     * A method for casting the configuration value to the Java
     * object. This may have to be overriden in anonyous classes.
     *
     * @param value   the configuration value as String
     * @return this returns a Java object
     */
    public T fromString(String value) throws ConfigurationException;

    /**
     * Get the name of the argument.
     */
    public String getName();

    /**
     * Get a (human readable) description of the argument.
     */
    public String getDescription();

    /**
     * Get a description of the type. This should be something like
     * {@link Class#toString()}.
     */
    public String getType();

    /**
     * Get the default value if any.
     */
    public T getDefaultValue();

    /**
     * Get an array of allowed values or <code>null</code> if there
     * are no bounds.
     */
    public T[] getAllowedValues();

    /**
     * Whether the argument is required in the configuration file or
     * not.
     */
    public boolean isRequired();

    /**
     * Get the value of for the argument from an arguments map,
     * validate it and return the Java object. This should return a
     * default value or throw a {@link ConfigurationException} if the
     * argument is not present in the map.
     *
     * @param arguments  the map of arguments (parsed from the configuration)
     * @return the Java object
     */
    public T getValue(Map<String, String> arguments) throws ConfigurationException;

    /**
     * Validate a string value against this argument descriptor.
     */
    public boolean validate(String value);

    /**
     * Get the argument defined by this descriptor from an arguments
     * map and validate it. Missing required arguments are invalid.
     */
    public boolean validate(Map<String, String> arguments);

    /**
     * A static method that validates the given arguments map with an
     * array of {@link ArgumentDescriptor}s.
     *
     * @param argumentDescriptors  array of {@link ArgumentDescriptor}s
     * @param arguments            arguments map
     * @return                     this returns a boolean
     */
    public static boolean validateAll(ArgumentDescriptor<?>[] argumentDescriptors, Map<String, String> arguments)
	throws ConfigurationException {
	boolean result = true;
	for (ArgumentDescriptor<?> argumentDescriptor : argumentDescriptors) {
	    result = result && argumentDescriptor.validate(arguments);
	}
	return result;
    }

}

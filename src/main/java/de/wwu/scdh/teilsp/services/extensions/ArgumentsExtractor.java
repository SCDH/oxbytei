package de.wwu.scdh.teilsp.services.extensions;

import java.util.HashMap;
import java.util.Map;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;

/**
 * A argument descriptor that requires the argument value to be of the
 * form <code>key1=value1,key2=value2</code>. It returns a map of key
 * value pairs on {@link getValue()}.
 *
 * This class offers also a static method to parse such key value
 * pairs.
 */
public class ArgumentsExtractor<M extends Map<String, String>> extends ArgumentDescriptorImpl<M>  {

    public ArgumentsExtractor(String name, String description) {
    	super((Class<M>) new HashMap<String, String>().getClass(), name, description);
    }

    public ArgumentsExtractor(String name, String description, M defaultValue) {
    	super((Class<M>) new HashMap<String, String>().getClass(), name, description, defaultValue);
    }

    @Override
    public M fromString(String value)
	throws ConfigurationException{
	return (M) ArgumentsExtractor.arguments(value);
    }

    /**
     * Make a map or key value pairs from arguments encoded in a
     * string, where arguments are separated by space and keys and
     * values are separtated by =. If a value contains a space, it
     * must be quoted.
     * @param arguments string encoding the arguments
     * @return a map
     */
    public static Map<String, String> arguments(String argumentsString)
	    throws ConfigurationException {
	HashMap<String, String> args = new HashMap<String, String>();

	// return empty map for empty string and null
	if (argumentsString == "" || argumentsString == null) {
	    return args;
	}

	String[] pairs = argumentsString.trim().split("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	int i;
	String[] pair = new String[2];
	int l;
	for (i = 0; i < pairs.length; i++) {
	    pair = pairs[i].split("=", 2);
	    if (pair.length != 2) {
		throw new ConfigurationException("Bad arguments: " + pairs[i]);
	    }
	    if (pair[0].length() == 0) {
		throw new ConfigurationException("Bad arguments: argument name may not be the empty string");
	    }
	    if (pair[1].startsWith("\"") && pair[1].endsWith("\"")) {
		l = pair[1].length() - 1;
		pair[1] = pair[1].substring(1, l);
	    }
	    args.put(pair[0], pair[1]);
	}

	return args;
    }

}

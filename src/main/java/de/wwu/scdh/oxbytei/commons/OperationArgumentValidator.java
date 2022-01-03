/**
 * OperationArgumentValidator is a class for validating arguments and
 * their values of author operation.
 *
 */

package de.wwu.scdh.oxbytei.commons;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class OperationArgumentValidator {

    /**
     * Validate argument and return the input value as a string
     * @param name
     * @param args
     * @return String value of argument
     * @throws AuthorOperationException
     */
    public static String validateStringArgument(String name, ArgumentsMap args)
	throws AuthorOperationException {

	Object val = args.getArgumentValue(name);

	if (val == null || ! (val instanceof String)) {
		throw new IllegalArgumentException("parameter not declared or invalid value: "
						   + name + ", " + val);
	};

	return (String) val;
    }

    /**
     * Validate argument and return the input value as a string
     * @param name
     * @param args
     * @param def default value
     * @return String value of argument
     */
    public static String validateStringArgument(String name, ArgumentsMap args, String def)
	throws AuthorOperationException {

	Object val = args.getArgumentValue(name);

	if (val == null) {
	    return def;
	}

	if (val instanceof String) {
		throw new IllegalArgumentException("invalid parameter type: "
						   + name + ", " + val);
	}

	return (String) val;
    }

    
}

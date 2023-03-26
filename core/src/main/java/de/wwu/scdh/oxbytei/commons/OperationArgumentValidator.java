/**
 * OperationArgumentValidator is a class for validating arguments and
 * their values of author operation.
 *
 */

package de.wwu.scdh.oxbytei.commons;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;

public class OperationArgumentValidator {

    /**
     * Validate argument and return the input value as a string
     * @param name
     * @param args
     * @return String value of argument
     * @throws IllegalArgumentException
     */
    public static String validateStringArgument(String name, ArgumentsMap args)
	throws IllegalArgumentException {

	Object val = args.getArgumentValue(name);

	if (val == null || ! (val instanceof String)) {
		throw new IllegalArgumentException("parameter not declared or invalid value: "
						   + name + ", " + val);
	};

	return (String) val;
    }

    /**
     * Validate argument and return the input value as a string with editor variables expanded.
     * @param name
     * @param args
     * @param authorAccess access to author mode utilities
     * @param expandAsk whether the ask editor variable should be expanded
     * @return String value of argument
     * @throws IllegalArgumentException
     */
    public static String validateStringArgument
	(String name, ArgumentsMap args,
	 AuthorAccess authorAccess, boolean expandAsk)
	throws IllegalArgumentException {

	Object val = args.getArgumentValue(name);

	if (val == null || ! (val instanceof String)) {
		throw new IllegalArgumentException("parameter not declared or invalid value: "
						   + name + ", " + val);
	};

	// expand as long as there are still unexpanded editor variables
	String unexpanded = (String) val;
	String expanded =
	    authorAccess.getUtilAccess().expandEditorVariables(unexpanded,
							       authorAccess.getEditorAccess().getEditorLocation(),
							       expandAsk);
	while (! expanded.equals(unexpanded)) {
	    unexpanded = expanded;
	    expanded = authorAccess.getUtilAccess().expandEditorVariables(unexpanded,
									  authorAccess.getEditorAccess().getEditorLocation(),
									  expandAsk);
	}
	return expanded;
    }

    /**
     * Validate argument and return the input value as a string
     * @param name
     * @param args
     * @param def default value
     * @return String value of argument
     */
    public static String validateStringArgument(String name, ArgumentsMap args, String def) {

	Object val = args.getArgumentValue(name);

	if (val == null) {
	    return def;
	}

	if (! (val instanceof String)) {
		throw new IllegalArgumentException("invalid parameter type: "
						   + name + ", " + val);
	}

	return (String) val;
    }

}

/**
 * OperationArgumentValidator is a class for validating arguments and
 * their values of author operation.
 *
 */

package de.wwu.scdh.oxbytei.commons;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.AuthorAccess;

public class OperationArgumentValidator {

    /**
     * Validate argument and return the input value as a string
     * @param name
     * @param args
     * @return String value of argument
     * @throws AuthorOperationException
     */
    public static String validateStringArgument(String name, ArgumentsMap args)
	throws AuthorOperationException, IllegalArgumentException {

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
     * @throws AuthorOperationException
     */
    public static String validateStringArgument
	(String name, ArgumentsMap args,
	 AuthorAccess authorAccess, boolean expandAsk)
	throws AuthorOperationException, IllegalArgumentException {

	Object val = args.getArgumentValue(name);

	if (val == null || ! (val instanceof String)) {
		throw new IllegalArgumentException("parameter not declared or invalid value: "
						   + name + ", " + val);
	};

	return authorAccess.getUtilAccess().expandEditorVariables((String) val,
								  authorAccess.getEditorAccess().getEditorLocation(),
								  expandAsk);
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

	if (! (val instanceof String)) {
		throw new IllegalArgumentException("invalid parameter type: "
						   + name + ", " + val);
	}

	return (String) val;
    }

}

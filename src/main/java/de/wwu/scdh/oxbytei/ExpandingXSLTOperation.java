package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.commons.operations.XSLTOperation;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import de.wwu.scdh.oxbytei.commons.UpdatableArgumentsMap;

/**
 * Same os {@link XSLTOperation}, but expands editor variables in
 * arguments.
 */
public class ExpandingXSLTOperation extends XSLTOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
	throws AuthorOperationException {

	// expand editor variables
	UpdatableArgumentsMap newArgs = new UpdatableArgumentsMap(args, getArguments());
	newArgs.expand("sourceLocation", authorAccess, true);
	newArgs.expand("targetLocation", authorAccess, true);
	newArgs.expand("action", authorAccess, true);
	newArgs.expand("externalParams", authorAccess, true);

	super.doOperation(authorAccess, newArgs);
    }

}

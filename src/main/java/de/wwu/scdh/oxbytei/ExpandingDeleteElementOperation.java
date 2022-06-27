package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.commons.operations.DeleteElementOperation;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import de.wwu.scdh.oxbytei.commons.UpdatableArgumentsMap;

/**
 * Same as {@link DeleteElementOperation}, but expands editor variables in
 * arguments.
 */
public class ExpandingDeleteElementOperation extends DeleteElementOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
	throws AuthorOperationException {

	// expand editor variables
	UpdatableArgumentsMap newArgs = new UpdatableArgumentsMap(args, getArguments());
	newArgs.expand("elementLocation", authorAccess, true);

	super.doOperation(authorAccess, newArgs);
    }

}

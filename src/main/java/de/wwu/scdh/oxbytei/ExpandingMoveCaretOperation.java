package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.commons.operations.MoveCaretOperation;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import de.wwu.scdh.oxbytei.commons.UpdatableArgumentsMap;

/**
 * Same as {@link MoveCaretOperation}, but expands editor variables in
 * arguments.
 */
public class ExpandingMoveCaretOperation extends MoveCaretOperation {

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
	throws AuthorOperationException {

	// expand editor variables
	UpdatableArgumentsMap newArgs = new UpdatableArgumentsMap(args, getArguments());
	newArgs.expand("xpathLocation", authorAccess, true);
	newArgs.expand("position", authorAccess, true);
	newArgs.expand("selection", authorAccess, true);

	super.doOperation(authorAccess, newArgs);
    }

}

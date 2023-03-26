package de.wwu.scdh.oxbytei;

import java.util.Arrays;

import ro.sync.ecss.extensions.commons.operations.XSLTOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.oxbytei.commons.UpdatableArgumentsMap;


/**
 * {@link SurroundWithAnchorsXSLTOperation} first inserts empty anchor
 * elements around the user selection, then moves to a defined
 * position and performs an {@link XSLTOperation}, with the IDs of the
 * anchor elements passed to the XSL stylesteet.
 *
 */
public class SurroundWithAnchorsXSLTOperation extends XSLTOperation {

    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#getDescription()
     */
    @Override
    public String getDescription() {
	return "Insert empty anchor elements around the user selection and then perform an XSLT operation.";
    }

    SurroundWithAnchorsOperation anchorsOperation;

    private static final Logger LOGGER = LoggerFactory.getLogger(SurroundWithAnchorsXSLTOperation.class);

    public SurroundWithAnchorsXSLTOperation() {
	super();
	anchorsOperation = new SurroundWithAnchorsOperation();
    }

    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#getArguments()
     */
    @Override
    public ArgumentDescriptor[] getArguments() {
	ArgumentDescriptor[] anchors = anchorsOperation.getArguments();
	ArgumentDescriptor[] xslt = super.getArguments();
	// TODO: Is there a guarantee, that a missing 'value' argument
	// does not cause an exception? I.e. is it guaranteed, thtere
	// is no validation, before the arugments are passed to the
	// operation?
	ArgumentDescriptor[] all = Arrays.copyOf(xslt, xslt.length + anchors.length);
	System.arraycopy(anchors, 0, all, xslt.length, anchors.length);
	return all;
    }

    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
     */
    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
	throws AuthorOperationException {

	UpdatableArgumentsMap newArgs;

	// surround the user selection with anchors
	newArgs = new UpdatableArgumentsMap(args, anchorsOperation.getArguments());
	anchorsOperation.doOperation(authorAccess, newArgs);
	// get the anchors' IDs
	String startId = anchorsOperation.getStartId();
	String endId = anchorsOperation.getEndId();
	String anchorsContainer = anchorsOperation.getAnchorsContainer();

	// do XSLT
	newArgs = new UpdatableArgumentsMap(args, super.getArguments());
	// update stylesheet parameters
	String params = (String) newArgs.getArgumentValue("externalParams");
	if (params == null) {
	    params = "";
	}
	if (! params.isEmpty()) {
	    params += ", ";
	}
	params += "startId=" + startId + ", endId=" + endId + ", container=" + anchorsContainer;
	newArgs.update("externalParams", params);
	// expand editor variables in some arguments
	LOGGER.debug("targetLocation before expansion: {}", newArgs.getArgumentValue("targetLocation"));
	newArgs.expand("targetLocation", authorAccess, true);
	newArgs.expand("sourceLocation", authorAccess, true);
	newArgs.expand("action", authorAccess, true);
	newArgs.expand("externalParams", authorAccess, true);
	LOGGER.debug("targetLocation after expansion: {}", newArgs.getArgumentValue("targetLocation"));
	// do the operation
	super.doOperation(authorAccess, newArgs);
    }

}

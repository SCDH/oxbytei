package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;


/**
 * {@link SelectReferencedOperation} makes a user selection for the
 * nodes between the start and end location passed in as arguments.
 *
 * @author Christian Lück
 */
public class SelectReferencedOperation implements AuthorOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectReferencedOperation.class);

    public String getDescription() {
	return "Select the text between two referenced elements.";
    }

    private static final ArgumentDescriptor ARGUMENT_START =
	new ArgumentDescriptor("start",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The location of the start element.");

    private static final ArgumentDescriptor ARGUMENT_END =
	new ArgumentDescriptor("end",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The location of the end element.");

    public ArgumentDescriptor[] getArguments() {
	return new ArgumentDescriptor[] {
	    ARGUMENT_START,
	    ARGUMENT_END
	};
    }

    public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
	throws AuthorOperationException {

	// get XPaths from arguments
	String start =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_START.getName(), args, authorAccess, false);
	String end =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_END.getName(), args, authorAccess, false);

	LOGGER.debug("XPaths of the referenced text: {} to {}", start, end);

	// get offsets by XPaths
	int startOffset =
	    authorAccess.getDocumentController().getXPathLocationOffset(start, AuthorConstants.POSITION_BEFORE, true);
	int endOffset =
	    authorAccess.getDocumentController().getXPathLocationOffset(end, AuthorConstants.POSITION_AFTER, true);

	LOGGER.debug("Offsets of the referenced text: {} to {}", startOffset, endOffset);

	// select the text
	authorAccess.getEditorAccess().select(startOffset, endOffset);
    }

}

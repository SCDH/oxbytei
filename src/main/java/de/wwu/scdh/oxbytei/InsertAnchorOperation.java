package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;


/**
 * {@link InsertAnchorOperation} insert a single anchor add the
 * current caret position. The anchors ID is then present on the
 * {@code ${anchorId}} editor variable.
 *
 * Steps for getting the ID:
 *
 * 1) get string from arguments
 *
 * 2) expand editor variables in it
 *
 * 3) evaluate it as an XPath expression
 *
 * @author Christian Lück
 */
public class InsertAnchorOperation
    implements AuthorOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertAnchorOperation.class);

    @Override
    public String getDescription() {
	return "Insert an empty anchor element at current caret position.";
    }

    public static final ArgumentDescriptor ARGUMENT_ANCHOR =
	new ArgumentDescriptor("anchor",
			       ArgumentDescriptor.TYPE_STRING,
			       "The local name of the anchor element.");

    public static final ArgumentDescriptor ARGUMENT_ANCHOR_NAMESPACE =
	new ArgumentDescriptor("anchorNamespace",
			       ArgumentDescriptor.TYPE_STRING,
			       "The local name of the anchor element.");

    public static final ArgumentDescriptor ARGUMENT_ID_XPATH =
	new ArgumentDescriptor("anchorId",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
		 		"XPath expression defining how to get the ID of the anchor."
			       + "\nDefaults to the editor variable \"'\\$\\{id\\}'\" as constant.",
			       "'${id}'");

    @Override
    public ArgumentDescriptor[] getArguments() {
	return new ArgumentDescriptor[] {
	    ARGUMENT_ANCHOR,
	    ARGUMENT_ANCHOR_NAMESPACE,
	    ARGUMENT_ID_XPATH
	};
    }

    protected String anchorId;

    public String getAnchorId() {
	return anchorId;
    }

    @Override
    public void doOperation
	(AuthorAccess authorAccess,
	 ArgumentsMap arguments)
	throws  AuthorOperationException, IllegalArgumentException {

	// get start ID from arguments and expand editor variables
	String expandedIdXPath =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_ID_XPATH.getName(), arguments,
							      authorAccess, false);
	// evaluate it as an XPath expression
	Object[] anchorIds =
	    authorAccess.getDocumentController().evaluateXPath(expandedIdXPath, false, false, false);
	anchorId = (String) anchorIds[0];

	LOGGER.debug("anchor ID: {}", anchorId);

	// get name of the start anchor element
	String anchorNamespace =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_ANCHOR_NAMESPACE.getName(), arguments, null);
	String anchorLName =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_ANCHOR.getName(), arguments, authorAccess, true);
	String anchorQName = makeQName(anchorNamespace, anchorLName);

	int position = authorAccess.getEditorAccess().getCaretOffset();

	// get access to the editing API
	AuthorDocumentController doc = authorAccess.getDocumentController();

	// variable for accumulating editing results
	boolean success = true;

	// We have to insert the end anchor first, otherwise positions get lost!

	// create and insert end anchor
	AuthorElement tag = doc.createElement(anchorQName);
	success = success && doc.insertElement(position, tag);
	doc.setAttribute("xml:id", new AttrValue(anchorId), tag);

	if (!success) {
	    throw new AuthorOperationException("Failed to insert anchors");
	}

	// store in state variables
	GlobalState.anchorId = anchorId;
    }

    /**
     * Make a string representation of a QName.
     */
    private static String makeQName(String ns, String ln) {
	// TODO: DocumentController#createElement(String) takes a
	// qName as String. How is the namespace and the local name
	// delimited?

	// QName qname = new QName(ns, ln);
	// return qname.toString();
	return ln;
    }

}

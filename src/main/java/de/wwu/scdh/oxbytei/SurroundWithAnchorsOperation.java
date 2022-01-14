package de.wwu.scdh.oxbytei;

import javax.xml.namespace.QName;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.commons.operations.MoveCaretOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;
import de.wwu.scdh.oxbytei.commons.UpdatableArgumentsMap;


/**
 * {@link SurroundWithAnchors} can be used to make author mode
 * operations insert empty XML elements as anchors around the user
 * selection. It is intended for use in composition with classes, that
 * inherit from other author mode operations.
 *
 * Steps for getting IDs:
 *
 * 1) get string from arguments
 *
 * 2) expand editor variables in it
 *
 * 3) evaluate it as an XPath expression
 *
 * @author Christian Lück
 */
public class SurroundWithAnchorsOperation
    implements AuthorOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurroundWithAnchorsOperation.class);

    @Override
    public String getDescription() {
	return "Insert empty anchor elements at the start and end point of the user selection.";
    }

    public static final ArgumentDescriptor ARGUMENT_START_ANCHOR =
	new ArgumentDescriptor("startAnchor",
			       ArgumentDescriptor.TYPE_STRING,
			       "The local name of the start anchor element.");

    public static final ArgumentDescriptor ARGUMENT_START_ANCHOR_NAMESPACE =
	new ArgumentDescriptor("startAnchorNamespace",
			       ArgumentDescriptor.TYPE_STRING,
			       "The local name of the start anchor element.");

    public static final ArgumentDescriptor ARGUMENT_END_ANCHOR =
	new ArgumentDescriptor("endAnchor",
			       ArgumentDescriptor.TYPE_STRING,
			       "The local name of the end anchor element."
			       + "\nIf not given, the same name as start anchor is used.");

    public static final ArgumentDescriptor ARGUMENT_END_ANCHOR_NAMESPACE =
	new ArgumentDescriptor("endAnchorNamespace",
			       ArgumentDescriptor.TYPE_STRING,
			       "The local name of the end anchor element."
			       + "\nIf not given, the same namespace as start anchor is used.");

    public static final ArgumentDescriptor ARGUMENT_START_ID_XPATH =
	new ArgumentDescriptor("startId",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
		 		"XPath expression defining how to get the ID of the start anchor."
			       + "\nDefaults to the editor variable \"'\\$\\{id\\}'\" as constant.",
			       "'${id}'");

    public static final ArgumentDescriptor ARGUMENT_END_ID_XPATH =
	new ArgumentDescriptor("endId",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
		 		"XPath expression defining how to get the ID of the end anchor."
			       + "\nDefaults to the editor variable \"'\\$\\{id\\}'\" as constant.",
			       "'${id}'");

    public static final String[] ARGUMENT_MOVE_ALLOWED_VALUES = new String[] {
	AuthorConstants.ARG_VALUE_FALSE,
	AuthorConstants.ARG_VALUE_TRUE
    };

    public static final ArgumentDescriptor ARGUMENT_MOVE =
	new ArgumentDescriptor("moveToEnd",
			       ArgumentDescriptor.TYPE_CONSTANT_LIST,
			       "Move the caret to the end anchor after insertion of both anchors."
			       + " Without moving, the caret is at the start anchor after the operation."
			       + "\nDefaults to false.",
			       ARGUMENT_MOVE_ALLOWED_VALUES,
			       AuthorConstants.ARG_VALUE_FALSE);

    @Override
    public ArgumentDescriptor[] getArguments() {
	return new ArgumentDescriptor[] {
	    ARGUMENT_START_ANCHOR,
	    ARGUMENT_START_ANCHOR_NAMESPACE,
	    ARGUMENT_END_ANCHOR,
	    ARGUMENT_END_ANCHOR_NAMESPACE,
	    ARGUMENT_START_ID_XPATH,
	    ARGUMENT_END_ID_XPATH,
	    ARGUMENT_MOVE
	};
    }

    protected String startId;

    protected String endId;

    protected String anchorsContainer;

    public String getStartId() {
	return startId;
    }

    public String getEndId() {
	return endId;
    }

    @Override
    public void doOperation
	(AuthorAccess authorAccess,
	 ArgumentsMap arguments)
	throws  AuthorOperationException, IllegalArgumentException {

	// get start ID from arguments and expand editor variables
	String expandedStartXPath =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_START_ID_XPATH.getName(), arguments,
							      authorAccess, false);
	// evaluate it as an XPath expression
	Object[] startIds =
	    authorAccess.getDocumentController().evaluateXPath(expandedStartXPath, false, false, false);
	startId = (String) startIds[0];

	// get end ID from arguments and expand editor variables
	String expandedEndXPath =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_END_ID_XPATH.getName(), arguments,
							      authorAccess, false);
	// evaluate it as an XPath expression
	Object[] endIds =
	    authorAccess.getDocumentController().evaluateXPath(expandedEndXPath, false, false, false);
	endId = (String) endIds[0];

	LOGGER.debug("start ID: {}, end ID: {}", startId, endId);

	// get name of the start anchor element
	String startTagNamespace =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_START_ANCHOR_NAMESPACE.getName(), arguments, null);
	String endTagNamespace =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_END_ANCHOR_NAMESPACE.getName(), arguments, startTagNamespace);
	String startTagLName =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_START_ANCHOR.getName(), arguments, authorAccess, true);
	String startTagQName = makeQName(startTagNamespace, startTagLName);

	String endTagQName = startTagQName;
	try {
	    String endTagLName =
		OperationArgumentValidator.validateStringArgument(ARGUMENT_END_ANCHOR.getName(), arguments, authorAccess, true);
		endTagQName = makeQName(endTagNamespace, endTagLName);
	} catch (AuthorOperationException e) {
	    // do nothing, we take the value from the start tag
	} catch (IllegalArgumentException e) {
	    // do nothing, we take the value from the start tag
	}

	int startSelection = authorAccess.getEditorAccess().getSelectionStart();
	int endSelection = authorAccess.getEditorAccess().getSelectionEnd();

	// get access to the editing API
	AuthorDocumentController doc = authorAccess.getDocumentController();

	// variable for accumulating editing results
	boolean success = true;

	// We have to insert the end anchor first, otherwise positions get lost!

	// create and insert end anchor
	AuthorElement endTag = doc.createElement(startTagQName);
	success = success && doc.insertElement(endSelection, endTag);
	doc.setAttribute("xml:id", new AttrValue(endId), endTag);

	// create and insert start anchor
	AuthorElement startTag = doc.createElement(endTagQName);
	success = success && doc.insertElement(startSelection, startTag);
	doc.setAttribute("xml:id", new AttrValue(startId), startTag);

	// get XPath of the deepest element, that contains both anchors
	// (//*[descendant::*[@xml:id = 'a1'] and descendant::*[@xml:id = 'a2']])[last()]
	String containerXPath = "string-join(for $node in (//*[descendant::*[@xml:id = '" + startId + "'] and descendant::*[@xml:id = '" + endId + "']]) return concat('*:', name($node), '[', count(preceding-sibling::*[name() eq name($node)]) + 1, ']'), '/')";
	Object[] container =
	    authorAccess.getDocumentController().evaluateXPath(containerXPath, true, false, false);
	anchorsContainer = null;
	try {
	    anchorsContainer = "/" + (String) container[0];
	} catch (IndexOutOfBoundsException e) {
	    throw new AuthorOperationException("Failed to get the XPath of the anchors' container");
	}
	LOGGER.debug("The anchors' container is {}", anchorsContainer);

	if (!success) {
	    throw new AuthorOperationException("Failed to insert anchors");
	}

	// store in state variables
	GlobalState.startAnchorId = startId;
	GlobalState.endAnchorId = endId;

	// move the caret if that's on the arguments
	String moveString =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_MOVE.getName(), arguments);
	moveString = authorAccess.getUtilAccess().expandEditorVariables(moveString, authorAccess.getEditorAccess().getEditorLocation());
	boolean move = moveString.equals(AuthorConstants.ARG_VALUE_TRUE);
	LOGGER.debug("moveToEnd: {}, logical value: {}", moveString, move);
	if (move) {
	    LOGGER.debug("Moving the caret to the end anchor.");
	    AuthorOperation moveOperation = new MoveCaretOperation();
	    UpdatableArgumentsMap newArgs = new UpdatableArgumentsMap(arguments, moveOperation.getArguments());
	    newArgs.update("xpathLocation", "//*[@xml:id eq '" + endId + "']");
	    newArgs.update("position", "Before");
	    newArgs.update("selection", "None");
	    moveOperation.doOperation(authorAccess, newArgs);
	}
	
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

package de.wwu.scdh.oxbytei;

import java.util.Arrays;
import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.commons.operations.ChangeAttributeOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;
import de.wwu.scdh.oxbytei.commons.UpdatableArgumentsMap;


/**
 * {@link IdPicker} picks the ID of the element at the current editing
 * position (caret) and stores it to the <code>{anchorId}</code>
 * editor variable. If no ID is present, one is generated on the basis
 * of the configuration arguments and it is then added to the element.
 */
public class IdPicker extends ChangeAttributeOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdPicker.class);

    @Override
    public String getDescription() {
	return "Pick the ID from the element at the caret or assign one";
    }

    public static final ArgumentDescriptor ARGUMENT_ID_XPATH =
	new ArgumentDescriptor("id-xpath",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression for making a missing ID"
			       + "\nDefaults to the editor variable \"'\\$\\{id\\}'\" as constant.",
			       "'${id}'");

    @Override
    public ArgumentDescriptor[] getArguments() {
	ArgumentDescriptor[] help = {
	    ARGUMENT_ID_XPATH
	};
	ArgumentDescriptor[] sup = super.getArguments();
	// TODO: Is there a guarantee, that a missing 'value' argument
	// does not cause an exception? I.e. is it guaranteed, thtere
	// is no validation, before the arugments are passed to the
	// operation?
	ArgumentDescriptor[] all = Arrays.copyOf(sup, sup.length + help.length);
	System.arraycopy(help, 0, all, sup.length, help.length);
	return all;
    }


    @Override
    public void doOperation
	(AuthorAccess authorAccess,
	 ArgumentsMap arguments)
	throws  AuthorOperationException, IllegalArgumentException {

	// read arguments, editor variables are expanded
	String idAttribute = OperationArgumentValidator.validateStringArgument("name", arguments, authorAccess, false);
	String idXPath = OperationArgumentValidator.validateStringArgument(ARGUMENT_ID_XPATH.getName(), arguments, authorAccess, false);

	// evaluate idXPath as an XPath expression in the context of the current document
	Object[] anchorIds =
	    authorAccess.getDocumentController().evaluateXPath(idXPath, false, false, false);
	String generatedId = (String) anchorIds[0];

	// get access to the editing API
	AuthorDocumentController doc = authorAccess.getDocumentController();

	int position = authorAccess.getEditorAccess().getCaretOffset();

	try {
	    // get the current context element
	    AuthorNode currentNode = doc.getNodeAtOffset(position);
	    AuthorElement context;
	    if (currentNode.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
		context = (AuthorElement) currentNode;
	    } else {
		context = (AuthorElement) currentNode.getParent();
	    }

	    // get the ID or set generated ID if not present
	    AttrValue idAttrValue = context.getAttribute(idAttribute);
	    if (idAttrValue == null) {
		// pass over to super class
		UpdatableArgumentsMap newArgs = new UpdatableArgumentsMap(arguments, super.getArguments());
		newArgs.update("value", (Object) generatedId);
		super.doOperation(authorAccess, newArgs);
		// store ID
		GlobalState.anchorId = generatedId;
	    } else {
		GlobalState.anchorId = idAttrValue.getValue();
	    }
	    LOGGER.info("ID {} stored to \\{anchorId\\} editor variable", GlobalState.anchorId);
	} catch (BadLocationException e) {
	    LOGGER.error("failed to get node at position {}", position);
	    throw new AuthorOperationException(e.getMessage());
	}
    }

}

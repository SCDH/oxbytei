/**
 * PrefixURIChangeAttributeOperation - is a class for setting an
 * attribute value with a selection from the values defined in a
 * target given in a <prefixDef> element.
 *
 */
package de.wwu.scdh.oxbytei;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;
import de.wwu.scdh.oxbytei.AbstractOperation;


public class PrefixURIChangeAttributeOperation
    extends AbstractOperation
    implements AuthorOperation {

    private static final ArgumentDescriptor ARGUMENT_ATTRIBUTE =
	new ArgumentDescriptor("attribute",
			       ArgumentDescriptor.TYPE_STRING,
			       "The attribute which the link goes into.");

    private static final ArgumentDescriptor ARGUMENT_LOCATION =
	new ArgumentDescriptor("location",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "An XPath 2.0 locating the element on which the link is to be stored."
			       + " Defaults to the current element context.",
			       "self::*");

    private static final String[] ARGUMENT_MULTIPLE_ALLOWED_VALUES = new String[] {
	AuthorConstants.ARG_VALUE_FALSE,
	AuthorConstants.ARG_VALUE_TRUE
    };

    private static final ArgumentDescriptor ARGUMENT_MULTIPLE =
	new ArgumentDescriptor("multiple",
			       ArgumentDescriptor.TYPE_CONSTANT_LIST,
			       "Whether or not multiple selections are allowed."
			       + " Defaults to false.",
			       ARGUMENT_MULTIPLE_ALLOWED_VALUES,
			       AuthorConstants.ARG_VALUE_FALSE);

    private static final ArgumentDescriptor ARGUMENT_MESSAGE =
	new ArgumentDescriptor("message",
			       ArgumentDescriptor.TYPE_STRING,
			       "The message in the user dialog.");

    private static final ArgumentDescriptor ARGUMENT_DIALOG =
	new ArgumentDescriptor("dialog",
			       ArgumentDescriptor.TYPE_STRING,
			       "The user dialogue used for this operation.",
			       "de.wwu.scdh.oxbytei.commons.OxygenSelectionDialog");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
	ARGUMENT_ATTRIBUTE,
	ARGUMENT_LOCATION,
	ARGUMENT_MULTIPLE,
	ARGUMENT_MESSAGE,
	ARGUMENT_DIALOG
    };

    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#getArguments()
     */
    public ArgumentDescriptor[] getArguments() {
	return ARGUMENTS;
    }

    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#getDescription()
     */
    public String getDescription() {
	return "Set an attribute by presenting the user a selection generated from <prefixDef> in the current file context and from configuration.";
    }
    
    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation()
     */
    public void doOperation(AuthorAccess auAccess, ArgumentsMap args)
	throws AuthorOperationException, IllegalArgumentException {

	// Validate arguments
	attributeName = OperationArgumentValidator.validateStringArgument(ARGUMENT_ATTRIBUTE.getName(), args);
	String location = OperationArgumentValidator.validateStringArgument(ARGUMENT_LOCATION.getName(), args);
	String multipleString = OperationArgumentValidator.validateStringArgument(ARGUMENT_MULTIPLE.getName(), args);
	message = OperationArgumentValidator.validateStringArgument(ARGUMENT_MESSAGE.getName(), args);
	dialog = OperationArgumentValidator.validateStringArgument(ARGUMENT_DIALOG.getName(), args);

	multiple = multipleString.equals(AuthorConstants.ARG_VALUE_TRUE);

	authorAccess = auAccess;

	int selStart = auAccess.getEditorAccess().getSelectionStart();
	try {
	    // get location, which must be set for subsequent method calls
	    AuthorDocumentController doc = auAccess.getDocumentController();
	    AuthorNode selectionContext = doc.getNodeAtOffset(selStart);
	    locationNode =
		(AuthorElement) (doc.findNodesByXPath((String) location, selectionContext, false, true, true, false))[0];

	    // set up the providers from prefix definitions
	    setupLabelledEntriesProviders();

	    // call setAttribute() to open user dialog and set the attribute
	    setAttribute();
	} catch (BadLocationException e) {
	    // This can occur on getNodeAtOffset()
	    System.err.println("Error: At bad editor location. Offset " + selStart);
	} catch (IndexOutOfBoundsException e) {
	    // This occurs, when the XPath of the 'location'
	    // argument does not return an elemnt. Then the
	    // accessing the first element of the array returned
	    // by findNodesByXPath, [0], fails.
	    throw new AuthorOperationException("An error occured\n"
					       + "Please check the XPath expression given as `location`!\n\n"
					       + e);
	}

    }

}

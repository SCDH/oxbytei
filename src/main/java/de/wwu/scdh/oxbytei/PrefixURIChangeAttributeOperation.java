/**
 * PrefixURIChangeAttributeOperation - is a class for setting an
 * attribute value with a selection from the values defined in a
 * target given in a <prefixDef> element.
 *
 */
package de.wwu.scdh.oxbytei;

import javax.swing.text.BadLocationException;

import org.w3c.dom.Attr;

import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;
import de.wwu.scdh.oxbytei.commons.ISelectionDialog;
import de.wwu.scdh.oxbytei.commons.EdiarumSelectionDialog;
import de.wwu.scdh.oxbytei.commons.OxygenSelectionDialog;


public class PrefixURIChangeAttributeOperation
    extends AbstractPrefixURIOperation
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
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "Whether or not multiple selections are allowed."
			       + " Defaults to false.",
			       ARGUMENT_MULTIPLE_ALLOWED_VALUES,
			       AuthorConstants.ARG_VALUE_FALSE);

    private static final ArgumentDescriptor ARGUMENT_MESSAGE =
	new ArgumentDescriptor("message",
			       ArgumentDescriptor.TYPE_STRING,
			       "The message in the user dialog.");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
	ARGUMENT_ATTRIBUTE,
	ARGUMENT_LOCATION,
	ARGUMENT_MULTIPLE,
	ARGUMENT_MESSAGE
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
	// FIXME
	return "FIXME";
    }
    
    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation()
     */
    public void doOperation(final AuthorAccess authorAccess, final ArgumentsMap args)
	throws AuthorOperationException, IllegalArgumentException {

	// Validate arguments
	final String attributeName = OperationArgumentValidator.validateStringArgument(ARGUMENT_ATTRIBUTE.getName(), args);
	final String location = OperationArgumentValidator.validateStringArgument(ARGUMENT_LOCATION.getName(), args);
	final String multiple = OperationArgumentValidator.validateStringArgument(ARGUMENT_MULTIPLE.getName(), args);
	final String message = OperationArgumentValidator.validateStringArgument(ARGUMENT_MESSAGE.getName(), args);

	// put the selected URI into the attribute value
	try {
	    // get location and current attribute value
	    int selStart = authorAccess.getEditorAccess().getSelectionStart();
	    AuthorDocumentController doc = authorAccess.getDocumentController();
	    AuthorNode selectionContext = doc.getNodeAtOffset(selStart);
	    AuthorNode locationNode =
		(AuthorElement) (doc.findNodesByXPath((String) location, selectionContext, false, true, true, false))[0];
	    AuthorElement locationElement = (AuthorElement) locationNode;
	    Object[] attrNodes =
		doc.evaluateXPath("@" + attributeName, locationNode, false, false, false, false);
	    String currentId = "";
	    boolean attributePresent = false;
	    if (attrNodes.length > 0) {
		currentId = ((Attr) attrNodes[0]).getValue();
		attributePresent = true;
	    }

	    // do user interaction
	    ISelectionDialog dialog = new OxygenSelectionDialog(); // FIXME: make pluggable
	    dialog.init(authorAccess, message, multiple, currentId, getConfiguredProviders(authorAccess));
	    String selectedId = dialog.doUserInteraction();

	    // set attribute
	    if (!(selectedId.isEmpty())) {
		doc.setAttribute(attributeName,
				 new AttrValue(selectedId),
				 locationElement);
	    } else {
		// remove attribute
		doc.removeAttribute(attributeName, locationElement);
	    }
	}
	catch (BadLocationException e) {
	    // ???
	}
	catch (IndexOutOfBoundsException e) {
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

/**
 * PrefixURIChangeAttributeOperation - is a class for setting an
 * attribute value with a selection from the values defined in a
 * target given in a <prefixDef> element.
 *
 */
package de.wwu.scdh.oxbytei;

import java.util.List;
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
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;


public class PrefixURIChangeAttributeOperation
    extends AbstractPrefixURIOperation
    implements AuthorOperation {


    private String attributeName;
    private String multiple;
    private String message;
    private AuthorAccess authorAccess;
    private AuthorNode locationNode;

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
    public void doOperation(AuthorAccess auAccess, ArgumentsMap args)
	throws AuthorOperationException, IllegalArgumentException {

	// Validate arguments
	attributeName = OperationArgumentValidator.validateStringArgument(ARGUMENT_ATTRIBUTE.getName(), args);
	String location = OperationArgumentValidator.validateStringArgument(ARGUMENT_LOCATION.getName(), args);
	multiple = OperationArgumentValidator.validateStringArgument(ARGUMENT_MULTIPLE.getName(), args);
	message = OperationArgumentValidator.validateStringArgument(ARGUMENT_MESSAGE.getName(), args);

	authorAccess = auAccess;

	int selStart = authorAccess.getEditorAccess().getSelectionStart();
	try {
	    // get location
	    AuthorDocumentController doc = authorAccess.getDocumentController();
	    AuthorNode selectionContext = doc.getNodeAtOffset(selStart);
	    locationNode =
		(AuthorElement) (doc.findNodesByXPath((String) location, selectionContext, false, true, true, false))[0];
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

    /**
     * Set the attribute given by {@link attributeName} on the
     * {@link locationNode} node.
     */
    private void setAttribute()	throws AuthorOperationException {

	// get current attribute value
	AuthorDocumentController doc = authorAccess.getDocumentController();
	Object[] attrNodes =
	    doc.evaluateXPath("@" + attributeName, locationNode, false, false, false, false);
	String currentId = "";
	boolean attributePresent = false;
	if (attrNodes.length > 0) {
	    currentId = ((Attr) attrNodes[0]).getValue();
	    attributePresent = true;
	}

	// get initialized providers
	List<ILabelledEntriesProvider> providers = getConfiguredProviders(authorAccess);

	// do user interaction
	ISelectionDialog dialog = new OxygenSelectionDialog(); // TODO: make pluggable
	dialog.init(authorAccess, message, multiple, currentId, providers);
	String selectedId = dialog.doUserInteraction();

	// set the attribute value
	// null returned form doUserInteraction means cancellation
	if (selectedId != null) {
	    AuthorElement locationElement = (AuthorElement) locationNode;
	    // set attribute if not empty string
	    if (!(selectedId.isEmpty())) {
		AttrValue val = new AttrValue(selectedId);
		doc.setAttribute(attributeName, val, locationElement);
	    } else {
		// remove attribute if empty string
		doc.removeAttribute(attributeName, locationElement);
	    }
	}
    }
    
}

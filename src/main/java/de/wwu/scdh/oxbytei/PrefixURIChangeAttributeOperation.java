/**
 * PrefixURIChangeAttributeOperation - is a class for setting an
 * attribute value with a selection from the values defined in a
 * target given in a <prefixDef> element.
 *
 */
package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;
import de.wwu.scdh.teilsp.completion.PrefixDef;

import javax.swing.text.BadLocationException;

public class PrefixURIChangeAttributeOperation
    implements AuthorOperation {

    private static final ArgumentDescriptor ARGUMENT_ATTRIBUTE =
	new ArgumentDescriptor("attribute",
			       ArgumentDescriptor.TYPE_STRING,
			       "The attribute which the link goes into.");

    private static final ArgumentDescriptor ARGUMENT_PREFIX =
	new ArgumentDescriptor("prefix",
			       ArgumentDescriptor.TYPE_STRING,
			       "The prefix of the URI scheme given in prefixDef/@ident."
			       + " This may be a regular expression, following the XPath flavour, e.g. ^(plc|place)."
			       + " It must be present in your TEI document.");

    private static final ArgumentDescriptor ARGUMENT_PREFIX_LOCAL =
	new ArgumentDescriptor("prefixLocalVariable",
			       ArgumentDescriptor.TYPE_STRING,
			       "Optioal: The name of the editor variable for overwriting the 'prefix' argument."
			       + " Default: 'oxbytei.uri.<PREFIX>.prefix'.");

    private static final ArgumentDescriptor ARGUMENT_LOCATION =
	new ArgumentDescriptor("location",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "An XPath 2.0 locating the element on which the link is to be stored."
			       + " Defaults to the current element context.",
			       "self::*");

    private static final ArgumentDescriptor ARGUMENT_LOCATION_LOCAL =
	new ArgumentDescriptor("locationLocalVariable",
			       ArgumentDescriptor.TYPE_STRING,
			       "Optional: The name of the editor variable for overwriting the 'location' argument."
			       + " Default: 'oxbytei.uri.<PREFIX>.location'.");

    private static final ArgumentDescriptor ARGUMENT_SELECTION =
	new ArgumentDescriptor("selection",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression to use for generating the labels of selection values."
			       + " This should regard the structure of the referred TEI document.",
			       "self::*");

    private static final ArgumentDescriptor ARGUMENT_SELECTION_LOCAL =
	new ArgumentDescriptor("selectionLocalVariable",
			       ArgumentDescriptor.TYPE_STRING,
			       "Optional: The name of the editor variable for overwriting the 'selection' argument."
			       + " Default: 'oxbytei.uri.<PREFIX>.selection'.");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
	ARGUMENT_ATTRIBUTE,
	ARGUMENT_PREFIX,
	ARGUMENT_PREFIX_LOCAL,
	ARGUMENT_LOCATION,
	ARGUMENT_LOCATION_LOCAL,
	ARGUMENT_SELECTION,
	ARGUMENT_SELECTION_LOCAL
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
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
	throws AuthorOperationException, IllegalArgumentException {

	// Validate arguments
	String attributeName = OperationArgumentValidator.validateStringArgument(ARGUMENT_ATTRIBUTE.getName(), args);
	String prefix = OperationArgumentValidator.validateStringArgument(ARGUMENT_PREFIX.getName(), args);
	String location = OperationArgumentValidator.validateStringArgument(ARGUMENT_LOCATION.getName(), args);

	//String prefixLocal = OperationArgumentValidator.validateStringArgument(ARGUMENT_PREFIX_LOCAL, args);

	// Get prefixDef elements from current document
	AuthorNode[] prefixNodes;
	String xpathToPrefixDef = "//*:prefixDef[matches(@ident, '" + prefix + "')]";
	try {
	    prefixNodes = authorAccess.getDocumentController().findNodesByXPath(xpathToPrefixDef, true, true, true);
	}
	catch (AuthorOperationException e) {
	    // this may be thrown by findNodesByXPath
	    throw new AuthorOperationException("prefixDef with @ident='" + prefix + "' not found\n\n" + e);
	}

	System.err.println(xpathToPrefixDef);
	System.err.println(prefixNodes.length);

	// store the prefixDef elements into a PrefixDef array
	PrefixDef[] prefixDefs = new PrefixDef[prefixNodes.length];
	int i;
	for (i = 0; i < prefixNodes.length; i++) {
	    prefixDefs[i] = new PrefixDef((AuthorElement)prefixNodes[i]);
	    System.err.println("prefixDef: "
			       + prefixDefs[i].getIdent() + " "
			       + prefixDefs[i].getReplacementPattern() + " "
			       + prefixDefs[i].getMatchPattern() + "\n");
	}


	String xpathFromSelection = "self::*";

	String selectedId = "somewhere_out_there";
	

	if (!(selectedId == "")) {
	    try {
		int selStart = authorAccess.getEditorAccess().getSelectionStart();
		AuthorNode selNode = authorAccess.getDocumentController().getNodeAtOffset(selStart);
		AuthorElement selElement = (AuthorElement) (authorAccess.getDocumentController().findNodesByXPath((String) location, selNode, false, true, true, false))[0];
		
		String newAttributeVal = prefix + ":" + selectedId;
		authorAccess.getDocumentController().setAttribute(attributeName,
								  new AttrValue(newAttributeVal),
								  selElement);
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
	};
    }
    
}

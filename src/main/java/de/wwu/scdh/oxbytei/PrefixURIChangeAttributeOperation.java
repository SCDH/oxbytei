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
import de.wwu.scdh.oxbytei.commons.PrefixDef;

import javax.swing.text.BadLocationException;

public class PrefixURIChangeAttributeOperation
    implements AuthorOperation {

    private static final String ARGUMENT_ATTRIBUTE = "attribute";

    private static final String ARGUMENT_PREFIX = "prefix";

    private static final String ARGUMENT_PREFIX_LOCAL = "prefixLocalVariable";

    private static final String ARGUMENT_LOCATION = "location";

    private static final String ARGUMENT_LOCATION_LOCAL = "locationLocalVariable";

    private static final String ARGUMENT_SELECTION = "selection";

    private static final String ARGUMENT_SELECTION_LOCAL = "selectionLocalVariable";

    private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
	new ArgumentDescriptor(ARGUMENT_ATTRIBUTE,
			       ArgumentDescriptor.TYPE_STRING,
			       "The attribute which the link goes into."),
	new ArgumentDescriptor(ARGUMENT_PREFIX,
			       ArgumentDescriptor.TYPE_STRING,
			       "The prefix of the URI scheme given in prefixDef/@ident."
			       + "This must be present in your TEI document."),
	new ArgumentDescriptor(ARGUMENT_PREFIX_LOCAL,
			       ArgumentDescriptor.TYPE_STRING,
			       "The name of the editor variable for overwriting the 'prefix' argument."
			       + "Default: 'oxbytei.uri.<PREFIX>.prefix'."),
	new ArgumentDescriptor(ARGUMENT_LOCATION,
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "An XPath locating the element on which the link is to be stored."
			       + "Defaults to the current element context.",
			       "self::*"),
	new ArgumentDescriptor(ARGUMENT_LOCATION_LOCAL,
			       ArgumentDescriptor.TYPE_STRING,
			       "The name of the editor variable for overwriting the 'location' argument."
			       + "Default: 'oxbytei.uri.<PREFIX>.location'."),
	new ArgumentDescriptor(ARGUMENT_SELECTION,
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression to use for generating the labels of selection values."
			       + "This should regard the structure of the referred TEI document.",
			       "self::*"),
	new ArgumentDescriptor(ARGUMENT_SELECTION_LOCAL,
			       ArgumentDescriptor.TYPE_STRING,
			       "The name of the editor variable for overwriting the 'selection' argument."
			       + "Default: 'oxbytei.uri.<PREFIX>.selection'.")
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
	String attributeName = OperationArgumentValidator.validateStringArgument(ARGUMENT_ATTRIBUTE, args);
	String prefix = OperationArgumentValidator.validateStringArgument(ARGUMENT_PREFIX, args);
	String location = OperationArgumentValidator.validateStringArgument(ARGUMENT_LOCATION, args);

	//String prefixLocal = OperationArgumentValidator.validateStringArgument(ARGUMENT_PREFIX_LOCAL, args);

	// Get prefixDef elements from current document
	AuthorNode[] prefixNodes;
	String xpathToPrefixDef = "//*:prefixDef[matches(@ident, '" + prefix + "')]";
	try {
	    prefixNodes = authorAccess.getDocumentController().findNodesByXPath(xpathToPrefixDef, true, true, true);
	}
	catch (AuthorOperationException e) {
	    // this may be thrown by findNodesByXPath
	    throw new AuthorOperationException("prefixDef with @ident='" + prefix + "not found\n\n" + e);
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

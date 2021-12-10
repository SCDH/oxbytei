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
	//String prefixLocal = OperationArgumentValidator.validateStringArgument(ARGUMENT_PREFIX_LOCAL, args);

	String xpathFromSelection = "self::*";

	String selectedId = "some";
	

	if (!(selectedId == "")) {
	    try {
		int selStart = authorAccess.getEditorAccess().getSelectionStart();
		AuthorNode selNode = authorAccess.getDocumentController().getNodeAtOffset(selStart);
		AuthorElement selElement = (AuthorElement) (authorAccess.getDocumentController().findNodesByXPath((String) xpathFromSelection, selNode, false, true, true, false))[0];
		
		String newAttributeVal = prefix + ":" + selectedId;
		authorAccess.getDocumentController().setAttribute(attributeName,
								  new AttrValue(newAttributeVal),
								  selElement);
	    } catch (BadLocationException e) {} // ???
	};
    }
    
}

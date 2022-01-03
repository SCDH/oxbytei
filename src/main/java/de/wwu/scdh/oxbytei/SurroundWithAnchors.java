package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;


/**
 * {@link SurroundWithAnchors} can be used to make author mode
 * operations insert empty XML elements as anchors around the user
 * selection. It is intended for use in composition with classes, that
 * inherit from other author mode operations.
 *
 * @author Christian Lück
 */
public class SurroundWithAnchors {

    public static final ArgumentDescriptor ARGUMENT_START_ID_XPATH =
	new ArgumentDescriptor("startIdXPath",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
		 		"XPath expression defining how to get the ID of the start anchor."
			       + "\nDefaults to the editor variable \\$\\{id\\}",
			       "'${id}'");
    
    public static final ArgumentDescriptor ARGUMENT_END_ID_XPATH =
	new ArgumentDescriptor("endIdXPath",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
		 		"XPath expression defining how to get the ID of the end anchor."
			       + "\nDefaults to the editor variable \\$\\{id\\}",
			       "'${id}'");

    public static ArgumentDescriptor[] getArguments() {
	return new ArgumentDescriptor[] {
	    ARGUMENT_START_ID_XPATH,
	    ARGUMENT_END_ID_XPATH
	};
    }
    
    protected String startId;

    protected String endId;
    
    public SurroundWithAnchors
	(AuthorAccess authorAccess,
	 ArgumentsMap arguments)
	throws  AuthorOperationException {
	String startXPath = null; // TODO
	String expandedStartXPath = null; // TODO
	Object[] startIds = authorAccess.getDocumentController().evaluateXPath(expandedStartXPath, false, false, false);
	startId = (String) startIds[0];
	
    }

    public void doInsertion() {
    }
}

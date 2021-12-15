/**
 * PrefixURIChangeAttributeOperation - is a class for setting an
 * attribute value with a selection from the values defined in a
 * target given in a <prefixDef> element.
 *
 */
package de.wwu.scdh.oxbytei;

import java.net.MalformedURLException;
import java.awt.Frame;
import javax.swing.text.BadLocationException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.util.UtilAccess;

import org.bbaw.telota.ediarum.InsertRegisterDialog;

import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntries;
import de.wwu.scdh.teilsp.services.extensions.ArgumentsExtractor;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.exceptions.ProviderNotFoundException;
import de.wwu.scdh.teilsp.completion.PrefixDef;
import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;
import de.wwu.scdh.oxbytei.commons.Resolver;


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

    private static final ArgumentDescriptor ARGUMENT_PROVIDER =
	new ArgumentDescriptor("provider",
			       ArgumentDescriptor.TYPE_STRING,
			       "The full qualified (dotted) name of the provider class.",
			       "de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXML");

    private static final ArgumentDescriptor ARGUMENT_PROVIDER_ARGUMENTS =
	new ArgumentDescriptor("providerArguments",
			       ArgumentDescriptor.TYPE_STRING,
			       "Arguments for the provider.");


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

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
	ARGUMENT_ATTRIBUTE,
	ARGUMENT_PREFIX,
	ARGUMENT_PREFIX_LOCAL,
	ARGUMENT_LOCATION,
	ARGUMENT_LOCATION_LOCAL,
	ARGUMENT_PROVIDER,
	ARGUMENT_PROVIDER_ARGUMENTS,
	ARGUMENT_MULTIPLE
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
	String provider = OperationArgumentValidator.validateStringArgument(ARGUMENT_PROVIDER.getName(), args);
	String providerArgs = OperationArgumentValidator.validateStringArgument(ARGUMENT_PROVIDER_ARGUMENTS.getName(), args);
	String multiple = OperationArgumentValidator.validateStringArgument(ARGUMENT_MULTIPLE.getName(), args);

	//String prefixLocal = OperationArgumentValidator.validateStringArgument(ARGUMENT_PREFIX_LOCAL, args);

	UtilAccess utilAccess;
	utilAccess = authorAccess.getUtilAccess();
	//utilAccess.addCustomEditorVariablesResolver(null);

	String myvar = utilAccess.expandEditorVariables("${myvar}", null);
	System.err.println("editor variable: " + myvar);

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

	// we need a copy enriched by the parameters from prefixDef
	Map<String, String> parsedArgs, enrichedArgs;
	try {
	    parsedArgs = ArgumentsExtractor.arguments(providerArgs);
	} catch (Exception e) {
	    throw new IllegalArgumentException("Invalid argument 'providerArguments'\n\n" + e);
	}
	
	// store the prefixDef elements into a PrefixDef array
	final int l = prefixNodes.length;
	PrefixDef[] prefixDefs = new PrefixDef[l];
	List<LabelledEntry> items = new ArrayList<LabelledEntry>();
	int i;
	for (i = 0; i < l; i++) {
	    prefixDefs[i] = new PrefixDef((AuthorElement)prefixNodes[i]);
	    System.err.println("prefixDef: "
			       + prefixDefs[i].getIdent() + " "
			       + prefixDefs[i].getReplacementPattern() + " "
			       + prefixDefs[i].getMatchPattern() + "\n");
	    try {
		// enrich arguments with data found in prefixDef
		enrichedArgs = new HashMap<String, String>(parsedArgs);
		enrichedArgs.put("uri", Resolver.resolve(authorAccess, prefixDefs[i]));
		enrichedArgs.put("prefix", prefixDefs[i].getIdent() + ":");
		// get the provider and call getLabelledEntries() on it
		items.addAll(LabelledEntries.provider(provider).getLabelledEntries(enrichedArgs));
	    } catch (ProviderNotFoundException e) {
		throw new AuthorOperationException("Plugin not found: " + provider + "\n\n" + e);
	    } catch (ExtensionException e) {
		throw new AuthorOperationException("Error while reading from URI given in "
						   + prefixDefs[i].getReplacementPattern()
						   + "\nCaused by plugin " + provider
						   + "\n\n" + e);
	    } catch (MalformedURLException e) {
		throw new AuthorOperationException("Malformed URL " + e);
	    }
		    
	}

	int total = items.size();

	// get keys and labels into separate string arrays, which are
	// needed for the dialog.
	Iterator<LabelledEntry> iter = items.iterator();
	String[] keys = new String[total];
	String[] labels = new String[total];
	int j = 0;
	LabelledEntry entry;
	while (iter.hasNext()) {
	    entry = iter.next();
	    keys[j] = entry.getKey();
	    labels[j] = entry.getLabel();
	    j++;
	}

	// Ask the user for selection
	InsertRegisterDialog dialog =
	    new InsertRegisterDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(),
				     labels,
				     keys,
				     ((String) multiple).equals(AuthorConstants.ARG_VALUE_TRUE));
	String selectedId = dialog.getSelectedID(); //"somewhere_out_there";
	
	// put the selected URI into the attribute value
	if (!(selectedId.isEmpty())) {
	    try {
		int selStart = authorAccess.getEditorAccess().getSelectionStart();
		AuthorNode selNode = authorAccess.getDocumentController().getNodeAtOffset(selStart);
		AuthorElement selElement = (AuthorElement) (authorAccess.getDocumentController().findNodesByXPath((String) location, selNode, false, true, true, false))[0];
		
		String newAttributeVal = selectedId;
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

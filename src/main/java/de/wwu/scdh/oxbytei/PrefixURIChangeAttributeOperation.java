/**
 * PrefixURIChangeAttributeOperation - is a class for setting an
 * attribute value with a selection from the values defined in a
 * target given in a <prefixDef> element.
 *
 */
package de.wwu.scdh.oxbytei;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URISyntaxException;
import java.awt.Frame;
import javax.swing.text.BadLocationException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerException;

import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.util.UtilAccess;
import ro.sync.exml.workspace.api.util.XMLUtilAccess;

import org.bbaw.telota.ediarum.InsertRegisterDialog;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntries;
import de.wwu.scdh.teilsp.services.extensions.ArgumentsExtractor;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.exceptions.ProviderNotFoundException;
import de.wwu.scdh.teilsp.tei.PrefixDef;
import de.wwu.scdh.teilsp.config.ArgumentsConditionsPair;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
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
    public void doOperation(final AuthorAccess authorAccess, final ArgumentsMap args)
	throws AuthorOperationException, IllegalArgumentException {

	// Validate arguments
	final String attributeName = OperationArgumentValidator.validateStringArgument(ARGUMENT_ATTRIBUTE.getName(), args);
	final String prefix = OperationArgumentValidator.validateStringArgument(ARGUMENT_PREFIX.getName(), args);
	final String location = OperationArgumentValidator.validateStringArgument(ARGUMENT_LOCATION.getName(), args);
	//final String provider = OperationArgumentValidator.validateStringArgument(ARGUMENT_PROVIDER.getName(), args);
	//final String providerArgs = OperationArgumentValidator.validateStringArgument(ARGUMENT_PROVIDER_ARGUMENTS.getName(), args);
	final String multiple = OperationArgumentValidator.validateStringArgument(ARGUMENT_MULTIPLE.getName(), args);

	//String prefixLocal = OperationArgumentValidator.validateStringArgument(ARGUMENT_PREFIX_LOCAL, args);

	// Load plugins
	List<ILabelledEntriesProvider> entriesProviders = LabelledEntries.providers();

	// get access to utilities
	UtilAccess utilAccess = authorAccess.getUtilAccess();

	// get the uri resolver used by oxygen
	URIResolver resolver = authorAccess.getXMLUtilAccess().getURIResolver();

	// get the URL of the configuration file
	String defaultConfigFile = utilAccess.expandEditorVariables(OxbyteiConstants.DEFAULT_CONFIG_FILE, null);
	String configFile = defaultConfigFile;
	try {
	    // use resolver with xml catalogs
	    configFile = resolver.resolve(defaultConfigFile, null).getSystemId();
	} catch (TransformerException e) {}

	System.err.println("loading config from " + configFile);

	// read the plugin configuration from the config file
	List<ExtensionConfiguration> extensionsConfiguration = new ArrayList<ExtensionConfiguration>();
	try {
	    extensionsConfiguration = ExtensionConfigurationReader.getExtensionsConfiguration(configFile);
	} catch (ConfigurationException e) {
	    throw new AuthorOperationException("Error reading config from '"
					       + configFile +
					       "'\n\nDetails:\n"
					       + e);
	}

	// we need some iteration variables
	int i, j, k, m;

	// a nested class to keep track of configured plugins
	class ConfiguredEntriesProvider {
	    public ILabelledEntriesProvider provider;
	    public Map<String, String> arguments;
	    public PrefixDef prefixDef;
	    public ConfiguredEntriesProvider(ILabelledEntriesProvider p, Map<String, String> args, PrefixDef prefix) {
		provider = p;
		arguments = args;
		prefixDef = prefix;
	    }
	}
	List<ConfiguredEntriesProvider> configuredEntriesProviders = new ArrayList<ConfiguredEntriesProvider>();

	// get plugins configured for current editing context
	ExtensionConfiguration config;
	AuthorDocumentController document = authorAccess.getDocumentController();
	System.err.println("plugin configurations: " + extensionsConfiguration.size());
	// iterate over extension (plugin) configurations from config file
	for (i = 0; i < extensionsConfiguration.size(); i++) {
	    config = extensionsConfiguration.get(i);
	    // iteratre over loaded plugins
	    for (j = 0; j < entriesProviders.size(); j++) {
		ILabelledEntriesProvider entriesProvider = (ILabelledEntriesProvider) entriesProviders.get(j);
		// check if the configuration is for this provider
		if (entriesProvider.getClass().getCanonicalName().equals(config.getClassName())) {
		    // check all specifications of this provider
		    for (k = 0; k < config.getSpecification().size(); k++) {
			ArgumentsConditionsPair spec = config.getSpecification().get(k);
			// prepare an error message we might throw multiple times
			String err = "Error running XPath configured as 'context' condition for "
			    + config.getClassName()
			    + " in config file "
			    + configFile
			    + "\n"
			    + spec.getConditions().get("context");
			// check condition defined in 'context' matches the current edition context
			try {
			    // run xpath configured as context on the current editing context
			    Object[] context = document.evaluateXPath(spec.getConditions().get("context"), false, true, true);
			    //System.err.println(context[0].toString());
			    if (context.length == 1 && context[0].toString().equals("true")) {
				// get all the prefixDef elements for this provider
				AuthorNode[] prefixDefNodes = document.findNodesByXPath(spec.getArguments().get("prefix"), false, false, false);
				for (m = 0; m < prefixDefNodes.length; m++) {
				    // parse the prefixDef element to a java type and append a configured provider
				    PrefixDef prefixDef = new PrefixDef((AuthorElement) prefixDefNodes[m]);
				    Map<String, String> arguments = spec.getArguments();

				    // FIXME: get plugin for extracting link from replacement pattern
				    arguments.put("systemID", Resolver.resolve(authorAccess, prefixDef));
				    arguments.put("prefix", prefixDef.getIdent() + ":");
				    configuredEntriesProviders.add(new ConfiguredEntriesProvider(entriesProvider, arguments, prefixDef));
				}
				if (prefixDefNodes.length == 0) {
				    System.err.println(err + "\nNo prefixDef found");
				}
			    }
			} catch (AuthorOperationException e) {
			    // we do not throw an exception here, but
			    // print an error message
			    System.err.println(err);
			} catch (IndexOutOfBoundsException e) {
			    // dito
			    System.err.println(err + "\nExpression should return a boolean value");
			} catch (NullPointerException e) {
			    throw new AuthorOperationException("Configuration error in "
							       + configFile
							       + "\n\n"
							       + e);
			} catch (TransformerException e) {
			    throw new AuthorOperationException("Error in syntax of the location given in prefixDef\n\n"
							       + err);
			} catch (MalformedURLException e) {
			    throw new AuthorOperationException("Malformed URL in the location given in prefixDef\n\n"
							       + err);
			}
		    }
		}
	    }
	}
	System.err.println("Configured plugins: " + configuredEntriesProviders.size());

	// FIXME
	//
	// the user dialogue from ediarum we currently use
	// takes two static string arrays: keys and values
	//
	// so we call the plugins here. But they should be called from
	// UI code in order to allow updates.


	List<String> keys = new ArrayList<String>();
	List<String> labels = new ArrayList<String>();
	LabelledEntry entry;
	k = 0;
	for (i = 0; i < configuredEntriesProviders.size(); i++) {
	    ConfiguredEntriesProvider configuredEntriesProvider = configuredEntriesProviders.get(i);
	    ILabelledEntriesProvider provider = configuredEntriesProvider.provider;
	    try {
		List<LabelledEntry> entries = provider.getLabelledEntries(configuredEntriesProvider.arguments);
		for (j = 0; j < entries.size(); j++) {
		    entry = entries.get(j);
		    keys.add(entry.getKey());
		    labels.add(entry.getLabel());
		    k++;
		}
	    } catch (ExtensionException e) {
		String report = "";
		for (Map.Entry<String, String> argument : configuredEntriesProvider.arguments.entrySet()) {
		    report += argument.getKey() + " = " + argument.getValue() + "\n";
		}
		throw new AuthorOperationException("Error reading entries\n\n"
						   + report + "\n\n" + e);
	    }

	    String report = "";
	    for (Map.Entry<String, String> argument : configuredEntriesProvider.arguments.entrySet()) {
		report += argument.getKey() + " = " + argument.getValue() + "\n";
	    }
	    System.err.println("Config of " + provider.getClass().getCanonicalName() + "\n" + report);
	    
	}
	String[] keysArray = new String[k];
	String[] labelsArray = new String[k];
	for (i = 0; i < k; i++) {
	    keysArray[i] = keys.get(i);
	    labelsArray[i] = labels.get(i);
	}
	System.err.println("Items: " + k);
	

	
	// // Get prefixDef elements from current document
	// AuthorNode[] prefixNodes;
	// String xpathToPrefixDef = "//*:prefixDef[matches(@ident, '" + prefix + "')]";
	// try {
	//     prefixNodes = authorAccess.getDocumentController().findNodesByXPath(xpathToPrefixDef, true, true, true);
	// }
	// catch (AuthorOperationException e) {
	//     // this may be thrown by findNodesByXPath
	//     throw new AuthorOperationException("prefixDef with @ident='" + prefix + "' not found\n\n" + e);
	// }

	// System.err.println(xpathToPrefixDef);
	// System.err.println(prefixNodes.length);

	// // we need a copy enriched by the parameters from prefixDef
	// Map<String, String> parsedArgs, enrichedArgs;
	// try {
	//     parsedArgs = ArgumentsExtractor.arguments(providerArgs);
	// } catch (Exception e) {
	//     throw new IllegalArgumentException("Invalid argument 'providerArguments'\n\n" + e);
	// }
	
	// // store the prefixDef elements into a PrefixDef array
	// int l = prefixNodes.length;
	// PrefixDef[] prefixDefs = new PrefixDef[l];
	// List<LabelledEntry> items = new ArrayList<LabelledEntry>();
	// for (i = 0; i < l; i++) {
	//     prefixDefs[i] = new PrefixDef((AuthorElement)prefixNodes[i]);
	//     System.err.println("prefixDef: "
	// 		       + prefixDefs[i].getIdent() + " "
	// 		       + prefixDefs[i].getReplacementPattern() + " "
	// 		       + prefixDefs[i].getMatchPattern() + "\n");
	//     try {
	// 	// enrich arguments with data found in prefixDef
	// 	enrichedArgs = new HashMap<String, String>(parsedArgs);
	// 	enrichedArgs.put("uri", Resolver.resolve(authorAccess, prefixDefs[i]));
	// 	enrichedArgs.put("prefix", prefixDefs[i].getIdent() + ":");
	// 	// get the provider and call getLabelledEntries() on it
	// 	items.addAll(LabelledEntries.provider(provider).getLabelledEntries(enrichedArgs));
	//     } catch (ProviderNotFoundException e) {
	// 	throw new AuthorOperationException("Plugin not found: " + provider + "\n\n" + e);
	//     } catch (ExtensionException e) {
	// 	throw new AuthorOperationException("Error while reading from URI given in "
	// 					   + prefixDefs[i].getReplacementPattern()
	// 					   + "\nCaused by plugin " + provider
	// 					   + "\n\n" + e);
	//     } catch (MalformedURLException e) {
	// 	throw new AuthorOperationException("Malformed URL " + e);
	//     } catch (TransformerException e) {
	// 	throw new AuthorOperationException("Error reading referenced file\n\n" + e);
	//     }//  catch (URISyntaxException e) {
	//     // 	throw new AuthorOperationException("Error reading referenced file\n\n" + e);
	//     // }
	// }

	// int total = items.size();

	// // get keys and labels into separate string arrays, which are
	// // needed for the dialog.
	// Iterator<LabelledEntry> iter = items.iterator();
	// String[] keys = new String[total];
	// String[] labels = new String[total];
	// j = 0;
	// LabelledEntry entry;
	// while (iter.hasNext()) {
	//     entry = iter.next();
	//     keys[j] = entry.getKey();
	//     labels[j] = entry.getLabel();
	//     j++;
	// }

	// Ask the user for selection
	InsertRegisterDialog dialog =
	    new InsertRegisterDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(),
				     labelsArray,
				     keysArray,
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

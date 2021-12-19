/**
 * {@link AbstractPrefixURIOperation} - is an abstract base class for
 * building selection dialogs with options from registered
 * {@link ILabelledEntriesProvider} and from the values defined in a
 * target given in a <prefixDef> element.
 *
 */
package de.wwu.scdh.oxbytei;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerException;

import org.xml.sax.EntityResolver;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntries;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.tei.PrefixDef;
import de.wwu.scdh.teilsp.config.ArgumentsConditionsPair;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.oxbytei.commons.Resolver;


public abstract class AbstractPrefixURIOperation {

    /**
     * Get the plugins registered for {@link ILabelledEntriesProvider}
     * and configure them based on config file and <prefixDef>
     * elements in the currently edited file.
     */
    public static List<ILabelledEntriesProvider> getConfiguredProviders(AuthorAccess authorAccess)
	throws AuthorOperationException {

	// Load providers
	List<ILabelledEntriesProvider> entriesProviders = LabelledEntries.providers();

	// get the uri resolver, entity resolver used by oxygen and editing context
	URIResolver resolver = authorAccess.getXMLUtilAccess().getURIResolver();
	EntityResolver entityResolver = authorAccess.getXMLUtilAccess().getEntityResolver();
	URL currentFileURL = authorAccess.getEditorAccess().getEditorLocation();

	// get the URL of the configuration file
	String defaultConfigFile =
	    authorAccess.getUtilAccess().expandEditorVariables(OxbyteiConstants.DEFAULT_CONFIG_FILE, null);
	String configFile = defaultConfigFile;
	try {
	    // use resolver with xml catalogs
	    configFile = resolver.resolve(defaultConfigFile, null).getSystemId();
	} catch (TransformerException e) {}

	//System.err.println("loading config from " + configFile);

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

	// get plugins configured for current editing context
	ExtensionConfiguration config;
	AuthorDocumentController document = authorAccess.getDocumentController();
	List<ILabelledEntriesProvider> configuredEntriesProviders = new ArrayList<ILabelledEntriesProvider>();
	//System.err.println("plugin configurations: " + extensionsConfiguration.size());
	// iterate over extension (plugin) configurations from config file
	for (i = 0; i < extensionsConfiguration.size(); i++) {
	    config = extensionsConfiguration.get(i);
	    // iteratre over loaded plugins
	    for (j = 0; j < entriesProviders.size(); j++) {
		ILabelledEntriesProvider entriesProvider = entriesProviders.get(j);
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
				    //System.err.println("prefixDef@ident " + prefixDef.getIdent());
				    // we need a new instance of the map, because we set some values of it
				    Map<String, String> arguments = new HashMap<String, String>(spec.getArguments());
				    // TODO: get plugin for extracting link from replacement pattern
				    arguments.put("systemID", Resolver.resolve(authorAccess, prefixDef));
				    arguments.put("prefix", prefixDef.getIdent() + ":");
				    // we make a new instance of the
				    // provider, because we do not
				    // want to configure the same
				    // several times.
				    ILabelledEntriesProvider p = entriesProvider.getClass().newInstance();
				    p.init(arguments, resolver, entityResolver, currentFileURL);
				    configuredEntriesProviders.add(p);
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
			} catch (InstantiationException e) {
			    throw new AuthorOperationException("Error loading plugin "
							       + entriesProvider.getClass().getCanonicalName()
							       + "\n\n"
							       + e);
			} catch (IllegalAccessException e) {
			    throw new AuthorOperationException("Error loading plugin "
							       + entriesProvider.getClass().getCanonicalName()
							       + "\n\n"
							       + e);
			} catch (ExtensionException e) {
			    throw new AuthorOperationException("Error initializing plugin "
							       + entriesProvider.getClass().getCanonicalName()
							       + "\n\n"
							       + e);
			}
		    }
		}
	    }
	}
	// System.err.println("Configured plugins: " + configuredEntriesProviders.size());
	// for (ILabelledEntriesProvider p : configuredEntriesProviders) {
	//     System.err.println(p.toString() + p.getArguments().toString() + p.getArguments().get("prefix"));
	// }
	return configuredEntriesProviders;
    }

}
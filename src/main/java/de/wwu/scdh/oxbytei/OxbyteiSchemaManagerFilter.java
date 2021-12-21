package de.wwu.scdh.oxbytei;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerException;

import org.xml.sax.EntityResolver;

import ro.sync.contentcompletion.xml.SchemaManagerFilter;
import ro.sync.contentcompletion.xml.CIAttribute;
import ro.sync.contentcompletion.xml.CIElement;
import ro.sync.contentcompletion.xml.CIValue;
import ro.sync.contentcompletion.xml.Context;
import ro.sync.contentcompletion.xml.WhatAttributesCanGoHereContext;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.PluginWorkspace;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntries;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.tei.PrefixDef;
import de.wwu.scdh.teilsp.config.ArgumentsConditionsPair;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.oxbytei.commons.Resolver;


public class OxbyteiSchemaManagerFilter
    implements SchemaManagerFilter {

    public String getDescription() {
	return "oXbytei content completer";
    }

    private static final int INFO = 0;
    private static final int DEBUG = 1;
    private static final int ERROR = 2;
    
    private void log(String msg, int level) {
	if (level > INFO) {
	    System.err.println("Log level " + level + "\n" + msg);
	}
    }

    private static final String[] namespaces = null;
    // {"xml", "http://www.w3.org/XML/1998/namespace",
    //  "tei", "http://www.tei-c.org/ns/1.0",
    //  "", "http://www.tei-c.org/ns/1.0"};
    
    @Override
    public List<CIValue> filterAttributeValues(List<CIValue> list, WhatPossibleValuesHasAttributeContext context) {

	// get some relevant context information
	String attributeName = context.getAttributeName();
	String alreadyTyped = context.getCurrentValuePrefix();

	// computeContextXPathExpression() is present according to compiler
	String contextXPath = context.computeContextXPathExpression();

	// Load providers
	List<ILabelledEntriesProvider> entriesProviders = LabelledEntries.providers();

	// get resolvers and url of the current editing context
	URIResolver resolver =
	    PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().getURIResolver();
	EntityResolver entityResolver =
	    PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().getEntityResolver();
	URL currentFileURL =
	    PluginWorkspaceProvider.getPluginWorkspace().getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA).getEditorLocation();

	// get the URL of the configuration file
	String configFile = OxbyteiConstants.getConfigFile();

	// read the plugin configuration from the config file
	List<ExtensionConfiguration> extensionsConfiguration = new ArrayList<ExtensionConfiguration>();
	try {
	    extensionsConfiguration = ExtensionConfigurationReader.getExtensionsConfiguration(configFile);
	} catch (ConfigurationException e) {
	    log("Error reading config from '"
		+ configFile +
		"'\n\nDetails:\n"
		+ e,
		ERROR);
	}

	// we need some iteration variables
	int i, j, k, m;

	// get plugins configured for current editing context
	ExtensionConfiguration config;
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
			log("NO error, yust info: " + err, INFO);
			// check condition defined in 'context' matches the current edition context
			try {
			    // run xpath configured as context on the current editing context
			    // note: we put the context form conditions into an xpath attribute!
			    String xpath = contextXPath + "[" + spec.getConditions().get("context") + "]";
			    log(xpath, INFO);
			    List<Object> contxt = context.executeXPath(xpath, namespaces, false);
			    log("This plugin matches: " + contxt.size(), INFO);
			    if (contxt.size() > 0
				//&& contxt.get(0).equals("true")
				&& attributeName.equals(spec.getConditions().get("attribute"))) {
				// get all the prefixDef elements for this provider
				xpath = spec.getConditions().get("prefix") + "/@ident";
				List<String> ident = context.executeXPath(xpath, namespaces);
				xpath = spec.getConditions().get("prefix") + "/@replacementPattern";
				List<String> rp = context.executeXPath(xpath, namespaces);
				xpath = spec.getConditions().get("prefix") + "/@matchPattern";
				List<String> mp = context.executeXPath(xpath, namespaces);
				int prefixCount = Integer.min(ident.size(), Integer.min(rp.size(), mp.size()));
				for (m = 0; m < prefixCount; m++) {
				    // parse the prefixDef element to a java type and append a configured provider
				    PrefixDef prefixDef = new PrefixDef(mp.get(m), rp.get(m), ident.get(m));
				    log("prefixDef@ident " + prefixDef.getIdent(), INFO);
				    // we need a new instance of the map, because we set some values of it
				    Map<String, String> arguments = new HashMap<String, String>(spec.getArguments());
				    // TODO: get plugin for extracting link from replacement pattern
				    arguments.put("systemID", Resolver.resolve(resolver, currentFileURL.toString(), prefixDef));
				    arguments.put("prefix", prefixDef.getIdent() + ":");
				    // we make a new instance of the
				    // provider, because we do not
				    // want to configure the same
				    // several times.
				    ILabelledEntriesProvider p = entriesProvider.getClass().newInstance();
				    p.init(arguments, resolver, entityResolver, currentFileURL);
				    configuredEntriesProviders.add(p);
				}
				if (prefixCount == 0) {
				    log(err + "\nNo prefixDef found", DEBUG);
				}
			    }
			} catch (IndexOutOfBoundsException e) {
			    log(err + "\nExpression should return a boolean value", DEBUG);
			} catch (NullPointerException e) {
			    log("Configuration error in " + configFile + "\n\n" + e, DEBUG);
			} catch (TransformerException e) {
			    log("Error in syntax of the location given in prefixDef\n\n"
				+ err, DEBUG);
			} catch (MalformedURLException e) {
			    log("Malformed URL in the location given in prefixDef\n\n"
				+ err, DEBUG);
			} catch (InstantiationException e) {
			    log("Error loading plugin "
				+ entriesProvider.getClass().getCanonicalName()
				+ "\n\n" + e, DEBUG);
			} catch (IllegalAccessException e) {
			    log("Error loading plugin "
				+ entriesProvider.getClass().getCanonicalName()
				+ "\n\n" + e, DEBUG);
			} catch (ExtensionException e) {
			    log("Error initializing plugin "
				+ entriesProvider.getClass().getCanonicalName()
				+ "\n\n" + e, DEBUG);
			}
		    }
		}
	    }
	}
	log("Configured plugins: " + configuredEntriesProviders.size(), INFO);
	for (ILabelledEntriesProvider p : configuredEntriesProviders) {
	    log(p.toString() + p.getArguments().toString() + p.getArguments().get("prefix"), INFO);
	}

	List<CIValue> suggestions = new ArrayList<CIValue>();
	for (ILabelledEntriesProvider provider : configuredEntriesProviders) {
	    try {
		List<LabelledEntry> entries = provider.getLabelledEntries(alreadyTyped);
		for (LabelledEntry entry : entries) {
		    suggestions.add(new CIValue(entry.getKey(), entry.getLabel()));
		}
	    } catch (ExtensionException e) {
		log("Error getting values from " + provider.getClass().getCanonicalName()
		    + "\n" + e, ERROR); 
	    }
	}
	suggestions.addAll(list);

	// // Dummy implementation
	// suggestions.add(new CIValue("Hello", "There is a description.\n\n"
	// 			    + contextXPath
	// 			    + "\n\nYou typed: "
	// 			    + alreadyTyped));

	return suggestions;
    }

    @Override
    public List<CIElement> filterElements(final List<CIElement> list, final WhatElementsCanGoHereContext context) {
        return list;
    }

    @Override
    public List<CIAttribute> filterAttributes(final List<CIAttribute> list, final WhatAttributesCanGoHereContext context) {
        return list;
    }

    @Override
    public List<CIValue> filterElementValues(final List<CIValue> list, final Context context) {
        return list;
    }
    
}

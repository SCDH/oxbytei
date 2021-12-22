package de.wwu.scdh.oxbytei;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import de.wwu.scdh.teilsp.services.extensions.LabelledEntriesLoader;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.config.ArgumentsConditionsPair;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;


public class OxbyteiSchemaManagerFilter
    implements SchemaManagerFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OxbyteiSchemaManagerFilter.class);

    public String getDescription() {
	return "oXbytei content completer";
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
	List<ILabelledEntriesProvider> entriesProviders = LabelledEntriesLoader.providers();

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
	    LOGGER.error("Error reading config from '{}'\nDetails:\n{}", configFile, e);
	}

	// we need some iteration variables
	int i, j, k;

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
			LOGGER.debug("Running XPath configured as 'context' condition for {} in config file {}:\n{}",
				     config.getClassName(),
				     configFile,
				     spec.getConditions().get("context"));
			// check condition defined in 'context' matches the current edition context
			try {
			    // run xpath configured as context on the current editing context
			    // note: we put the context form conditions into an xpath attribute!
			    String xpath = contextXPath + "[" + spec.getConditions().get("context") + "]";
			    LOGGER.debug(xpath);
			    List<Object> contxt = context.executeXPath(xpath, namespaces, false);
			    LOGGER.debug("This plugin matches: {}", contxt.size());
			    if (// matching context?
				contxt.size() > 0
				// configured for attribute?
				&& spec.getConditions().get("nodeType").equals("attribute")
				// matching attribute name?
				&& spec.getConditions().get("nodeName").equals(attributeName)
				) {
				// we make a new instance of the map, because we set some values of it
				Map<String, String> arguments = new HashMap<String, String>(spec.getArguments());
				// we make a new instance of the
				// provider, because we do not want to
				// configure the same several times.
				ILabelledEntriesProvider p = entriesProvider.getClass().newInstance();
				p.init(arguments, resolver, entityResolver, currentFileURL);
				configuredEntriesProviders.add(p);
			    }
			} catch (NullPointerException e) {
			    LOGGER.error("Configuration error in {}\n{}", configFile, e);
			} catch (InstantiationException e) {
			    LOGGER.error("Error loading plugin {}\n{}",
					 entriesProvider.getClass().getCanonicalName(),
					 e);
			} catch (IllegalAccessException e) {
			    LOGGER.error("Error loading plugin {}\n",
					 entriesProvider.getClass().getCanonicalName(),
					 e);
			} catch (ExtensionException e) {
				LOGGER.error("Error initializing plugin {} \nusing config file {}:\n",
					     entriesProvider.getClass().getCanonicalName(),
					     configFile, e);
			}
		    }
		}
	    }
	}
	LOGGER.debug("Configured plugins: {}", configuredEntriesProviders.size());
	for (ILabelledEntriesProvider p : configuredEntriesProviders) {
	    LOGGER.debug("Plugin {} configured with arguments:\n{}",
			 p.toString(),
			 p.getArguments().toString());
	}

	List<CIValue> suggestions = new ArrayList<CIValue>();
	for (ILabelledEntriesProvider provider : configuredEntriesProviders) {
	    try {
		List<LabelledEntry> entries = provider.getLabelledEntries(alreadyTyped);
		for (LabelledEntry entry : entries) {
		    suggestions.add(new CIValue(entry.getKey(), entry.getLabel()));
		}
	    } catch (ExtensionException e) {
		LOGGER.error("Error getting values from plugin {}:\n{}",
			     provider.getClass().getCanonicalName(), e);
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

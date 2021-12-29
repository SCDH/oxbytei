package de.wwu.scdh.teilsp.services.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.URIResolver;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;

import net.sf.saxon.xpath.XPathFactoryImpl;

import de.wwu.scdh.teilsp.config.ArgumentsConditionsPair;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.exceptions.ProviderNotFoundException;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;



/**
 * {@link LabelledEntriesLoader} - a class for loading plugins
 * registered for {@link ILabelledEntriesProvider} through the service
 * provider interface (SPI).
 */
public class LabelledEntriesLoader {

    public static final String ELEMENT_NODE = "element";
    public static final String ATTRIBUTE_NODE = "attribute";
    public static final String TEXT_NODE = "text";
    public static final String ATTRIBUTE_VALUE = "attribute"; // FIXME

    private static final String DEFAULT_PROVIDER = "de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXML";

    /**
     * Returns all providers in the classpath.
     *
     */
    public static List<ILabelledEntriesProvider> providers() {
	List<ILabelledEntriesProvider> services = new ArrayList<>();
	ServiceLoader<ILabelledEntriesProvider> loader = ServiceLoader.load(ILabelledEntriesProvider.class);
	loader.forEach(labelledEntriesProvider -> {
		services.add(labelledEntriesProvider);
	});
	return services;
    }

    /**
     * Get provider by name.
     *
     */
    public static ILabelledEntriesProvider provider(String providerName)
	throws ProviderNotFoundException {
	ServiceLoader<ILabelledEntriesProvider> loader = ServiceLoader.load(ILabelledEntriesProvider.class);
	Iterator<ILabelledEntriesProvider> iter = loader .iterator();
	while (iter.hasNext()) {
	    ILabelledEntriesProvider provider = iter.next();
	    if (providerName.equals(provider.getClass().getName())) {
		return provider;
	    }
	}
	throw new ProviderNotFoundException("ILabelledEntriesProvider " + providerName + " not found");
    }
    
    /**
     * Overloaded method, that returns the default provider.
     *
     */
    public static ILabelledEntriesProvider provider()
	throws ProviderNotFoundException {
	return provider(DEFAULT_PROVIDER);
    }

    /**
     * This reads the configuration file and returns a list of
     * initialized providers, defined for the current context.
     * @param document the current document
     * @param context the current cursor position in the document
     * given by an XPath expression
     * @param nodeType the type of node which to offer completion suggestions
     * @param nodeName the name of the element or attribute node, which to offer suggestions for
     * @param uriResolver a {@link URIResolver} to be used by the plugins
     * @param entityResolver a {@link EntityResolver} to be used by the plugins
     * @param namespaceDecl the {@link NamespaceContext} of the current document
     * @param configFile an URL pointing to the config file, use
     * {@code file:...} for local files
     */
    public static List<ILabelledEntriesProvider> providersForContext
	(Document document,
	 String systemId,
	 String context,
	 String nodeType,
	 String nodeName,
	 URIResolver uriResolver,
	 EntityResolver entityResolver,
	 NamespaceContext namespaceDecl,
	 String configFile)
	throws ExtensionException, ConfigurationException {
	List<ILabelledEntriesProvider> providers = new ArrayList<ILabelledEntriesProvider>();

	// setup logging utility
	final Logger LOGGER = LoggerFactory.getLogger(LabelledEntriesLoader.class);

	LOGGER.debug("Searching plugins for context: \"{}\"", context);
	LOGGER.debug("Searching pluging for nodeType: {}, nodeName: {}", nodeType, nodeName);

	// Load plugins
	List<ILabelledEntriesProvider> plugins = LabelledEntriesLoader.providers();

	// read the plugin configuration from the config file
	List<ExtensionConfiguration> extensionsConfiguration = ExtensionConfigurationReader.getExtensionsConfiguration(configFile);

	// we need some iteration variables
	int i, j, k;


	// setup XPath 2.0 from Saxon
	//XPathFactoryImpl xpathFactoryImpl = new XPathFactoryImpl();
	//xpathFactoryImpl.setConfiguration((Configuration) document.getDomConfig());
	XPath xpath = new XPathFactoryImpl().newXPath();
	xpath.setNamespaceContext(namespaceDecl);

	// just for debugging
	LOGGER.debug("Implementation of document: {}", document.getClass().getCanonicalName());
	try {
	    // can we evaluate an XPath expression?
	    NodeList nodes = (NodeList) xpath.evaluate("//*", document, XPathConstants.NODESET);
	    LOGGER.debug("Document contains {} element nodes.", nodes.getLength());
	} catch (XPathExpressionException e) {
	    throw new ConfigurationException("Failed to count element nodes\n" + e);
	}

	// get plugins configured for current editing context
	ExtensionConfiguration config;
	//System.err.println("plugin configurations: " + extensionsConfiguration.size());
	// iterate over extension (plugin) configurations from config file
	for (i = 0; i < extensionsConfiguration.size(); i++) {
	    config = extensionsConfiguration.get(i);
	    // iteratre over loaded plugins
	    for (j = 0; j < plugins.size(); j++) {
		ILabelledEntriesProvider plugin = plugins.get(j);
		// check if the configuration is for this provider
		if (plugin.getClass().getCanonicalName().equals(config.getClassName())) {
		    // check all specifications of this provider
		    for (k = 0; k < config.getSpecification().size(); k++) {
			ArgumentsConditionsPair spec = config.getSpecification().get(k);
			String contextXPath = context + "[" + spec.getConditions().get("context") + "]";
			LOGGER.debug("Running XPath configured as 'context' condition for {} in config file {}:\n{}\nResulting XPath: \"{}\"",
				     config.getClassName(),
				     configFile,
				     spec.getConditions().get("context"),
				     contextXPath);
			// check condition defined in 'context' matches the current edition context
			try {
			    // run xpath configured as context on the current editing context
			    // note: we put the context form conditions into an xpath attribute!
			    LOGGER.debug(contextXPath);
			    NodeList contextNodes = (NodeList) xpath.evaluate(contextXPath, document, XPathConstants.NODESET);
			    // context.executeXPath(xpath, namespaces, false);
			    LOGGER.debug("This plugin matches: {}", contextNodes.getLength());
			    if (// matching context?
				contextNodes.getLength() > 0
				// configured for attribute?
				&& spec.getConditions().get("nodeType").equals(nodeType)
				// matching attribute name?
				&& spec.getConditions().get("nodeName").equals(nodeName)
				) {
				// we make a new instance of the map, because we might set some values of it
				Map<String, String> arguments = new HashMap<String, String>(spec.getArguments());
				// we make a new instance of the
				// provider, because we do not want to
				// configure the same several times.
				ILabelledEntriesProvider p = plugin.getClass().newInstance();
				p.init(arguments, uriResolver, entityResolver, document, systemId);
				providers.add(p);
			    }
			} catch (XPathExpressionException e) {
			    throw new ConfigurationException("Error running XPath "
							     + spec.getConditions().get("context")
							     + " configured for "
							     + plugin.getClass().getCanonicalName()
							     + "\nResulting XPath expression:\n"
							     + contextXPath);
			} catch (NullPointerException e) {
			    throw new ConfigurationException("Configuration error in " + configFile + "\n" + e);
			} catch (InstantiationException e) {
			    throw new ConfigurationException("Error loading plugin "
							     + plugin.getClass().getCanonicalName()
							     + "\n" + e);
			} catch (IllegalAccessException e) {
			    throw new ConfigurationException("Error loading plugin "
							     + plugin.getClass().getCanonicalName()
							     + "\n" + e);
			} catch (ExtensionException e) {
			    throw new ConfigurationException("Error initializing plugin "
							     + plugin.getClass().getCanonicalName()
							     + " using config file "
							     + configFile
							     + ":\n" + e);
			}
		    }
		}
	    }
	}
	LOGGER.debug("Configured plugins: {}", providers.size());
	for (ILabelledEntriesProvider p : providers) {
	    LOGGER.debug("Plugin {} configured with arguments:\n{}",
			 p.toString(),
			 p.getArguments().toString());
	}
	return providers;
    }

}

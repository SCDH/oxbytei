package de.wwu.scdh.teilsp.services.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.wwu.scdh.teilsp.config.ArgumentsConditionsPair;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.config.EditorVariablesExpander;
import de.wwu.scdh.teilsp.exceptions.ProviderNotFoundException;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.xpath.XPathUtil;


/**
 * {@link ConfiguredPluginLoader} - a class for loading plugins
 * registered for an interface extending {@link ConfigurablePlugin}
 * through the service provider interface (SPI).
 */
public class ConfiguredPluginLoader <T extends ConfigurablePlugin> {

    private final Logger LOGGER = LoggerFactory.getLogger(ConfiguredPluginLoader.class);

    protected final String defaultProvider;

    protected final Class<T> pluginInterface;

    /**
     * Make a new loader for configured plugins implementing an
     * interface.
     *
     * @param pluginInterface  the interface for which plugins shall be loaded
     * @param defaultPlugin  the dotted name of the default plugin class
     *
     */
    public ConfiguredPluginLoader(Class<T> pluginInterface, String defaultPlugin) {
	this.pluginInterface = pluginInterface;
	this.defaultProvider = defaultPlugin;
    }

    /**
     * Returns all plugins present in the classpath.
     *
     */
    public List<T> providers() {
	List<T> services = new ArrayList<T>();
	ServiceLoader<T> loader = ServiceLoader.load(pluginInterface);
	loader.forEach(labelledEntriesProvider -> {
		services.add(labelledEntriesProvider);
	});
	return services;
    }

    /**
     * Get provider by name.
     *
     */
    public T provider(String providerName)
	throws ProviderNotFoundException {
	ServiceLoader<T> loader = ServiceLoader.load(pluginInterface);
	Iterator<T> iter = loader .iterator();
	while (iter.hasNext()) {
	    T provider = iter.next();
	    if (providerName.equals(provider.getClass().getName())) {
		return provider;
	    }
	}
	throw new ProviderNotFoundException("ConfigurablePlugin " + pluginInterface.toString()
					    + " " + providerName + " not found");
    }

    /**
     * Overloaded method, that returns the default provider.
     *
     */
    public T provider()
	throws ProviderNotFoundException {
	return provider(defaultProvider);
    }

    /**
     * This reads the configuration file and returns a list of
     * initialized plugins, defined for the current context.
     * @param document the current document
     * @param context the current cursor position in the document
     * given by an XPath expression
     * @param nodeType the type of node which to offer completion suggestions
     * @param nodeName the name of the element or attribute node, which to offer suggestions for
     * @param namespaceDecl the {@link NamespaceContext} of the current document
     * @param configFile an URL pointing to the config file, use
     * {@code file:...} for local files
     * @param expander an {@link EditorVariablesExpander}
     */
    public List<T> providersForContext
	(Document document,
	 String context,
	 String nodeType,
	 String nodeName,
	 NamespaceContext namespaceDecl,
	 String configFile,
	 EditorVariablesExpander expander)
	throws ExtensionException, ConfigurationException {
	// read the plugin configuration from the config file
	List<ExtensionConfiguration> extensionsConfiguration =
	    ExtensionConfigurationReader.getExtensionsConfiguration(configFile, expander);
	return providersForContext(document, context, nodeType, nodeName,
				   namespaceDecl, extensionsConfiguration);
    }

    /**
     * This returns a list of initialized providers, defined for the
     * current context.
     * @param document the current document
     * @param context the current cursor position in the document
     * given by an XPath expression
     * @param nodeType the type of node which to offer completion suggestions
     * @param nodeName the name of the element or attribute node, which to offer suggestions for
     * @param namespaceDecl the {@link NamespaceContext} of the current document
     * @param extensionsConfiguration the parsed config file
     * {@code file:...} for local files
     */
    public List<T> providersForContext
	(Document document,
	 String context,
	 String nodeType,
	 String nodeName,
	 NamespaceContext namespaceDecl,
	 List<ExtensionConfiguration> extensionsConfiguration)
	throws ExtensionException, ConfigurationException {
	List<T> providers = new ArrayList<T>();

	// setup logging utility

	LOGGER.debug("Searching plugins for context: \"{}\"", context);
	LOGGER.debug("Searching pluging for nodeType: {}, nodeName: {}", nodeType, nodeName);
	LOGGER.debug("Implementation of document: {}", document.getClass().getCanonicalName());

	// Load plugins
	List<T> plugins = providers();

	// we need some iteration variables
	int i, j, k;

	// prepare the XPath query, using Saxon here for XPath 2.0
	XPath xpath = XPathUtil.makeXPath(document);
	xpath.setNamespaceContext(namespaceDecl);

	// get plugins configured for current editing context
	ExtensionConfiguration config;
	//System.err.println("plugin configurations: " + extensionsConfiguration.size());
	// iterate over extension (plugin) configurations from config file
	for (i = 0; i < extensionsConfiguration.size(); i++) {
	    config = extensionsConfiguration.get(i);
	    // iteratre over loaded plugins
	    for (j = 0; j < plugins.size(); j++) {
		T plugin = plugins.get(j);
		// check if the configuration is for this provider
		if (plugin.getClass().getCanonicalName().equals(config.getClassName())) {
		    // check all specifications of this provider
		    for (k = 0; k < config.getSpecification().size(); k++) {
			ArgumentsConditionsPair spec = config.getSpecification().get(k);
			String contextXPath = context + "[" + spec.getConditions().get("context") + "]";
			LOGGER.debug("Running XPath configured as 'context' condition for {}:\n{}\nResulting XPath: \"{}\"",
				     config.getClassName(),
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
				T p = (T) plugin.getClass().newInstance();
				p.init(arguments);
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
			    throw new ConfigurationException("Configuration error\n" + e);
			} catch (InstantiationException e) {
			    throw new ConfigurationException("Error loading plugin "
							     + plugin.getClass().getCanonicalName()
							     + "\n" + e);
			} catch (IllegalAccessException e) {
			    throw new ConfigurationException("Error loading plugin "
							     + plugin.getClass().getCanonicalName()
							     + "\n" + e);
			}
		    }
		}
	    }
	}
	LOGGER.debug("Configured plugins: {}", providers.size());
	for (ConfigurablePlugin p : providers) {
	    LOGGER.debug("Plugin {} configured with arguments:\n{}",
			 p.toString(),
			 p.getArguments().toString());
	}
	return providers;
    }

}

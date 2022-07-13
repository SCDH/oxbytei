package de.wwu.scdh.teilsp.services.extensions;

import java.awt.Frame;
import java.lang.reflect.InvocationTargetException;
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

import de.wwu.scdh.teilsp.ui.ISelectionDialog;
import de.wwu.scdh.teilsp.config.ArgumentsConditionsPair;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.config.EditorVariablesExpander;
import de.wwu.scdh.teilsp.exceptions.ProviderNotFoundException;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.xpath.XPathUtil;


/**
 * {@link SelectionDialogLoader} - a class for loading plugins
 * registered for {@link ISelectionDialog} through the service
 * provider interface (SPI).
 */
public class SelectionDialogLoader {

    public static final String DEFAULT_PROVIDER = "de.wwu.scdh.teilsp.ui.EditableComboBoxSelectDialog";

    /**
     * Returns all providers in the classpath.
     *
     */
    public static List<ISelectionDialog> providers() {
	List<ISelectionDialog> services = new ArrayList<>();
	ServiceLoader<ISelectionDialog> loader = ServiceLoader.load(ISelectionDialog.class);
	loader.forEach(selectionDialog -> {
		services.add(selectionDialog);
	});
	return services;
    }

    /**
     * Get provider by name.
     *
     */
    public static ISelectionDialog provider(String providerName)
	throws ProviderNotFoundException {
	ServiceLoader<ISelectionDialog> loader = ServiceLoader.load(ISelectionDialog.class);
	Iterator<ISelectionDialog> iter = loader .iterator();
	while (iter.hasNext()) {
	    ISelectionDialog provider = iter.next();
	    if (providerName.equals(provider.getClass().getName())) {
		return provider;
	    }
	}
	throw new ProviderNotFoundException("ISelectionDialog " + providerName + " not found");
    }

    /**
     * Overloaded method, that returns the default provider.
     *
     */
    public static ISelectionDialog provider()
	throws ProviderNotFoundException {
	return provider(DEFAULT_PROVIDER);
    }

    /**
     * This reads the configuration file and returns a list of
     * initialized providers, defined for the current context.
     * @param document the current document
     * @param systemId the url string pointing to the current document
     * @param context the current cursor position in the document
     * given by an XPath expression
     * @param nodeType the type of node which to offer completion suggestions
     * @param nodeName the name of the element or attribute node, which to offer suggestions for
     * @param uriResolver a {@link URIResolver} to be used by the plugins
     * @param entityResolver a {@link EntityResolver} to be used by the plugins
     * @param namespaceDecl the {@link NamespaceContext} of the current document
     * @param configFile an URL pointing to the config file, use
     * {@code file:...} for local files
     * @param frame an Swing Frame object
     * @param expander an {@link EditorVariablesExpander}
     */
    public static List<ISelectionDialog> providersForContext
	(Document document,
	 String systemId,
	 String context,
	 String nodeType,
	 String nodeName,
	 URIResolver uriResolver,
	 EntityResolver entityResolver,
	 NamespaceContext namespaceDecl,
	 String configFile,
	 Frame frame,
	 EditorVariablesExpander expander)
	throws ExtensionException, ConfigurationException {
	// read the plugin configuration from the config file
	List<ExtensionConfiguration> extensionsConfiguration = ExtensionConfigurationReader.getExtensionsConfiguration(configFile, expander);
	return providersForContext(document, systemId, context, nodeType, nodeName,
				   uriResolver, entityResolver,
				   namespaceDecl, frame, extensionsConfiguration);
    }

    /**
     * This returns a list of initialized providers, defined for the
     * current context.
     * @param document the current document
     * @param systemId the url string pointing to the current document
     * @param context the current cursor position in the document
     * given by an XPath expression
     * @param nodeType the type of node which to offer completion suggestions
     * @param nodeName the name of the element or attribute node, which to offer suggestions for
     * @param uriResolver a {@link URIResolver} to be used by the plugins
     * @param entityResolver a {@link EntityResolver} to be used by the plugins
     * @param namespaceDecl the {@link NamespaceContext} of the current document
     * @param frame an Swing Frame object
     * @param extensionsConfiguration the parsed config file
     * {@code file:...} for local files
     */
    public static List<ISelectionDialog> providersForContext
	(Document document,
	 String systemId,
	 String context,
	 String nodeType,
	 String nodeName,
	 URIResolver uriResolver,
	 EntityResolver entityResolver,
	 NamespaceContext namespaceDecl,
	 Frame frame,
	 List<ExtensionConfiguration> extensionsConfiguration)
	throws ExtensionException, ConfigurationException {

	// setup logging utility
	final Logger LOGGER = LoggerFactory.getLogger(SelectionDialogLoader.class);

	LOGGER.debug("Searching plugins for context: \"{}\"", context);
	LOGGER.debug("Searching pluging for nodeType: {}, nodeName: {}", nodeType, nodeName);
	LOGGER.debug("Implementation of document: {}", document.getClass().getCanonicalName());

	// empty list, to be filled and returned
	List<ISelectionDialog> providers = new ArrayList<ISelectionDialog>();

	// Load plugins
	// this is not providers, because they are from no-argument constructor
	List<ISelectionDialog> plugins = SelectionDialogLoader.providers();

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
		ISelectionDialog plugin = plugins.get(j);
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
				//ISelectionDialog p = plugin.getClass().newInstance();
				// this way we can also call an arbitrary constructor
				ISelectionDialog p =
				    (ISelectionDialog) plugin.getClass().getDeclaredConstructor(Frame.class).newInstance(frame);
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
			} catch (NoSuchMethodException e) {
			    throw new ConfigurationException("Error loading dialog class "
							     + plugin.getClass()
							     + "\n" + "no constructor signature (java.awt.Frame frame)"
							     + "\n" + e);
			} catch (InvocationTargetException e) {
			    throw new ConfigurationException("Error loading dialog class "
							     + plugin.getClass()
							     + "\n" + "failed constructor call"
							     + "\n" + e);
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
	// TODO: should we dispose all plugins or does GC the job?

	LOGGER.debug("Configured plugins: {}", providers.size());
	for (ISelectionDialog p : providers) {
	    LOGGER.debug("Plugin {} configured with arguments:\n{}",
			 p.toString());
	}
	return providers;
    }

}

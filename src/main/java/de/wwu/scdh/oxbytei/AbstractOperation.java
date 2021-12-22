/**
 * {@link AbstractOperation} is an abstract base class for building
 * author mode operations.
 *
 * It offers a methods for presenting the user a selection dialog with
 * options from {@link ILabelledEntriesProvider} plugins.  And methods
 * to setup such provider plugins from the values defined in a target
 * given in a <prefixDef> element.
 *
 */
package de.wwu.scdh.oxbytei;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.xml.transform.URIResolver;

import org.w3c.dom.Attr;
import org.xml.sax.EntityResolver;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AttrValue;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntriesLoader;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.config.ArgumentsConditionsPair;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.oxbytei.commons.ISelectionDialog;


abstract class AbstractOperation {

    /**
     * Attribute name to be set by {@link setAttribute}
     */
    protected String attributeName;

    /**
     * Whether or not the selection dialog allows multiple selects.
     */
    protected boolean multiple;

    /**
     * Title message presented to the user in the selection dialog.
     */
    protected String message;

    /**
     * Canonical class name of the selection dialog.
     */
    protected String dialog;

    /**
     * {@link AuthorAccess} passed in from the author mode operation.
     */
    protected AuthorAccess authorAccess;

    /**
     * The node, which to operate on, i.e. which to set the attribute on.
     */
    protected AuthorNode locationNode;

    /**
     * Plugins setup by {@link setupProvidersFromPrefixDef()} and
     * used by {@link setAttribute()}.
     */
    private List<ILabelledEntriesProvider> providers;
    
    /**
     * Get the plugins registered for {@link ILabelledEntriesProvider}
     * and configure them based on config file and <prefixDef>
     * elements in the currently edited file.
     */
    protected void setupLabelledEntriesProviders()
	throws AuthorOperationException {

	// Load providers
	List<ILabelledEntriesProvider> entriesProviders = LabelledEntriesLoader.providers();

	// get the uri resolver, entity resolver used by oxygen and editing context
	URIResolver resolver = authorAccess.getXMLUtilAccess().getURIResolver();
	EntityResolver entityResolver = authorAccess.getXMLUtilAccess().getEntityResolver();
	URL currentFileURL = authorAccess.getEditorAccess().getEditorLocation();

	// get the URL of the configuration file
	String configFile = OxbyteiConstants.getConfigFile();

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
	int i, j, k;

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
				// we need a new instance of the map, because we set some values of it
				Map<String, String> arguments = new HashMap<String, String>(spec.getArguments());
				// we make a new instance of the
				// provider, because we do not want to
				// configure the same several times.
				ILabelledEntriesProvider p = entriesProvider.getClass().newInstance();
				p.init(arguments, resolver, entityResolver, currentFileURL);
				configuredEntriesProviders.add(p);
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
							       + "\nusing config file "
							       + configFile
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
	this.providers = configuredEntriesProviders;
    }

    /**
     * Set the attribute given by {@link attributeName} on the
     * {@link locationNode} node in a user dialogue.
     */
    protected void setAttribute() throws AuthorOperationException {

	// get current attribute value
	AuthorDocumentController doc = authorAccess.getDocumentController();
	Object[] attrNodes =
	    doc.evaluateXPath("@" + attributeName, locationNode, false, false, false, false);
	String currentString = "";
	boolean attributePresent = false;
	if (attrNodes.length > 0) {
	    currentString = ((Attr) attrNodes[0]).getValue();
	    attributePresent = true;
	}

	// split current values by space
	List<String> current = Arrays.asList(currentString.split("\\s+"));

	List<String> selected = null;

	// do user interaction
	try {
	    // get user dialog from configuration
	    ISelectionDialog dialogView;
	    Class dialogClass = Class.forName(dialog);
	    if (ISelectionDialog.class.isAssignableFrom(dialogClass)) {
		dialogView = (ISelectionDialog) dialogClass.newInstance();
		dialogView.init(authorAccess, message, multiple, current, providers);
		selected = dialogView.doUserInteraction();
	    } else {
		throw new AuthorOperationException("Configuration ERROR: ISelectionDialog not implemented by "
						   + dialog);
	    }

	} catch (ClassNotFoundException e) {
	    throw new AuthorOperationException("Error loading user dialog class "
					       + dialog + "\n\n" + e);
	} catch (InstantiationException e) {
	    throw new AuthorOperationException("Error instantiating user dialog class "
					       + dialog + "\n\n" + e);
	} catch (IllegalAccessException e) {
	    throw new AuthorOperationException("Error accessing user dialog class "
					       + dialog + "\n\n" + e);
	}

	// // TODO: dialog make pluggable
	// ISelectionDialog dialog = new OxygenSelectionDialog();
	// //ISelectionDialog dialog = new EdiarumSelectionDialog();
	// dialog.init(authorAccess, message, multiple, current, providers);
	// List<String> selected = dialog.doUserInteraction();

	// set the attribute value, if not null returned form
	// doUserInteraction(), because null means cancellation
	if (selected != null) {
	    // make the new value
	    String newValue = "";
	    for (int i = 0; i < selected.size(); i++) {
		if (i > 0) {
		    // add separator
		    newValue += " ";
		}
		newValue += selected.get(i);
	    }
	    // get the element
	    AuthorElement locationElement = (AuthorElement) locationNode;
	    // set attribute if not empty string
	    if (!(newValue.isEmpty())) {
		AttrValue val = new AttrValue(newValue);
		doc.setAttribute(attributeName, val, locationElement);
	    } else {
		// remove attribute if empty string
		doc.removeAttribute(attributeName, locationElement);
	    }
	}
    }
    
}

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

import java.util.Arrays;
import java.util.List;
import javax.xml.transform.URIResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import org.xml.sax.EntityResolver;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AttrValue;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntriesLoader;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.oxbytei.commons.ISelectionDialog;


abstract class AbstractOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOperation.class);

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
     * The node, which to operate on, i.e. which to set the attribute on.
     */
    protected AuthorNode locationNode;

    /**
     * Get the plugins registered for {@link ILabelledEntriesProvider}
     * and configure them based on config file and <prefixDef>
     * elements in the currently edited file.
     */
    public static List<ILabelledEntriesProvider> setupLabelledEntriesProviders
	(AuthorAccess authorAccess,
	 String nodeType,
	 String nodeName)
	throws AuthorOperationException {

	// get the uri resolver, entity resolver used by oxygen and editing context
	URIResolver uriResolver = authorAccess.getXMLUtilAccess().getURIResolver();
	EntityResolver entityResolver = authorAccess.getXMLUtilAccess().getEntityResolver();
	String currentFileURL = authorAccess.getEditorAccess().getEditorLocation().toString();

	// get the URL of the configuration file
	String configFile = OxbyteiConstants.getConfigFile();

	Object[] doc = authorAccess.getDocumentController().evaluateXPath(OxbyteiConstants.DOCUMENT_XPATH, true, false, false, true);
	Object[] context = authorAccess.getDocumentController().evaluateXPath(OxbyteiConstants.CONTEXT_XPATH, true, false, false, true);

	try {
	    return LabelledEntriesLoader.providersForContext((Document) doc[0],
							     currentFileURL,
							     (String) context[0],
							     nodeType,
							     nodeName,
							     uriResolver,
							     entityResolver,
							     null,
							     configFile);
	} catch (ConfigurationException e) {
	    throw new AuthorOperationException("" + e);
	} catch (ExtensionException e) {
	    throw new AuthorOperationException("" + e);
	}
    }

    /**
     * Set the attribute given by {@link attributeName} on the
     * {@link locationNode} node in a user dialogue.
     */
    protected void setAttribute(AuthorAccess authorAccess, List<ILabelledEntriesProvider> providers)
	throws AuthorOperationException  {

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

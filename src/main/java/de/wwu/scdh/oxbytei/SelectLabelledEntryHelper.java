package de.wwu.scdh.oxbytei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.transform.URIResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import org.xml.sax.EntityResolver;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.AuthorConstants;

import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntriesLoader;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.oxbytei.commons.ISelectionDialog;


/**
 * {@link SelectLabelledEntryHelper} provides plugin lookup and
 * interaction with the user.  It is intended to be mixed into a
 * derived {@link AuthorOperation}.
 *
 * After making an instance call {@link doUserInteraction()} to let the
 * user interactively select values generated by the plugins.
 *
 */
public class SelectLabelledEntryHelper
    implements InteractiveOperation {

    public static final String[] ARGUMENT_MULTIPLE_ALLOWED_VALUES = new String[] {
	AuthorConstants.ARG_VALUE_FALSE,
	AuthorConstants.ARG_VALUE_TRUE
    };

    public static final ArgumentDescriptor ARGUMENT_MULTIPLE =
	new ArgumentDescriptor("multiple",
			       ArgumentDescriptor.TYPE_CONSTANT_LIST,
			       "Whether or not multiple selections are allowed."
			       + " Defaults to false.",
			       ARGUMENT_MULTIPLE_ALLOWED_VALUES,
			       AuthorConstants.ARG_VALUE_FALSE);

    public static final ArgumentDescriptor ARGUMENT_MESSAGE =
	new ArgumentDescriptor("message",
			       ArgumentDescriptor.TYPE_STRING,
			       "The message in the user dialog.");

    public static final ArgumentDescriptor ARGUMENT_DIALOG =
	new ArgumentDescriptor("dialog",
			       ArgumentDescriptor.TYPE_STRING,
			       "The user dialogue used for this operation.",
			       "de.wwu.scdh.oxbytei.commons.OxygenSelectionDialog");

    /**
     * Return an array of the arguments usually needed.
     */
    public static final ArgumentDescriptor[] getArguments() {
	return new ArgumentDescriptor[] {
	    ARGUMENT_DIALOG,
	    ARGUMENT_MESSAGE,
	    ARGUMENT_MULTIPLE
	};
    }


    private static final Logger LOGGER = LoggerFactory.getLogger(SelectLabelledEntryHelper.class);

    /**
     * The type of the node to be edited.
     */
    protected String nodeType;

    /**
     * Attribute name to be set by {@link setAttribute}
     */
    protected String nodeName;

    /**
     * The XPath location that identifies the element.
     */
    protected String elementLocation;

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
     * Delimiter of multiple selected values.
     */
    protected String valuesDelimiter;

    /**
     * Regular expression used to split current value into multiple
     * selection values.
     */
    protected String valuesDelimiterRegex;

    private AuthorAccess authorAccess;

    private List<ILabelledEntriesProvider> providers;

    private String currentValue;

    /**
     * The current editing context as an XPath expression.
     */
    private String context;

    /**
     * The constructor loads the plugins for the current editing
     * context and configures them based on the config file.
     *
     * @param authorAccess {@link AuthorAccess} from the author operation
     * @param nodeType the type of the node to be edited
     * @param nodeName the name of the node to be edited
     * @param elementLocation the (relative) XPath location that identifies the element
     * @param dialog the canonical name of the selection user dialog class
     * @param message the text message presented to the user in the selection dialog
     * @param multiple whether or not multiple selections are possible
     * @param delimiter the delimiter if there are multiple selected values
     * @param delimiterRegex a regular expression used to used to
     * split the current node value into multiple selection values
     */
    public SelectLabelledEntryHelper
	(AuthorAccess authorAccess,
	 final String nodeType,
	 final String nodeName,
	 final String elementLocation,
	 final String dialog,
	 final String message,
	 final boolean multiple,
	 final String delimiter,
	 final String delimiterRegex)
	throws AuthorOperationException {

	this.authorAccess = authorAccess;
	this.nodeType = nodeType;
	this.nodeName = nodeName;
	this.elementLocation = elementLocation;
	this.dialog = dialog;
	this.message = message;
	this.multiple = multiple;
	valuesDelimiter = delimiter;
	valuesDelimiterRegex = delimiterRegex;

	// get the uri resolver, entity resolver used by oxygen and editing context
	URIResolver uriResolver = authorAccess.getXMLUtilAccess().getURIResolver();
	EntityResolver entityResolver = authorAccess.getXMLUtilAccess().getEntityResolver();
	String currentFileURL = authorAccess.getEditorAccess().getEditorLocation().toString();

	// get the URL of the configuration file
	String configFile = OxbyteiConstants.getConfigFile();

	providers = null;
	try {
	    // get the document DOM object
	    Object[] docNodes =
		authorAccess.getDocumentController().evaluateXPath(OxbyteiConstants.DOCUMENT_XPATH, true, false, false, true);
	    Document document = (Document) docNodes[0];

	    // get the current editing context as an XPath expression
	    Object[] contxt =
		authorAccess.getDocumentController().evaluateXPath(OxbyteiConstants.CONTEXT_XPATH, true, false, false, true);
	    context = (String) contxt[0];

	    LOGGER.debug("Loading providers for {} {} on context {}", nodeType, nodeName, context);

	    providers =
		LabelledEntriesLoader.providersForContext(document,
							  currentFileURL,
							  context,
							  nodeType,
							  nodeName,
							  uriResolver,
							  entityResolver,
							  null,
							  configFile);
	} catch (IndexOutOfBoundsException e) {
	    throw new AuthorOperationException("No document node found");
	} catch (ConfigurationException e) {
	    throw new AuthorOperationException("" + e);
	} catch (ExtensionException e) {
	    throw new AuthorOperationException("" + e);
	}

	// get current value
	if (nodeType == ExtensionConfiguration.ATTRIBUTE_VALUE) {
	    Object[] elementNodes =
		authorAccess.getDocumentController().evaluateXPath(elementLocation,
								   false, true, true, false);
	    currentValue = ((Element) elementNodes[0]).getAttribute(nodeName);
	    LOGGER.error("Current attribute value: {}", currentValue);
	} else {
	    if (nodeType == ExtensionConfiguration.TEXT_NODE) {
		// is that xpath correct?
		Object[] elementNodes =
		    authorAccess.getDocumentController().evaluateXPath(context,
								       false, true, true, false);
		currentValue = ((Text) elementNodes[0]).getWholeText();
	    } else {
		// TODO: what to do in other cases?
		currentValue = null;
	    }
	}

    }

     /**
     * The constructor loads the plugins for the current editing
     * context and configures them based on the config file. Selecting
     * multiple values is disabled.
     *
     * @param authorAccess {@link AuthorAccess} from the author operation
     * @param nodeType the type of the node to be edited
     * @param nodeName the name of the node to be edited
     * @param dialog the canonical name of the selection user dialog class
     */
    public SelectLabelledEntryHelper
	(AuthorAccess authorAccess,
	 String nodeType,
	 String nodeName,
	 String elementLocation,
	 String dialog,
	 String message)
	throws AuthorOperationException {
	this(authorAccess, nodeType, nodeName, elementLocation, dialog, message, false, null, null);
    }


    /**
     * Do the actual user interaction.
     */
    public String doUserInteraction()
	throws AuthorOperationException  {

	List<String> currentSelection;
	// split current values by space
	if (valuesDelimiter != null && currentValue != null) {
	    currentSelection = Arrays.asList(currentValue.split(valuesDelimiterRegex));
	} else {
	    currentSelection = new ArrayList<String>();
	    currentSelection.add(currentValue);
	}

	List<String> selected = null;

	// do user interaction
	try {
	    // get user dialog from configuration
	    ISelectionDialog dialogView;
	    Class dialogClass = Class.forName(dialog);
	    if (ISelectionDialog.class.isAssignableFrom(dialogClass)) {
		dialogView = (ISelectionDialog) dialogClass.newInstance();
		dialogView.init(authorAccess, message, multiple, currentSelection, providers);
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
		    newValue += valuesDelimiter;
		}
		newValue += selected.get(i);
	    }
	    return newValue;
	} else {
	    return null;
	}
    }

}

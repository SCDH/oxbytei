package de.wwu.scdh.oxbytei;

import java.awt.Frame;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.contentcompletion.xml.WhatAttributesCanGoHereContext;
import ro.sync.contentcompletion.xml.CIAttribute;

import de.wwu.scdh.teilsp.config.EditorVariablesExpander;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntriesLoader;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.oxbytei.commons.EditorVariablesExpanderImpl;
import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;
import de.wwu.scdh.teilsp.exceptions.UIException;
import de.wwu.scdh.teilsp.ui.ISelectionDialog;
import de.wwu.scdh.oxbytei.commons.WSSchemaManager;
import de.wwu.scdh.oxbytei.commons.SchemaAttributeDialog;


public class SchemaAttributeEditor
    implements InteractiveOperation {

    public static final String[] ARGUMENT_BOOLEAN_VALUES = new String[] {
	AuthorConstants.ARG_VALUE_FALSE,
	AuthorConstants.ARG_VALUE_TRUE
    };

    public static final ArgumentDescriptor ARGUMENT_ROLLBACK_ON_CANCEL =
	new ArgumentDescriptor("rollbackOnCancel",
			       ArgumentDescriptor.TYPE_CONSTANT_LIST,
			       "This controls how a cancellation of the user dialog is handled."
			       + " In chains of multiple actions throwing an error an stopping the chain"
			       + " is a good idea."
			       + "\n\nDefaults to true.",
			       ARGUMENT_BOOLEAN_VALUES,
			       AuthorConstants.ARG_VALUE_FALSE);

    public static final ArgumentDescriptor ARGUMENT_MESSAGE =
	new ArgumentDescriptor("message",
			       ArgumentDescriptor.TYPE_STRING,
			       "The message in the user dialog.");

    public static final ArgumentDescriptor ARGUMENT_DIALOG =
	new ArgumentDescriptor("dialog",
			       ArgumentDescriptor.TYPE_STRING,
			       "The user dialogue used for this operation.",
			       "de.wwu.scdh.teilsp.ui.ComboBoxSelectDialog");

    public static final URL DEFAULT_ICON =
	SelectLabelledEntryInteraction.class.getResource("/images/ask-24.png");

    public static final ArgumentDescriptor ARGUMENT_ICON =
	new ArgumentDescriptor("icon",
			       ArgumentDescriptor.TYPE_STRING,
			       "The icon displayed in the user dialogue.",
			       DEFAULT_ICON.toString());

    public static final ArgumentDescriptor ARGUMENT_DELIMITER =
	new ArgumentDescriptor("valuesDelimiter",
			       ArgumentDescriptor.TYPE_STRING,
			       "The string that separates multiple selected values."
			       + "\nDefaults to space.",
			       " ");

    public static final ArgumentDescriptor ARGUMENT_DELIMITER_REGEX =
	new ArgumentDescriptor("valuesDelimiterRegex",
			       ArgumentDescriptor.TYPE_STRING,
			       "A regular expression for splitting multiple values in the current value string."
			       + "\nDefaults to whitespace.",
			       "\\s+");

    /**
     * Return an array of the arguments usually needed.
     */
    public static final ArgumentDescriptor[] getArguments() {
	return new ArgumentDescriptor[] {
	    ARGUMENT_DIALOG,
	    ARGUMENT_ROLLBACK_ON_CANCEL,
	    ARGUMENT_MESSAGE,
	    ARGUMENT_ICON,
	    ARGUMENT_DELIMITER,
	    ARGUMENT_DELIMITER_REGEX
	};
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectLabelledEntryInteraction.class);

    /**
     * The type of the node to be edited.
     */
    protected String nodeType;

    /**
     * Attribute or element name to be set by the operation
     */
    protected String nodeName;

    /**
     * Namespace of the element or attribute name to be set by the operation
     *
     */
    protected String nodeNamespace;

    /**
     * The XPath location relative to the current editing position that identifies the element.
     */
    protected String location;

    /**
     * A map of arguments passed in to the author mode operation
     */
    protected ArgumentsMap arguments;

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
     * @param nodeName the local name of the node to be edited
     * @param nodeNamespace the namespace part of the node's name
     * @param location the relative XPath location with respect to the
     * current caret position that identifies the element
     * @param argumentsMap the map of arguments, passed to the author
     * mode operation
     */
    public SchemaAttributeEditor
	(AuthorAccess authorAccess,
	 final String nodeType,
	 final String nodeName,
	 final String nodeNamespace,
	 final String location,
	 final ArgumentsMap argumentsMap)
	throws AuthorOperationException {

	this.authorAccess = authorAccess;
	this.nodeType = nodeType;
	this.nodeName = nodeName;
	this.nodeNamespace = nodeNamespace;
	this.location = location;
	this.arguments = argumentsMap;

    }

    /**
     * Do the actual user interaction.
     */
    public String doUserInteraction()
	throws AuthorOperationException {

	// get attributes allowed in the editing context
	List<CIAttribute> ciattributes = WSSchemaManager.whatAttributesCanGoHere();
	//Vector<String> attributes = new Vector<String>();
	Vector<CIAttribute> attributes = new Vector<CIAttribute>(ciattributes);
	// for (CIAttribute attr : ciattributes) {
	//     String prefix = "";
	//     if (attr.getPrefix() != null) {
	// 	if (!attr.getPrefix().isEmpty()) {
	// 	    prefix = ":" + attr.getPrefix();
	// 	}
	//     }
	//     //attributes.add(prefix + attr.getName());
	//     attributes.add(attr.toString());
	// }

	// System.out.println("Allowed attributes");
	// for (String name : attributes) {
	//     System.out.println(name);
	// }

	Frame frame = (Frame) authorAccess.getWorkspaceAccess().getParentFrame();
	SchemaAttributeDialog schemaDialog = new SchemaAttributeDialog(frame, context, DEFAULT_ICON, attributes);
	try {
	    schemaDialog.doUserInteraction();
	} catch (ExtensionException e) {
	    //
	}

	return null;
    }

}

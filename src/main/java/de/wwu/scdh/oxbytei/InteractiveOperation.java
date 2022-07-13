package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.ArgumentsMap;

import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.exceptions.UIException;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.oxbytei.commons.RollbackException;
import de.wwu.scdh.oxbytei.commons.DocumentReaderException;

/**
 * An interface for an interactive (author mode) operation.
 *
 * @author Christian Lück
 */
public interface InteractiveOperation {

    /**
     * This loads the plugins for the current editing
     * context and configures them based on the config file.
     *
     * @param nodeType the type of the node to be edited
     * @param nodeName the local name of the node to be edited
     * @param nodeNamespace the namespace part of the node's name
     * @param location the relative XPath location with respect to the
     * current caret position that identifies the element
     *
     * @return the count of providers for the node at the location
     */
    public int init
	(String nodeType,
	 String nodeName,
	 String nodeNamespace,
	 String location)
	throws ExtensionException, ConfigurationException, DocumentReaderException;

    /**
     * Calling {@code doUserInteraction()} actually does the user
     * interaction.
     *
     * @param argumentsMap the map of arguments, passed to the author
     * mode operation
     */
    public String doUserInteraction
	(ArgumentsMap argumentsMap)
	throws ExtensionException, ConfigurationException, UIException, RollbackException;

}

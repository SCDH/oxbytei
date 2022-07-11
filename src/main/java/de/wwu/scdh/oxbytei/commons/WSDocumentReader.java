/**
 * @author Christian Lück
 */
package de.wwu.scdh.oxbytei.commons;

import org.w3c.dom.Document;


/**
 * An interface for basic reading of the XML file currently
 * edited. There may be implementations for text mode and author
 * mode. This interface enables us to write more generic classes.
 *
 * There are failing and non-failing version of each method.
 */
public interface WSDocumentReader {

    /**
     * This returns a text node.
     *
     * @param location The XPath location relative to the current
     * editing position that identifies the element.
     */
    public String getTextNode(String location)
	throws DocumentReaderException;

    /**
     * Non-failing version of {@link getTextNode}
     *
     * @param location The XPath location relative to the current
     * editing position that identifies the element.
     */
    public String lookupTextNode(String location);

    /**
     * This returns an attribute value.
     *
     * @param location The XPath location relative to the current
     * editing position that identifies the element.
     * @param name The local name of the node (element name,
     * attribute name).
     * @param namespace The namespace name of the node.
     */
    public String getAttributeValue(String location, String name, String namespace)
	throws DocumentReaderException;

    /**
     * Non-failing version of {@link getAttributeValue}
     *
     * @param location The XPath location relative to the current
     * editing position that identifies the element.
     * @param name The local name of the node (element name,
     * attribute name).
     * @param namespace The namespace name of the node.
     */
    public String lookupAttributeValue(String location, String name, String namespace);

    /**
     * This returns a node.
     *
     * @param location The XPath location relative to the current
     * editing position that identifies the element.
     * @param nodeType The type of node. See {@link ExtensionConfiguration}
     * for constants
     * @param name The local name of the node (element name,
     * attribute name).
     * @param namespace The namespace name of the node.
     */
    public String getNode(String location, String nodeType, String name, String namespace)
	throws DocumentReaderException;

    /**
     * Non-failing version of {@link getNode}
     *
     * @param location The XPath location relative to the current
     * editing position that identifies the element.
     * @param nodeType The type of node. See {@link ExtensionConfiguration}
     * for constants
     * @param name The local name of the node (element name,
     * attribute name).
     * @param namespace The namespace name of the node.
     */
    public String lookupNode(String location, String nodeType, String name, String namespace);

    /**
     * This returns the document node of the document currently edited.
     */
    public Document getDocument() throws DocumentReaderException;

    /**
     * Non-failing version of {@link getDocument}
     *
     */
    public Document lookupDocument();

    /**
     * This returns the XPath to the current node of the document
     * edited.
     */
    public String getContextXPath() throws DocumentReaderException;

    /**
     * Non-failing version of {@link getContextXPath}
     *
     */
    public String lookupContextXPath();

}

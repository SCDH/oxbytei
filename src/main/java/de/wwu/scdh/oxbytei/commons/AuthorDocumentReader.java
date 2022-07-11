package de.wwu.scdh.oxbytei.commons;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.oxbytei.OxbyteiConstants;

/**
 * An implementation of {@link WSDocumentReader} for author mode.
 *
 */
public class AuthorDocumentReader implements WSDocumentReader {

    private AuthorAccess authorAccess;

    public AuthorDocumentReader(AuthorAccess authorAccess) {
	this.authorAccess = authorAccess;
    }

    /**
     * @param location The XPath location relative to the current
     * editing position that identifies the element.
     */
    public String getAttributeValue(String location, String name, String namespace)
	throws DocumentReaderException {
	String currentValue;
	try {
	    Object[] elementNodes =
		authorAccess.getDocumentController().evaluateXPath(location, false, true, true, false);
	    if (namespace == null) {
		currentValue = ((Element) elementNodes[0]).getAttribute(name);
	    } else {
		currentValue = ((Element) elementNodes[0]).getAttributeNS(namespace, name);
	    }
	} catch (AuthorOperationException e) {
	    throw new DocumentReaderException(e);
	}
	return currentValue;
    }

    public String lookupAttributeValue(String location, String name, String namespace) {
	try {
	    return getAttributeValue(location, name, namespace);
	} catch (DocumentReaderException e) {
	    return null;
	}
    }

    public String getTextNode(String location)
	throws DocumentReaderException {
	String currentValue;
	try {
	    // is that xpath correct?
	    Object[] elementNodes =
		authorAccess.getDocumentController().evaluateXPath(location, false, true, true, false);
	    currentValue = ((Text) elementNodes[0]).getWholeText();
	} catch (AuthorOperationException e) {
	    throw new DocumentReaderException(e);
	}
	return currentValue;
    }

    public String lookupTextNode(String location) {
	try {
	    return getTextNode(location);
	} catch (DocumentReaderException e) {
	    return null;
	}
    }

    public String getNode(String nodeType, String location, String name, String namespace)
	throws DocumentReaderException {
	// get current value
	if (nodeType == ExtensionConfiguration.ATTRIBUTE_VALUE) {
	    return getAttributeValue(location, name, namespace);
	} else if (nodeType == ExtensionConfiguration.TEXT_NODE) {
	    return getTextNode(location);
	} else {
	    // TODO: what to do in other cases?
	    return null;
	}
    }

    public String lookupNode(String nodeType, String location, String name, String namespace) {
	try {
	    return getNode(nodeType, location, name, namespace);
	} catch (DocumentReaderException e) {
	    return null;
	}
    }

    public Document getDocument()
	throws DocumentReaderException {
	try {
	    // get the document DOM object
	    Object[] docNodes =
		authorAccess.getDocumentController().evaluateXPath(OxbyteiConstants.DOCUMENT_XPATH, false, false, false, true);
	    Document document = (Document) docNodes[0];
	    return document;
	} catch (IndexOutOfBoundsException e) {
	    throw new DocumentReaderException("Error getting document node using XPath "
					      + OxbyteiConstants.DOCUMENT_XPATH
					      + " No document node found");
	} catch (AuthorOperationException e) {
	    throw new DocumentReaderException("Error getting document node using XPath "
					      + OxbyteiConstants.DOCUMENT_XPATH
					      + "\n\n" + e);
	}
    }

    public Document lookupDocument() {
	try {
	    return getDocument();
	} catch (DocumentReaderException e) {
	    return null;
	}
    }

    public String getContextXPath()
	throws DocumentReaderException {
	try {
	    // get the current editing context as an XPath expression
	    // TODO: reflect relative 'location'
	    Object[] contxt =
		authorAccess.getDocumentController().evaluateXPath(OxbyteiConstants.CONTEXT_XPATH, true, false, false, true);
	    String context = (String) contxt[0];
	    return context;
	} catch (IndexOutOfBoundsException e) {
	    throw new DocumentReaderException("Error getting context using XPath "
					      + OxbyteiConstants.CONTEXT_XPATH
					      + " : nothing returned");
	} catch (AuthorOperationException e) {
	    throw new DocumentReaderException("Error getting context using XPath "
					      + OxbyteiConstants.CONTEXT_XPATH
					      + "\n\n" + e);
	}
    }

    public String lookupContextXPath() {
	try {
	    return getContextXPath();
	} catch (DocumentReaderException e) {
	    return null;
	}
    }

}

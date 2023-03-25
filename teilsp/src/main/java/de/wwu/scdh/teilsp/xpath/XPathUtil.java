package de.wwu.scdh.teilsp.xpath;

import javax.xml.xpath.XPath;

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.w3c.dom.Document;

/**
 * Utilities for processing XPath 2.0.
 */
public class XPathUtil {

    /**
     * This static method encapsulates calling an XPath
     * factory. It calls Saxon's XPath factory and passes the
     * document configuration to the factory.
     *
     * @param document  an DOM document node
     * @return an Saxon XPath object that processes 2.0 expressions
     */
    public static XPath makeXPath (Document document) {
	XPathFactoryImpl xpathFactoryImpl = new XPathFactoryImpl();
	if (document instanceof DocumentOverNodeInfo) {
	    xpathFactoryImpl.setConfiguration(((DocumentOverNodeInfo) document).getUnderlyingNodeInfo().getConfiguration());
	}
	XPath xpath = xpathFactoryImpl.newXPath();
	return xpath;
    }

}

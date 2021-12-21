package de.wwu.scdh.teilsp.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.wwu.scdh.teilsp.exceptions.DocumentReaderException;
import de.wwu.scdh.teilsp.xml.NamespaceContextImpl;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;


public class LabelledEntriesFromXMLReader {

    protected String prefix;

    protected String selectionXPath;

    protected String labelXPath;

    protected String keyXPath;

    protected NamespaceContext namespaceDecl;

    protected URL url;

    public LabelledEntriesFromXMLReader() {}

    public LabelledEntriesFromXMLReader(String prefix, URL href,
					String selection, String key, String label,
					String namespaces)
	throws DocumentReaderException {

	if (prefix == null) {
	    this.prefix = "";
	} else {
	    this.prefix = prefix;
	}

	this.selectionXPath = selection;
	this.keyXPath = key;
	this.labelXPath = label;
	this.namespaceDecl = new NamespaceContextImpl(namespaces);

	this.url = href;
    }

    public List<LabelledEntry> getLabelledEntries(String userInput)
	throws ExtensionException {
	List<LabelledEntry> entries = new ArrayList<LabelledEntry>();
	try {
	    // open the document url
            URLConnection urlConnection = this.url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();

	    // prepare dom builder
	    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true);
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    // parse the input document
	    InputSource inputSource = new InputSource(inputStream);
	    Document indexDoc = builder.parse(inputSource);
	    // prepare the XPath query
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    xpath.setNamespaceContext(this.namespaceDecl);
	    // run the XPath query
	    NodeList itemNodes = (NodeList) xpath.evaluate(this.selectionXPath, indexDoc, XPathConstants.NODESET);

	    //this.itemNodes = (NodeList) result;

	    // get the count of nodes found in getItemNodes()
	    int count = 0;
	    if (itemNodes != null) {
		count = itemNodes.getLength();
	    }

	    try {
		// for every node in itemNodes ...
		int i;
		for (i = 0; i < count; i++) {
		    // make an element from it
		    Element item = (Element) itemNodes.item(i);

		    // get key
		    String key = xpath.evaluate(keyXPath, item);

		    // get label
		    String label = xpath.evaluate(labelXPath, item);

		    // store them away
		    entries.add(new LabelledEntry(this.prefix + key, label));
		}
	    } catch (XPathExpressionException e) {
		inputStream.close();
		throw new ExtensionException(e);
	    } catch (NullPointerException e) {
		inputStream.close();
		throw new ExtensionException(e);
	    }
	    inputStream.close();
	} catch (ParserConfigurationException e) {
	    throw new ExtensionException(e);
	} catch (SAXException e) {
	    throw new ExtensionException(e);
	} catch (IOException e) {
	    throw new ExtensionException(e);
	} catch (XPathExpressionException e) {
	    throw new ExtensionException(e);
	}
	return entries;
    }

}

package de.wwu.scdh.teilsp.completion;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

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


public class SelectionItemsXMLReader {

    private LabelledEntry[] entries;

    private String prefix;

    private String url;

    private String selectionXPath;

    private String labelXPath;

    private String keyXPath;

    private NamespaceContext namespaceDecl;

    private InputStream inputStream;

    private NodeList itemNodes;

    public SelectionItemsXMLReader(PrefixDef prefixDef, InputStream inStream,
				   String selection, String key, String label,
				   String namespaces)
	throws DocumentReaderException {

	this.prefix = prefixDef.getIdent() + ":";

	this.selectionXPath = selection;
	this.keyXPath = key;
	this.labelXPath = label;
	this.namespaceDecl = new NamespaceContextImpl(namespaces);

	this.inputStream = inStream;
	this.getItemNodes();
	this.read();
    }

    /**
     * return the parsed labelled entries
     */
    public LabelledEntry[] getEntries() {
	return this.entries;
    }

    /**
     * get count of parsed labelled entries
     */
    public int getLength() {
	return this.entries.length;
    }

    /**
     * get count of found nodes for making the selection
     */
    public int nodesCount() {
	return this.itemNodes.getLength();
    }

    private void getInputStream() throws DocumentReaderException {
	try {
	    try {
		URL theURL = new URL(this.url);
		URLConnection urlConnection = theURL.openConnection();
		this.inputStream = urlConnection.getInputStream();
	    } catch (MalformedURLException e) {
		this.inputStream = new FileInputStream(this.url);
	    }
	} catch (IOException e) {
	    throw new DocumentReaderException(e);
	}
    }

    private void getItemNodes() throws DocumentReaderException {
	try {
	    // prepare dom builder
	    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true);
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    // parse the input document
	    InputSource inputSource = new InputSource(this.inputStream);
	    Document indexDoc = builder.parse(inputSource);
	    // prepare the XPath query
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    xpath.setNamespaceContext(this.namespaceDecl);
	    // run the XPath query
	    Object result = xpath.evaluate(this.selectionXPath, indexDoc, XPathConstants.NODESET);
	    this.itemNodes = (NodeList) result;
	} catch (ParserConfigurationException e) {
	    throw new DocumentReaderException(e);
	} catch (SAXException e) {
	    throw new DocumentReaderException(e);
	} catch (IOException e) {
	    throw new DocumentReaderException(e);
	} catch (XPathExpressionException e) {
	    throw new DocumentReaderException(e);
	}
    }

    private void read() throws DocumentReaderException {

	// get the count of nodes found in getItemNodes()
	int count = 0;
	if (this.itemNodes != null) {
	    count = this.itemNodes.getLength();
	}

	// initialize with size
	this.entries = new LabelledEntry[count];

	// prepare XPath queries
	XPathFactory xpathFactory = XPathFactory.newInstance();
	XPath xpath = xpathFactory.newXPath();
	xpath.setNamespaceContext(this.namespaceDecl);

	try {
	    // for every node in itemNodes ...
	    int i;
	    for (i = 0; i < count; i++) {
		// make an element from it
		Element item = (Element) this.itemNodes.item(i);

		// get key
		String key = xpath.evaluate(keyXPath, item);

		// get label
		String label = xpath.evaluate(labelXPath, item);

		// store them away
		this.entries[i] = new LabelledEntry(this.prefix + key, label);
	    }
	} catch (XPathExpressionException e) {
	    throw new DocumentReaderException(e);
	} catch (NullPointerException e) {
	    throw new DocumentReaderException(e);
	}
    }
}

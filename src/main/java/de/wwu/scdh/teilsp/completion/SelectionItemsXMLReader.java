package de.wwu.scdh.teilsp.completion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.net.MalformedURLException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.wwu.scdh.teilsp.exceptions.DocumentReaderException;


public class SelectionItemsXMLReader {

    private LabelledEntry[] entries;

    private String url;

    private String selectionXPath;

    private String labelXPath;

    private String namespaceDecl;

    private InputStream inputStream;

    private NodeList itemNodes;

    public SelectionItemsXMLReader(PrefixDef prefixDef, String selection, String label, String namespaces)
	throws DocumentReaderException {

	try {
	    // FIXME: make conform TEI's abstract definition
	    this.url = prefixDef.getReplacementPattern().split("#\\$")[0];

	} catch (Exception e) {
	    throw new DocumentReaderException(e);
	}

	this.selectionXPath = selection;
	this.labelXPath = label;
	this.namespaceDecl = namespaces;

	this.getInputStream();
	this.read();
	this.getItemNodes();
    }

    public LabelledEntry[] getEntries() {
	return entries;
    }

    private void getInputStream() throws DocumentReaderException {
	try {
	    URL url = new URL(this.url);
	    URLConnection urlConnection = url.openConnection();
	    this.inputStream = urlConnection.getInputStream();
	    }
	} catch (MalformedURLException e) {
	    throw new DocumentReaderException(e);
	} catch (IOException e) {
	    throw new DocumentReaderException(e);
	}
    }

    private void getItemNodes() throws DocumentReaderException {
	try {
	    // Das neue Dokument wird vorbereitet.
	    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true);
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    // Dann wird die Datei gelesen.
	    InputSource inputSource = new InputSource(this.inputStream);
	    Document indexDoc = builder.parse(inputSource);
	    // Die xPath-Routinen werden vorbereitet.
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    // Für Namespaces:
	    String[] namespaceSplit = namespaceDecl.split(" ");
	    String[][] namespaces = new String[namespaceSplit.length][2];
	    for (int i=0; i<namespaceSplit.length; i++) {
		String currentNamespace = namespaceSplit[i];
		int k = currentNamespace.indexOf(":");
		namespaces[i][0] = currentNamespace.substring(0, k);
		namespaces[i][1] = currentNamespace.substring(k+1);
	    }
	    final String[][] namespacesfinal = namespaces;
	    //			final PrefixResolver resolver = new PrefixResolverDefault(indexDoc);
	    NamespaceContext ctx = new NamespaceContext() {

		    @Override
		    public String getNamespaceURI(String prefix) {
			//					return resolver.getNamespaceForPrefix(prefix);
			String uri = null;
			for (int i=0; i<namespacesfinal.length; i++) {
			    if (prefix.equals(namespacesfinal[i][0])) {
				uri = namespacesfinal[i][1];
			    }
			}
			return uri;
		    }

		    @Override
		    public String getPrefix(String namespaceURI) {
			// TODO Auto-generated method stub
			return null;
		    }

		    @Override
		    public Iterator getPrefixes(String namespaceURI) {
			// TODO Auto-generated method stub
			return null;
		    }
		};
	    xpath.setNamespaceContext(ctx);
	    // Das XPath-Query wird definiert.
	    //XPathExpression expr = xpath.compile(node);

	    // Die Resultate werden ausgelesen..
	    //Object result = expr.evaluate(indexDoc, XPathConstants.NODESET);
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

    }
}

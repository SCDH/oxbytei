package de.wwu.scdh.teilsp.completion;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
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

//import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.wwu.scdh.teilsp.exceptions.DocumentReaderException;


public class SelectionItemsXMLReader {

    private LabelledEntry[] entries;

    private String prefix;

    private String url;

    private String selectionXPath;

    private String labelXPath;

    private String keyXPath;

    private String namespaceDecl;

    private InputStream inputStream;

    private boolean needsClose;

    private NodeList itemNodes;

    public SelectionItemsXMLReader(PrefixDef prefixDef, InputStream inStream,
				   String selection, String key, String label, String namespaces)
	throws DocumentReaderException {

	this.prefix = prefixDef.getIdent() + ":";

	this.selectionXPath = selection;
	this.keyXPath = key;
	this.labelXPath = label;
	this.namespaceDecl = namespaces;

	this.inputStream = inStream;
	this.read();
	this.getItemNodes();
    }

    public LabelledEntry[] getEntries() {
	return this.entries;
    }

    public int getLength() {
	return this.entries.length;
    }

    private void getInputStream() throws DocumentReaderException {
	this.needsClose = false;
	try {
	    try {
		URL theURL = new URL(this.url);
		URLConnection urlConnection = theURL.openConnection();
		this.inputStream = urlConnection.getInputStream();
	    } catch (MalformedURLException e) {
		this.inputStream = new FileInputStream(this.url);
		this.needsClose = true;
	    }
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
	this.entries = new LabelledEntry[this.itemNodes.getLength()];

	System.err.println("Length: " + this.itemNodes.getLength());

	String label, key;
	XPath xpath = XPathFactory.newInstance().newXPath();
	try {
	    // Für jeden Knoten ..
	    for (int i=0; i < this.itemNodes.getLength(); i++) {
		Element currentElement = (Element) this.itemNodes.item(i);
		// .. wird der Eintrag konstruiert.
		label = "";
		String currentEintrag = this.labelXPath;
		// Falls im Ausdruck der Hinweis "$XPATH{..}" vorkommt, ..
		int k = currentEintrag.indexOf("$XPATH{");
		while (k>=0) {
		    // .. wird der String davor als Text eingefügt, ..
		    label += currentEintrag.substring(0, k);
		    currentEintrag = currentEintrag.substring(k);
		    int l = currentEintrag.indexOf("}");
		    // .. und der Ausdruck selbst ausgewertet:
		    String xpathExpression = currentEintrag.substring("$XPATH{".length(), l);
		    // Jedes Glied kann entweder als Attribut, ..
		    if (xpathExpression.startsWith("@")) {
			String attributeName = xpathExpression.substring(1);
			label += currentElement.getAttribute(attributeName);
		    }
		    // .. als nachkommendes Element ..
		    if (xpathExpression.startsWith("//")) {
			// .. (was direkt gelesen werden kann und schnell geht, ..
			String elementName = xpathExpression.substring(2);
			if (elementName.contains(":")) {
			    elementName = elementName.substring(elementName.indexOf(":")+1);
			}
			if (currentElement.getElementsByTagName(elementName).getLength()>0){
			    label += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
			} else {
			    // .. oder welches über eine X-Path-Abfrage gelesen werden kann und lange dauert), ..
			    XPathExpression queryExpr = xpath.compile("."+xpathExpression);
			    NodeList elementNodes = (NodeList) queryExpr.evaluate(this.itemNodes.item(i), XPathConstants.NODESET);
			    if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
				label += elementNodes.item(0).getTextContent();
			    }
			}
			// .. als direktes Kindelement (was schnell geht) ..
		    } else if (xpathExpression.startsWith("/")) {
			String elementName = xpathExpression.substring(1);
			if (elementName.contains(":")) {
			    elementName = elementName.substring(elementName.indexOf(":")+1);
			}
			if (currentElement.getElementsByTagName(elementName).getLength()>0){
			    label += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
			}
		    }
		    // .. oder als X-Path-Ausdruck (was sehr lange dauert) verstanden werden.
		    if (xpathExpression.startsWith(".")) {
			XPathExpression queryExpr = xpath.compile(xpathExpression);
			System.out.println(xpathExpression);
			NodeList elementNodes = (NodeList) queryExpr.evaluate(this.itemNodes.item(i), XPathConstants.NODESET);
			System.out.println(":"+elementNodes.item(0).getTextContent()+":");
			if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
			    label += elementNodes.item(0).getTextContent();
			}
		    }
		    // Für X-Path-Ausdrücke mit Funktionen:
		    if (xpathExpression.startsWith("#")) {
			XPathExpression queryExpr = xpath.compile(xpathExpression.substring(1));
			System.out.println(xpathExpression);
			String elementString = (String) queryExpr.evaluate(this.itemNodes.item(i), XPathConstants.STRING);
			System.out.println(":"+elementString+":");
			label += elementString;
		    }					// Der übrige Ausdruck wird danach ausgewertet.
		    currentEintrag = currentEintrag.substring(l+1);
		    k = currentEintrag.indexOf("$XPATH{");
		};
		// Falls "$XPATH[..]" nicht mehr auftaucht, wird der Rest angehängt.
		label += currentEintrag;

		// Genauso wird die ID konstruiert.
		key = "";
		String currentID = this.keyXPath;
		// Falls im Ausdruck der Hinweis "$XPATH{..}" vorkommt, ..
		k = currentID.indexOf("$XPATH{");
		while (k>=0) {
		    // .. wird der String davor als Text eingefügt, ..
		    key += currentID.substring(0, k);
		    currentID = currentID.substring(k);
		    int l = currentID.indexOf("}");
		    // .. und der Ausdruck selbst ausgewertet:
		    String xpathExpression = currentID.substring("$XPATH{".length(), l);
		    // Jedes Glied kann entweder als Attribut, ..
		    if (xpathExpression.startsWith("@")) {
			String attributeName = xpathExpression.substring(1);
			key += currentElement.getAttribute(attributeName);
		    }
		    // .. als nachkommendes Element ..
		    if (xpathExpression.startsWith("//")) {
			// .. (was direkt gelesen werden kann und schnell geht, ..
			String elementName = xpathExpression.substring(2);
			if (elementName.contains(":")) {
			    elementName = elementName.substring(elementName.indexOf(":")+1);
			}
			if (currentElement.getElementsByTagName(elementName).getLength()>0){
			    key += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
			} else {
			    // .. oder welches über eine X-Path-Abfrage gelesen werden kann und lange dauert), ..
			    XPathExpression queryExpr = xpath.compile("."+xpathExpression);
			    NodeList elementNodes = (NodeList) queryExpr.evaluate(this.itemNodes.item(i), XPathConstants.NODESET);
			    if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
				key += elementNodes.item(0).getTextContent();
			    }
			}
			// .. als direktes Kindelement (was schnell geht) ..
		    } else if (xpathExpression.startsWith("/")) {
			String elementName = xpathExpression.substring(1);
			if (elementName.contains(":")) {
			    elementName = elementName.substring(elementName.indexOf(":")+1);
			}
			if (currentElement.getElementsByTagName(elementName).getLength()>0){
			    key += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
			}
		    }
		    // .. oder als X-Path-Ausdruck (was sehr lange dauert) verstanden werden.
		    if (xpathExpression.startsWith(".")) {
			XPathExpression queryExpr = xpath.compile(xpathExpression);
			NodeList elementNodes = (NodeList) queryExpr.evaluate(this.itemNodes.item(i), XPathConstants.NODESET);
			if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
			    key += elementNodes.item(0).getTextContent();
			}
		    }
		    // Der übrige Ausdruck wird danach ausgewertet.
		    currentID = currentID.substring(l+1);
		    k = currentID.indexOf("$XPATH{");
		};
		// Falls "$XPATH[..]" nicht mehr auftaucht, wird der Rest angehängt.
		key += currentID;
		key = this.prefix + key;

		this.entries[i] = new LabelledEntry(key, label);
	    };
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
    }
}

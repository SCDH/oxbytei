package de.wwu.scdh.teilsp.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerException;

import org.xml.sax.EntityResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.saxon.xpath.XPathFactoryImpl;
import net.sf.saxon.dom.DocumentOverNodeInfo;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.xml.NamespaceContextImpl;


/**
 * {@link LabelledEntriesFromXML} is a plugin for reading a list of
 * {@link de.wwu.scdh.teilsp.services.extensions.LabelledEntry}
 * objects from a given XML file.
 *
 */
public class LabelledEntriesFromXMLByPrefixDef
    extends LabelledEntriesFromXMLReader
    implements ILabelledEntriesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelledEntriesFromXMLByPrefixDef.class);

    private Map<String, String> arguments;

    private static final String DEFAULT_REF_XPATH = "tokenize(@replacementPattern, '#')[1]";
    
    private static final ArgumentDescriptor ARGUMENT_PREFIX =
	new ArgumentDescriptor("prefixDef",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath to the <prefixDef> element in the currently edited file.");

    private static final ArgumentDescriptor ARGUMENT_PREFIX_REF =
	new ArgumentDescriptor("prefixDefHref",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath to the <prefixDef> element in the currently edited file.",
			       DEFAULT_REF_XPATH);

    private static final ArgumentDescriptor ARGUMENT_SELECTION =
	new ArgumentDescriptor("selection",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression to use for finding selection values."
			       + " This should regard the structure of the referred XML document.",
			       "//*[@xml:id]");

    private static final ArgumentDescriptor ARGUMENT_KEY =
	new ArgumentDescriptor("key",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression to use for generating key values of the selection items."
			       + " This should regard the structure of the referred XML document."
			       + " Default: @xml:id",
			       "@xml:id");

    private static final ArgumentDescriptor ARGUMENT_LABEL =
	new ArgumentDescriptor("label",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression to use for generating the labels of the selection items."
			       + " This should regard the structure of the referred XML document.",
			       "self::*");

    private static final ArgumentDescriptor ARGUMENT_NAMESPACE =
	new ArgumentDescriptor("namespace",
			       ArgumentDescriptor.TYPE_STRING,
			       "A space-separated list of prefix:namespace-name tuples for"
			       + " use in the XPath expressions for accessing the target documents."
			       + " This should regard the structure of the referred XML document.",
			       "t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
	ARGUMENT_PREFIX,
	ARGUMENT_PREFIX_REF,
	ARGUMENT_SELECTION,
	ARGUMENT_KEY,
	ARGUMENT_LABEL,
	ARGUMENT_NAMESPACE
	};

    /**
     * 
     */
    public ArgumentDescriptor[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    public void init(Map<String, String> args,
		     URIResolver uriResolver,
		     EntityResolver entityResolver,
		     Document currentDoc,
		     String systemId)
	throws ExtensionException {

	// get the XPath to the <prefixDef> element
	String prefixDefXPath = args.get("prefixDef");
	if (prefixDefXPath == null) {
	    throw new ExtensionException("Error: 'prefixDef' not set for "
					 + getClass().getCanonicalName());
	}

	// get XPath for getting the href out of replacement pattern
	String hrefXPath = args.getOrDefault("prefixDefRef", DEFAULT_REF_XPATH);

	// store namespace context declaration
	if (args.get("namespaces") != null) {
	    namespaceDecl = new NamespaceContextImpl(args.get("namespaces"));
	} else {
	    throw new ExtensionException("Argument 'namespaces' is required");
	}
	
	// get url from prefixDef
	String ident = null;
	String href = null;
	//String systemId = null;
	try {
	    // // prepare the XPath query, using Saxon here for XPath 2.0
	    // XPath xpath = new XPathFactoryImpl().newXPath();
	    // //xpath.setXPathFunctionResolver(new XPathFunctionLibrary().getXPathFunctionResolver());
	    // xpath.setNamespaceContext(namespaceDecl);

	    // setup XPath 2.0 from Saxon
	    XPathFactoryImpl xpathFactoryImpl = new XPathFactoryImpl();
	    if (currentDoc instanceof DocumentOverNodeInfo) {
	    	xpathFactoryImpl.setConfiguration(((DocumentOverNodeInfo) currentDoc).getUnderlyingNodeInfo().getConfiguration());
	    }
	    XPath xpath = xpathFactoryImpl.newXPath();
	    xpath.setNamespaceContext(namespaceDecl);

	    // run the XPath query
	    //systemId = (String) xpath.evaluate("base-uri(/)", currentDoc, XPathConstants.STRING);

	    NodeList prefixNodes = (NodeList) xpath.evaluate(prefixDefXPath, currentDoc, XPathConstants.NODESET);
	    if (prefixNodes.getLength() != 1) {
		throw new ExtensionException("Unsupported: There were " + prefixNodes.getLength()
					     + " <prefixDef> elements found by plugin "
					     + getClass().getCanonicalName()
					     + "\nwith XPath "
					     + prefixDefXPath);
	    } else {
		Element prefixDef = (Element) prefixNodes.item(0);
		ident = xpath.evaluate("@ident", prefixDef);
		href = xpath.evaluate(hrefXPath, prefixDef);
	    }
	} catch (XPathExpressionException e) {
	    throw new ExtensionException(e);
	}

	if (href == null) {
	    throw new ExtensionException("Error: No URL from prefix definition");
	} else {
	    try {
		String urlString = uriResolver.resolve(href, systemId.toString()).getSystemId();
		LOGGER.error("Resolving URL in prefixDef \"{}\" given in {} to {}", href, systemId, urlString);

		// open the document url
		URL url = new URL(urlString);
		URLConnection urlConnection = url.openConnection();
		InputStream inputStream = urlConnection.getInputStream();

		// prepare dom builder
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		// parse the input document
		InputSource inputSource = new InputSource(inputStream);
		document = builder.parse(inputSource);
		inputStream.close();
	    } catch (MalformedURLException e) {
		throw new ExtensionException("Error opening URL " + args.get("url") + "\n" + e);
	    } catch (TransformerException e) {
		throw new ExtensionException(e);
	    } catch (ParserConfigurationException e) {
		throw new ExtensionException(e);
	    } catch (SAXException e) {
		throw new ExtensionException(e);
	    } catch (IOException e) {
		throw new ExtensionException(e);
	    } catch (NullPointerException e) {
		throw new ExtensionException("Error excuting base-uri(/)\n" + e);
	    }
	}

	prefix = ident + ":";

	selectionXPath = args.get("selection");
	if (selectionXPath == null) {
	    throw new ExtensionException("Argument 'selection' is required");
	}
	keyXPath = args.get("key");
	if (keyXPath == null) {
	    throw new ExtensionException("Argument 'key' is required");
	}
	labelXPath = args.get("label");
	if (labelXPath == null) {
	    throw new ExtensionException("Argument 'label' is required");
	}

	arguments = args;

    }

    public Map<String, String> getArguments() {
	return arguments;
    }

}

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

import javax.xml.namespace.NamespaceContext;
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

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;
import de.wwu.scdh.teilsp.xml.NamespaceContextImpl;
import de.wwu.scdh.teilsp.xml.TEINamespaceContext;
import de.wwu.scdh.teilsp.xpath.XPathUtil;


/**
 * {@link LabelledEntriesFromXMLByPrefixDef} is a plugin for reading a list of
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

    private static final ArgumentDescriptor<String> ARGUMENT_PREFIX =
	new ArgumentDescriptorImpl<String>
	(String.class, "prefixDef",
	 "The XPath to the <prefixDef> element in the currently edited file.");

    private static final ArgumentDescriptor<String> ARGUMENT_PREFIX_REF =
	new ArgumentDescriptorImpl<String>
	(String.class, "prefixDefHref",
	 "The XPath to the <prefixDef> element in the currently edited file.",
	 DEFAULT_REF_XPATH);

    private static final ArgumentDescriptor<String> ARGUMENT_SELECTION =
	new ArgumentDescriptorImpl<String>
	(String.class, "selection",
	 "The XPath expression to use for finding selection values."
	 + " This should regard the structure of the referred XML document.",
	 "//*[@xml:id]");

    private static final ArgumentDescriptor<String> ARGUMENT_KEY =
	new ArgumentDescriptorImpl<String>
	(String.class, "key",
	 "The XPath expression to use for generating key values of the selection items."
	 + " This should regard the structure of the referred XML document."
	 + " Default: @xml:id",
	 "@xml:id");

    private static final ArgumentDescriptor<String> ARGUMENT_LABEL =
	new ArgumentDescriptorImpl<String>
	(String.class, "label",
	 "The XPath expression to use for generating the labels of the selection items."
	 + " This should regard the structure of the referred XML document.",
	 "self::*");

    private static final ArgumentDescriptor<? extends NamespaceContext> ARGUMENT_NAMESPACE =
	new ArgumentDescriptorImpl<NamespaceContextImpl>
	(NamespaceContextImpl.class, "namespace",
	 "A space-separated list of prefix:namespace-name tuples for"
	 + " use in the XPath expressions for accessing the target documents."
	 + " This should regard the structure of the referred XML document.",
	 new TEINamespaceContext());

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
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
    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    private String prefixDefXPath;
    private String hrefXPath;

    public void init(Map<String, String> args)
	throws ConfigurationException {

	prefixDefXPath = ARGUMENT_PREFIX.getValue(args);
	hrefXPath = ARGUMENT_PREFIX_REF.getValue(args);
	namespaceDecl = ARGUMENT_NAMESPACE.getValue(args);
	selectionXPath = ARGUMENT_SELECTION.getValue(args);
	keyXPath = ARGUMENT_KEY.getValue(args);
	labelXPath = ARGUMENT_LABEL.getValue(args);

	arguments = args;
    }

    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document currentDoc,
	 String systemId,
	 String context)
	throws ExtensionException {

	// get url from prefixDef
	String ident = null;
	String href = null;
	//String systemId = null;
	try {
	    // prepare the XPath query, using Saxon here for XPath 2.0
	    XPath xpath = XPathUtil.makeXPath(currentDoc);
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

	String urlString = href;
	if (href == null) {
	    throw new ExtensionException("Error: No URL from prefix definition");
	} else {
	    try {
		if (uriResolver != null) {
		    LOGGER.debug("Resolving URL in prefixDef \"{}\" given in {}", href, systemId);
		    urlString = uriResolver.resolve(href, systemId.toString()).getSystemId();
		    LOGGER.debug("Resolved URL in prefixDef \"{}\" given in {} to {}", href, systemId, urlString);
		}

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
		throw new ExtensionException("Error opening URL " + arguments.get("url") + "\n" + e);
	    } catch (TransformerException e) {
		throw new ExtensionException(e);
	    } catch (ParserConfigurationException e) {
		throw new ExtensionException(e);
	    } catch (SAXException e) {
		throw new ExtensionException(e);
	    } catch (IOException e) {
		throw new ExtensionException(e);
	    } catch (NullPointerException e) {
		throw new ExtensionException("Error opening URL " + urlString + "\n" + e);
	    }
	}

	prefix = ident + ":";

    }

    public Map<String, String> getArguments() {
	return arguments;
    }

}

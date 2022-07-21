package de.wwu.scdh.teilsp.extensions;

import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.*;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.om.NodeInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.teilsp.services.extensions.ExtensionException;

/**
 * A provider for labelled entries that produces its collection of
 * entries through XSLT.
 *
 * <P>Stylesheet parameters are taken from the configuration and the
 * current editing context is also passed into the stylesheet as
 * parameters: the document node is passed in as parameter 'document'
 * and the XPath expression identifying the currently edited node is
 * passed in as parameter 'context'.
 */
public class LabelledEntriesXSLTWithContext
    extends LabelledEntriesXSLT {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelledEntriesXSLTWithContext.class);

    /**
     * The name of the stylesheet parameter via which the document is
     * passed in. Be aware of the Type: documnet()
     */
    public static final QName PARAMETER_DOCUMENT = new QName("document");

    /**
     * The name of the stylesheet parameter via which the editing
     * context (XPath expression) is passed in.
     */
    public static final QName PARAMETER_CONTEXT = new QName("context");


    @Override
    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document document,
	 String systemId,
	 String context) throws ExtensionException {
	super.setup(uriResolver, entityResolver, document, systemId, context);

	// wrap org.w3c document into saxon NodeInfo
	// see https://www.saxonica.com/html/documentation11/sourcedocs/thirdparty.html
	DocumentWrapper doc = new DocumentWrapper(document, systemId, saxonConfig);
	NodeInfo rootNode = doc.getRootNode();
	XdmNode docNode = new XdmNode(rootNode);

	// pass document node and context as parameter into stylesheet
	xdmParameters.put(PARAMETER_DOCUMENT, docNode);
	xdmParameters.put(PARAMETER_CONTEXT, new XdmAtomicValue(context));
    }

}

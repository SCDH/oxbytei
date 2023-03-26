package de.wwu.scdh.oxbytei.xpath;

import java.net.URL;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.dom.DOMNodeWrapper;
import net.sf.saxon.value.EmptySequence;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.oxbytei.OxbyteiConstants;

/**
 * This class defines a new XPath function
 *
 * <code>obt:current-element(url as xs:string) as node()*</code><P/>
 *
 * If the document given by URL is opened in the editor, the element
 * the caret is located in, is returned. If the caret is on the
 * prolog, the root element is returned.<P/>
 *
 * This function is handy if you want to transform only a part of the
 * currently edited document.
 */
public class CurrentElement extends ExtensionFunctionDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentElement.class);

    public final String NAME = "current-element";

    /**
     * {@inheritDoc}
     */
    @Override
    public final StructuredQName getFunctionQName() {
        return new StructuredQName(OxbyteiConstants.XPATH_EXTENSION_PREFIX,
                                   OxbyteiConstants.XPATH_EXTENSION_NAMESPACE,
                                   NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SequenceType[] getArgumentTypes() {
        return new SequenceType[] {
            SequenceType.SINGLE_STRING
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.NODE_SEQUENCE;
    }

    /**
     * This function has side effects: It asks the editor state and pulls in a document.
     */
    @Override
    public final boolean hasSideEffects() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionFunctionCall makeCallExpression() {
	return new ExtensionFunctionCall() {
	    @Override
            public Sequence call(XPathContext context, Sequence[] arguments)
		throws XPathException {

		// get the URL of document from the arguments
		URL url = XPathUtils.getUrlArgument(arguments[0]);
		LOGGER.debug("getting current element on {}", url.toString());

		try {
		    // DOM objects we need to store
		    Element elementNode;
		    Document documentNode;

		    // get access to oxygen editor workspace
		    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

		    // we first need to get access to the editor and the editor page
		    WSEditor wsEditor = pluginWorkspace.getEditorAccess(url, PluginWorkspace.MAIN_EDITING_AREA);
		    if (wsEditor == null) {
			// the document is not edited or something else prevents editor access
			LOGGER.error("no editor access to {}", url.toString());
			return EmptySequence.getInstance();
		    }
		    WSEditorPage editorPage = wsEditor.getCurrentPage();

		    // now we can do mode specific things: getting the
		    // offset and the document controller
		    if (WSXMLTextEditorPage.class.isAssignableFrom(editorPage.getClass())) {
			// the document is edited in text mode
			WSXMLTextEditorPage page = (WSXMLTextEditorPage) editorPage;
			LOGGER.debug("document is opened in text mode, caret offset {}", page.getCaretOffset());
			// use the evaluateXPath method of the editor page
			Object[] xpathDocument = page.evaluateXPath("root()");
			Object[] xpathElement = page.evaluateXPath("ancestor-or-self::*[1]");
			documentNode = (Document) xpathDocument[0];
			elementNode = getElementFromXPathResult(xpathElement, documentNode);
		    } else if (WSAuthorEditorPage.class.isAssignableFrom(editorPage.getClass())) {
			// the document is edited in author mode
			WSAuthorEditorPage page = (WSAuthorEditorPage) editorPage;
			LOGGER.debug("document is opened in author mode, caret offset {}", page.getCaretOffset());
			// use the evaluateXPath method of the document controller
			AuthorDocumentController documentController = page.getDocumentController();
			Object[] xpathDocument = documentController.evaluateXPath("root()", true, true, true);
			Object[] xpathElement = documentController.evaluateXPath("ancestor-or-self::*[1]", true, true, true);
			documentNode = (Document) xpathDocument[0];
			elementNode = getElementFromXPathResult(xpathElement, documentNode);
		    } else {
			// the document is edited in an other mode (grid)
			LOGGER.error("no access to current editor location");
			throw new XPathException("No access to the current editor location in this editing mode. Try text mode or author mode.");
		    }

		    LOGGER.debug("root: {}, current element: {}",
				 getRootElement(documentNode).getLocalName(),
				 elementNode.getLocalName());

		    // wrap DOM nodes into Saxon's node types
		    DocumentWrapper docWrapper = new DocumentWrapper(documentNode, url.toString(), context.getConfiguration());
		    DOMNodeWrapper elementWrapper = docWrapper.wrap(elementNode);

		    // materialize() returns a GroundedValue, i.e. a Sequence
		    return elementWrapper.materialize();
		} catch (IndexOutOfBoundsException e) {
		    LOGGER.error(e.getMessage());
		    throw new XPathException(e);
		} catch (ro.sync.exml.workspace.api.editor.page.text.xml.XPathException e) {
		    LOGGER.error(e.getMessage());
		    throw new XPathException(e);
		} catch (AuthorOperationException e) {
		    LOGGER.error(e.getMessage());
		    throw new XPathException(e);
		}
	    }
	};
    }

    private Element getElementFromXPathResult(Object[] result, Document document) throws XPathException {
	if (result.length > 0) {
	    // we have an element in the result sequence
	    return (Element) result[0];
	} else {
	    Element root = getRootElement(document);
	    if (root != null) {
		return root;
	    } else {
		LOGGER.error("no element found");
		throw new XPathException("no element found");
	    }
	}
    }

    private Element getRootElement(Document document) {
	NodeList nodes = document.getChildNodes();
	for (int i = 0; i < nodes.getLength(); i++) {
	    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
		return (Element) nodes.item(i);
	    }
	}
	LOGGER.debug("no root element found in ", document.getBaseURI());
	return null;
    }
}

package de.wwu.scdh.oxbytei.xpath;

import java.net.URL;
import java.io.IOException;

import javax.swing.text.BadLocationException;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.ecss.extensions.api.node.AuthorDocumentProvider;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.WSTextBasedEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorDocument;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.oxbytei.OxbyteiConstants;


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
		URL url = XPathUtils.getUrlArgument(arguments[0]);
		LOGGER.error("getting current element on {}", url.toString());

		// get access to oxygen editor workspace
		PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

		try {
		    // the current caret position is accessible
		    // through WSTextBasedEditorPage.getCaretOffset()
		    // but it may differ in text or author mode
		    int offset;
		    AuthorDocumentController documentController;
		    
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
			offset = ((WSXMLTextEditorPage) editorPage).getCaretOffset();
			LOGGER.error("document is opened in text mode, caret offset {}", offset);
			// expand=flase seems to result in a document without text nodes and PIs
			// expand=true 
			AuthorDocumentProvider documentProvider = pluginWorkspace.createAuthorDocumentProvider(url, null, true);
			documentController = documentProvider.getAuthorDocumentController();
		    } else if (WSAuthorEditorPage.class.isAssignableFrom(editorPage.getClass())) {
			// the document is edited in author mode
			offset = ((WSAuthorEditorPage) editorPage).getCaretOffset();
			LOGGER.error("document is opened in author mode, caret offset {}", offset);
			documentController = ((WSAuthorEditorPage) editorPage).getDocumentController();
		    } else {
			// the document is edited in an other mode (grid)
			LOGGER.error("no access to editor offset");
			throw new XPathException("no access to editor offset");
		    }

		    // the node at the caret position is accessible through AuthorDocumentController.getNodeAtOffset()
		    AuthorNode currentNode = documentController.getNodeAtOffset(offset);

		    // get the current element node
		    AuthorElement currentElement;
		    if (currentNode.getType() == AuthorNode.NODE_TYPE_DOCUMENT) {
			// the caret is on the prolog, the root element or the epilog
			LOGGER.error("on document node");
			currentElement = ((AuthorDocument) currentNode).getRootElement();
		    } else if (currentNode.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			// we are on an element already
			LOGGER.error("on element node");
			currentElement = (AuthorElement) currentNode;
		    } else {
			// walk the ancestor axis up to the first element
			LOGGER.error("in tree");
			while (!(currentNode.getType() == AuthorNode.NODE_TYPE_ELEMENT || currentNode.getType() == AuthorNode.NODE_TYPE_DOCUMENT)) {
			    currentNode = currentNode.getParent();
			}
			if (currentNode.getType() == AuthorNode.NODE_TYPE_DOCUMENT) {
			    currentElement = ((AuthorDocument) currentNode).getRootElement();
			} else {
			    currentElement = (AuthorElement) currentNode;
			}
		    }
		    AuthorDocument authorDocument = currentNode.getOwnerDocument();
		    LOGGER.error("root: {}, current element: {}",
				 authorDocument.getRootElement().getName(),
				 currentElement.getName());

		    // cast to DOM nodes
		    Element element = (Element) currentElement;
		    Document documentNode = (Document) authorDocument;

		    // wrap DOM nodes into Saxon's node types
		    DocumentWrapper docWrapper = new DocumentWrapper(documentNode, url.toString(), context.getConfiguration());
		    DOMNodeWrapper elementWrapper = docWrapper.wrap(element);

		    // materialize() returns a GroundedValue, i.e. a Sequence
		    return elementWrapper.materialize();

		} catch (IOException e) {
		    LOGGER.error(e.getMessage());
		    throw new XPathException(e);
		} catch (BadLocationException e) {
		    LOGGER.error(e.getMessage());
		    throw new XPathException(e);
		}
	    }
	};
    }
}

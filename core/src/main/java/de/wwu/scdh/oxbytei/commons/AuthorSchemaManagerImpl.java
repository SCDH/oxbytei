/*
 * A schema manager for the workspace that works in either editing mode.
 */

package de.wwu.scdh.oxbytei.commons;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorSchemaManager;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.contentcompletion.xml.WhatAttributesCanGoHereContext;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;
import ro.sync.contentcompletion.xml.CIAttribute;

/**
 * A schema manager for author mode.
 *
 * @author Christian Lück
 */
public class AuthorSchemaManagerImpl implements WSSchemaManager {

    protected AuthorAccess authorAccess;
    protected WSDocumentReader documentReader;

    public AuthorSchemaManagerImpl(AuthorAccess authorAccess) {
	this.authorAccess = authorAccess;
	this.documentReader = new AuthorDocumentReader(authorAccess);
    }

    public List<CIAttribute> whatAttributesCanGoHere(WhatAttributesCanGoHereContext context) {
	AuthorSchemaManager manager = authorAccess.getDocumentController().getAuthorSchemaManager();
	return manager.whatAttributesCanGoHere(context);
    }

    public List<CIAttribute> whatAttributesCanGoHere(int offset) {
	AuthorSchemaManager manager = authorAccess.getDocumentController().getAuthorSchemaManager();
	try {
	    AuthorNode node = authorAccess.getDocumentController().getNodeAtOffset(offset);
	    WhatAttributesCanGoHereContext context = manager.createWhatAttributesCanGoHereContext((AuthorElement) node);
	    return manager.whatAttributesCanGoHere(context);
	} catch (BadLocationException e) {
	    return new ArrayList<CIAttribute>(); // empty list, better null?
	}
    }

    public List<CIAttribute> whatAttributesCanGoHere() {
	int offset = documentReader.getCaretOffset();
	return whatAttributesCanGoHere(offset);
    }

    public List<CIAttribute> whatAttributesCanGoHere(String locationXPath) {
	AuthorSchemaManager manager = authorAccess.getDocumentController().getAuthorSchemaManager();
	try {
	    AuthorNode[] node = authorAccess.getDocumentController().findNodesByXPath(locationXPath, false, true, true, true);
	    WhatAttributesCanGoHereContext context = manager.createWhatAttributesCanGoHereContext((AuthorElement) node[0]);
	    return manager.whatAttributesCanGoHere(context);
	} catch (AuthorOperationException e) {
	    return new ArrayList<CIAttribute>();
	} catch (IndexOutOfBoundsException e) {
	    return new ArrayList<CIAttribute>();
	}
    }

    public List<CIAttribute> whatAttributesAreHere(int offset) {
	AuthorElement contextNode;
	try {
	    contextNode = (AuthorElement) authorAccess.getDocumentController().getNodeAtOffset(offset);
	} catch (BadLocationException e) {
	    return new ArrayList<CIAttribute>();
	}
	return whatAttributesAreHere(contextNode);
    }

    public List<CIAttribute> whatAttributesAreHere() {
	int offset = documentReader.getCaretOffset();
	return whatAttributesAreHere(offset);
    }

    public List<CIAttribute> whatAttributesAreHere(String locationXPath) {
	AuthorElement contextNode;
	try {
	    AuthorNode[] node =
		authorAccess.getDocumentController().findNodesByXPath(locationXPath, false, true, true, true);
	    contextNode = (AuthorElement) node[0];
	} catch (IndexOutOfBoundsException e) {
	    return new ArrayList<CIAttribute>();
	} catch (AuthorOperationException e) {
	    return new ArrayList<CIAttribute>();
	}
	return whatAttributesAreHere(contextNode);
    }

    protected List<CIAttribute> whatAttributesAreHere(AuthorElement contextNode) {
	AuthorSchemaManager manager = authorAccess.getDocumentController().getAuthorSchemaManager();
	List<CIAttribute> attributes = new ArrayList<CIAttribute>();
	for (int i = 0; i < contextNode.getAttributesCount(); i++) {
	    String attributeName = contextNode.getAttributeAtIndex(i);

	    WhatPossibleValuesHasAttributeContext values =
		manager.createWhatPossibleValuesHasAttributeContext(contextNode, attributeName);
	    CIAttribute attribute = manager.getAttributeDescription(values);
	    attributes.add(attribute);
	}
	return attributes;
    }

}

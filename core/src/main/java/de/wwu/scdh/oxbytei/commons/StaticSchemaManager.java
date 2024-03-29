/*
 * A schema manager for the workspace that works in either editing mode.
 */

package de.wwu.scdh.oxbytei.commons;

import java.util.List;
import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorSchemaManager;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.contentcompletion.xml.WhatAttributesCanGoHereContext;
import ro.sync.contentcompletion.xml.CIAttribute;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;
import ro.sync.contentcompletion.xml.CIValue;

/**
 * A schema manager for the workspace that works in either editing mode.
 *
 * @author Christian Lück
 */
public class StaticSchemaManager {

    public static WSEditorPage getWsEditorPage() {
	// we can get a schema manager either in text mode or in author mode
	PluginWorkspace ws = PluginWorkspaceProvider.getPluginWorkspace();
	WSEditorPage page = ws.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA).getCurrentPage();
	return page;
    }

    private static int getCaretOffset(WSEditorPage page) {
	if (WSXMLTextEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in text mode
	    return ((WSXMLTextEditorPage) page).getCaretOffset();
	} else if (WSAuthorEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in author mode
	    return ((WSAuthorEditorPage) page).getCaretOffset();
	} else {
	    // we are in grid mode or so
	    return 0; // FIXME return null
	}
    }

    public static int getCaretOffset() {
	WSEditorPage page = getWsEditorPage();
	return getCaretOffset(page);
    }

    private static List<CIAttribute> whatAttributesCanGoHere(WhatAttributesCanGoHereContext context, WSEditorPage page) {
	List<CIAttribute> attributes;
	if (WSXMLTextEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in text mode
	    WSTextXMLSchemaManager manager = ((WSXMLTextEditorPage) page).getXMLSchemaManager();
	    attributes = manager.whatAttributesCanGoHere(context);
	} else if (WSAuthorEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in author mode
	    AuthorSchemaManager manager = ((WSAuthorEditorPage) page).getDocumentController().getAuthorSchemaManager();
	    attributes = manager.whatAttributesCanGoHere(context);
	} else {
	    // we are in grid mode or so
	    attributes = null; // better empty list?
	}
	return attributes;
    }

    public static List<CIAttribute> whatAttributesCanGoHere(WhatAttributesCanGoHereContext context) {
	WSEditorPage page = getWsEditorPage();
	return whatAttributesCanGoHere(context, page);
    }

    public static List<CIAttribute> whatAttributesCanGoHere() {
	WSEditorPage page = getWsEditorPage();
	int offset = getCaretOffset(page);
	WhatAttributesCanGoHereContext ctx = createAttributesCanGoHereContext(offset, page);
	return whatAttributesCanGoHere(ctx, page);
    }

    private static AuthorElement getAuthorElement(WSAuthorEditorPage page, int offset)
	throws BadLocationException {
	AuthorNode node = page.getDocumentController().getNodeAtOffset(offset);
	return (AuthorElement) node;
    }

    private static WhatAttributesCanGoHereContext createAttributesCanGoHereContext(int offset, WSEditorPage page) {
	WhatAttributesCanGoHereContext ctx;
	if (WSXMLTextEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in text mode
	    WSTextXMLSchemaManager manager = ((WSXMLTextEditorPage) page).getXMLSchemaManager();
	    ctx = manager.createWhatAttributesCanGoHereContext(offset);
	} else if (WSAuthorEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in author mode
	    try {
		// TODO: maybe simpler to perform xpath ancestor-or-self::*[1]
		WSAuthorEditorPage auPage = (WSAuthorEditorPage) page;
		AuthorSchemaManager manager = auPage.getDocumentController().getAuthorSchemaManager();
		ctx = manager.createWhatAttributesCanGoHereContext(getAuthorElement(auPage, offset));
	    } catch (BadLocationException e) {
		ctx = null;
	    }
	} else {
	    // we are in grid mode or so
	    ctx = null; // better empty list?
	}
	return ctx;
    }

    public static WhatAttributesCanGoHereContext createAttributesCanGoHereContext(int offset) {
	WSEditorPage page = getWsEditorPage();
	return createAttributesCanGoHereContext(offset, page);
    }

    private static List<CIValue> whatPossibleValuesHasAttribute
	(WSEditorPage page, WhatPossibleValuesHasAttributeContext context) {
	List<CIValue> values; // = new ArrayList<CIValue>();
	if (WSXMLTextEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in text mode
	    WSTextXMLSchemaManager manager = ((WSXMLTextEditorPage) page).getXMLSchemaManager();
	    values = manager.whatPossibleValuesHasAttribute(context);
	} else if (WSAuthorEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in author mode
	    AuthorSchemaManager manager = ((WSAuthorEditorPage) page).getDocumentController().getAuthorSchemaManager();
	    // this would loop when OxbyteiSchemaManagerFilter is active!
	    values = manager.whatPossibleValuesHasAttribute(context);

	} else {
	    // we are in grid mode or so
	    values = null; // better empty list?
	}
	return values;
    }

    private static WhatPossibleValuesHasAttributeContext createWhatPossibleValuesHasAttributeContext
	(int offset, String attributeName, WSEditorPage page) {
	WhatPossibleValuesHasAttributeContext ctx;
	if (WSXMLTextEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in text mode
	    // the attribute name is not used here. It seems to be determined by the offset.
	    WSTextXMLSchemaManager manager = ((WSXMLTextEditorPage) page).getXMLSchemaManager();
	    ctx = manager.createWhatPossibleValuesHasAttributeContext(offset);
	} else if (WSAuthorEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in author mode
	    try {
		// TODO: maybe simpler to perform xpath ancestor-or-self::*[1]
		WSAuthorEditorPage auPage = (WSAuthorEditorPage) page;
		AuthorSchemaManager manager = auPage.getDocumentController().getAuthorSchemaManager();
		ctx = manager.createWhatPossibleValuesHasAttributeContext(getAuthorElement(auPage, offset), attributeName);
	    } catch (BadLocationException e) {
		ctx = null;
	    }
	} else {
	    // we are in grid mode or so
	    ctx = null; // better empty list?
	}
	return ctx;
    }

    /**
     * Returns a list of possible attribute values
     *
     * @param offset  the offset of the attribute in text mode, of the elemen in author mode
     * @param attributeName in author mode the name of the attribute,
     * not used in text mode, because the attribute is reached by the
     * caret offset
     *
     */
    public static List<CIValue> whatPossibleValuesHasAttribute(int offset, String attributeName) {
	WSEditorPage page = getWsEditorPage();
	WhatPossibleValuesHasAttributeContext ctx =
	    createWhatPossibleValuesHasAttributeContext(offset, attributeName, page);
	return whatPossibleValuesHasAttribute(page, ctx);
    }

    /**
     * Returns a list of possible attribute values
     *
     * @param attributeName in author mode the name of the attribute,
     * not used in text mode, because the attribute is determined by the
     * caret position
     *
     */
    public static List<CIValue> whatPossibleValuesHasAttribute(String attributeName) {
	WSEditorPage page = getWsEditorPage();
	int offset = getCaretOffset(page);
	WhatPossibleValuesHasAttributeContext ctx =
	    createWhatPossibleValuesHasAttributeContext(offset, attributeName, page);
	return whatPossibleValuesHasAttribute(page, ctx);
    }

}

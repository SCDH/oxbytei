package de.wwu.scdh.oxbytei.commons;

import java.net.URL;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.util.UtilAccess;
import ro.sync.ecss.extensions.api.AuthorAccess;

import de.wwu.scdh.teilsp.config.EditorVariablesExpander;


public class EditorVariablesExpanderImpl implements EditorVariablesExpander {

    private UtilAccess utilAccess;

    private URL currentEditedURL;

    private boolean expandAsk;

    public EditorVariablesExpanderImpl(AuthorAccess authorAccess, URL currentEditedURL, boolean expandAsk) {
	this.utilAccess = authorAccess.getUtilAccess();
	this.currentEditedURL = currentEditedURL;
	this.expandAsk = expandAsk;
    }

    public EditorVariablesExpanderImpl(URL currentEditedURL, boolean expandAsk) {
	utilAccess = PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess();
	this.expandAsk = expandAsk;
    }

    @Override
    public String expand(String unexpanded) {
	String expanded = utilAccess.expandEditorVariables(unexpanded, currentEditedURL, expandAsk);
	// expand as long as possible
	while (! expanded.equals(unexpanded)) {
	    unexpanded = expanded;
	    expanded = utilAccess.expandEditorVariables(unexpanded, currentEditedURL, expandAsk);
	}
	return expanded;
    }

}

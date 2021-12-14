package de.wwu.scdh.oxbytei.commons;

import java.net.MalformedURLException;
import java.net.URL;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.workspace.api.editor.WSEditorBase;

import de.wwu.scdh.teilsp.completion.PrefixDef;

public class Resolver {

    public static String cfdu(AuthorAccess authorAccess)
	throws MalformedURLException, NullPointerException {
	// get current file url from author access
	URL cfu = authorAccess.getEditorAccess().getEditorLocation();
	// delete file from current file url
	System.err.println("cfu: " + cfu.toString());
	String path = cfu.getPath();
	String[] parts = path.split("/");
	path = "";
	int i;
	for (i = 0; i < parts.length - 1; i++) {
	    path += parts[i] + "/";
	}
	return path;
    }
    
    public static String resolve(AuthorAccess authorAccess, String link)
	throws MalformedURLException, NullPointerException {
	if (link.startsWith("/") || link.startsWith("~/")) {
	    // we have an absolute file path 
	    return "file:" + link;
	} else if (link.matches("^[a-zA-Z]+[a-zA-Z0-9_-]*:.*")) {
	    // we have some kind of URI
	    return link;
	} else {
	    // file relative to the currently edited document
	    return cfdu(authorAccess) + link;
	}
    }

    public static String resolve(AuthorAccess authorAccess, PrefixDef prefixDef)
	throws MalformedURLException, NullPointerException {
	// FIXME: make conform TEI's abstract definition
	String link = prefixDef.getReplacementPattern().split("#\\$")[0];
	return resolve(authorAccess, link);
    }

}

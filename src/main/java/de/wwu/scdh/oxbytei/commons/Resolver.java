package de.wwu.scdh.oxbytei.commons;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import javax.xml.transform.TransformerException;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.workspace.api.editor.WSEditorBase;

import de.wwu.scdh.teilsp.tei.PrefixDef;

public class Resolver {

    public static String cfdu(AuthorAccess authorAccess)
	throws MalformedURLException, URISyntaxException {
	// get current file url from author access
	URL cfu = authorAccess.getEditorAccess().getEditorLocation();
	// strip the file name
	String directory = Paths.get(cfu.toURI()).getParent().toString();
	return directory;
    }
    
    public static String resolveOFF(AuthorAccess authorAccess, String link)
	throws MalformedURLException, NullPointerException, URISyntaxException {
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

    public static String resolve(AuthorAccess authorAccess, String reference)
	throws MalformedURLException, TransformerException, URISyntaxException {
	return authorAccess.getXMLUtilAccess().getURIResolver().resolve(reference, cfdu(authorAccess)).getSystemId();
    }

    public static String resolve(AuthorAccess authorAccess, PrefixDef prefixDef)
	throws MalformedURLException, NullPointerException, TransformerException, URISyntaxException {
	return resolve(authorAccess, extractReference(prefixDef));
    }

    public static String extractReference(PrefixDef prefixDef)
	throws NullPointerException {
	// FIXME: make conform TEI's abstract definition and replace
	// with pluging
	return prefixDef.getReplacementPattern().split("#\\$")[0];
    }
    
}

package de.wwu.scdh.oxbytei.commons;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import ro.sync.ecss.extensions.api.AuthorAccess;


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

    public static String resolve(AuthorAccess authorAccess, String reference)
	throws MalformedURLException, TransformerException {
	String baseURL = authorAccess.getEditorAccess().getEditorLocation().toString();
	return authorAccess.getXMLUtilAccess().getURIResolver().resolve(reference, baseURL).getSystemId();
    }

    public static String resolve(AuthorAccess authorAccess, PrefixDef prefixDef)
	throws MalformedURLException, NullPointerException, TransformerException {
	return resolve(authorAccess, extractReference(prefixDef));
    }

    public static String resolve(URIResolver resolver, String defaultLocation, PrefixDef prefixDef)
	throws MalformedURLException, NullPointerException, TransformerException {
	return resolver.resolve(extractReference(prefixDef), defaultLocation).getSystemId();
    }

    public static String extractReference(PrefixDef prefixDef)
	throws NullPointerException {
	// FIXME: make conform TEI's abstract definition and replace
	// with pluging
	return prefixDef.getReplacementPattern().split("#\\$")[0];
    }
    
}

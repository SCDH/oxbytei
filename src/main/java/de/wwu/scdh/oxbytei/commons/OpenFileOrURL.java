package de.wwu.scdh.oxbytei.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.workspace.api.editor.WSEditorBase;

import de.wwu.scdh.teilsp.tei.PrefixDef;
import de.wwu.scdh.teilsp.exceptions.DocumentReaderException;

public class OpenFileOrURL {

    /**
     * The URL to an an other document.
     * 
     * We do not know whether it is relative or absolute, valid or
     * invalid.
     */
    private String link;

    private String currentBaseURL;
    
    public OpenFileOrURL(PrefixDef prefixDef, AuthorAccess authorAccess)
    throws DocumentReaderException {
	// get URL from prefixDef/@replacementPattern
	try {
	    // FIXME: make conform TEI's abstract definition
	    this.link = prefixDef.getReplacementPattern().split("#\\$")[0];
	} catch (Exception e) {
	    throw new DocumentReaderException(e);
	}
	
	// delete file from current file url
	URL cfu = authorAccess.getEditorAccess().getEditorLocation();
	System.err.println("cfu: " + cfu.toString());
	String path = cfu.getPath();
	String[] parts = path.split("/");
	path = "";
	int i;
	for (i = 0; i < parts.length - 1; i++) {
	    path += parts[i] + "/";
	}
	System.err.println("cfu: new path: " + path);
	System.err.println("cfu: host: " + cfu.getHost());
	try {
	    this.currentBaseURL = new URL(cfu.getProtocol(), cfu.getHost(), cfu.getPort(), path).toString();
	} catch (MalformedURLException e) {
	    this.currentBaseURL = null;
	    System.err.println("cfu: failed to make new url!");
	}
    }

    public InputStream open() throws
	IOException, MalformedURLException {
	URL theURL;
	URLConnection urlConnection;
	FileInputStream file;
	try {
	    theURL = new URL(this.link);
	    urlConnection = theURL.openConnection();
	    return urlConnection.getInputStream();
	} catch (MalformedURLException e) {
	} catch (IOException e) {
	}
	try {
	    file = new FileInputStream(this.link);
	    return file;
	} catch (FileNotFoundException e) {
	} catch (SecurityException e) {
	}
	try {
	    file = new FileInputStream(this.currentBaseURL + this.link);
	    return file;
	} catch (FileNotFoundException e) {
	} catch (SecurityException e) {
	}
	theURL = new URL(this.currentBaseURL + this.link);
	urlConnection = theURL.openConnection();
	return urlConnection.getInputStream();
    }
}

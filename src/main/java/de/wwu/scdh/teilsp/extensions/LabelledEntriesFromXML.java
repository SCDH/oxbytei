package de.wwu.scdh.teilsp.extensions;

import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;


import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.exceptions.DocumentReaderException;


public class LabelledEntriesFromXML
    implements ILabelledEntriesProvider {

    public ArrayList<LabelledEntry> getLabelledEntries(Map<String, String> args)
	throws ExtensionException {

	InputStream inputStream;
	String uriString = args.get("uri");
	if (uriString == null) {
	    throw new ExtensionException("Argument 'uri' is required");
	}
	try {
            URL theURL = new URL(uriString);
            URLConnection urlConnection = theURL.openConnection();
            inputStream = urlConnection.getInputStream();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        try {
            inputStream = new FileInputStream(uriString);
        } catch (FileNotFoundException e) {
	    throw new ExtensionException("URL or file not found: " + uriString);
        } catch (SecurityException e) {
	    throw new ExtensionException(e);
        }
	// we now have a uri

	String selection = args.get("selection");
	if (selection == null) {
	    try { inputStream.close(); } catch (IOException e) {}
	    throw new ExtensionException("Argument 'selection' is required");
	}
	String key = args.get("key");
	if (key == null) {
	    try { inputStream.close(); } catch (IOException e) {}
	    throw new ExtensionException("Argument 'key' is required");
	}
	String label = args.get("label");
	if (label == null) {
	    try { inputStream.close(); } catch (IOException e) {}
	    throw new ExtensionException("Argument 'label' is required");
	}

	ArrayList<LabelledEntry> entries = null;

	try {
	    LabelledEntriesFromXMLReader reader =
		new LabelledEntriesFromXMLReader(args.get("prefix"),
						 inputStream,
						 selection, key, label,
						 args.get("namespaces"));
	    entries = new ArrayList<LabelledEntry>(Arrays.asList(reader.getEntries()));
	} catch (DocumentReaderException e) {
	    try { inputStream.close(); } catch (IOException err) {}
	    throw new ExtensionException(e);
	}

	try {
	    inputStream.close();
	} catch (IOException e) {}

	return entries;
    }

}

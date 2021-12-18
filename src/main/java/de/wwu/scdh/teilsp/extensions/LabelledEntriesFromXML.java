package de.wwu.scdh.teilsp.extensions;

import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.exceptions.DocumentReaderException;


public class LabelledEntriesFromXML
    implements ILabelledEntriesProvider {

    private ArrayList<LabelledEntry> entries;

    private Map<String, String> arguments;

    private static final ArgumentDescriptor ARGUMENT_URI =
	new ArgumentDescriptor("systeID",
			       ArgumentDescriptor.TYPE_STRING,
			       "The URI pointing to the referatory.");

    private static final ArgumentDescriptor ARGUMENT_PREFIX =
	new ArgumentDescriptor("prefix",
			       ArgumentDescriptor.TYPE_STRING,
			       "The prefix of the URI scheme given in prefixDef/@ident.");

    private static final ArgumentDescriptor ARGUMENT_SELECTION =
	new ArgumentDescriptor("selection",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression to use for finding selection values."
			       + " This should regard the structure of the referred XML document.",
			       "//*[@xml:id]");

    private static final ArgumentDescriptor ARGUMENT_KEY =
	new ArgumentDescriptor("key",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression to use for generating key values of the selection items."
			       + " This should regard the structure of the referred XML document."
			       + " Default: @xml:id",
			       "@xml:id");

    private static final ArgumentDescriptor ARGUMENT_LABEL =
	new ArgumentDescriptor("label",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "The XPath expression to use for generating the labels of the selection items."
			       + " This should regard the structure of the referred XML document.",
			       "self::*");

    private static final ArgumentDescriptor ARGUMENT_NAMESPACE =
	new ArgumentDescriptor("namespace",
			       ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
			       "A space-separated list of prefix:namespace-name tuples for use in the XPath expressions for accessing the target documents."
			       + " This should regard the structure of the referred XML document.",
			       "t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
	ARGUMENT_URI,
	ARGUMENT_PREFIX,
	ARGUMENT_SELECTION,
	ARGUMENT_KEY,
	ARGUMENT_LABEL,
	ARGUMENT_NAMESPACE
	};

    /**
     * 
     */
    public ArgumentDescriptor[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    public void init(Map<String, String> args,
		     URIResolver uriResolver,
		     EntityResolver entityResolver,
		     URL systemId)
	throws ExtensionException {

	InputStream inputStream;
	String uriString = args.get("systemID");
	if (uriString == null) {
	    throw new ExtensionException("Argument 'uri' is required");
	}
	try {
            URL theURL = new URL(uriString);
            URLConnection urlConnection = theURL.openConnection();
            inputStream = urlConnection.getInputStream();
        } catch (MalformedURLException e) {
	    throw new ExtensionException("URL not found: " + uriString + "\n\n" + e);
	} catch (IOException e) {
	    throw new ExtensionException("URL not found: " + uriString + "\n\n" + e);
        }
	// we now have an input stream

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
	arguments = args;

	//entries = new ArrayList<LabelledEntry>();

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

    }

    public ArrayList<LabelledEntry> getLabelledEntries(String userInput)
	throws ExtensionException {
	return entries;
    }

    public Map<String, String> getArguments() {
	return arguments;
    }

}

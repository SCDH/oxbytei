package de.wwu.scdh.teilsp.extensions;

import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerException;

import org.xml.sax.EntityResolver;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.xml.NamespaceContextImpl;


/**
 * {@link LabelledEntriesFromXML} is a plugin for reading a list of
 * {@link de.wwu.scdh.teilsp.services.extensions.LabelledEntry}
 * objects from a given XML file.
 *
 */
public class LabelledEntriesFromXML
    extends LabelledEntriesFromXMLReader
    implements ILabelledEntriesProvider {

    public LabelledEntriesFromXML() {}

    private Map<String, String> arguments;

    private static final ArgumentDescriptor ARGUMENT_URL =
	new ArgumentDescriptor("url",
			       ArgumentDescriptor.TYPE_STRING,
			       "The URL pointing to the referatory."
			       + "\nIf not set, this defaults to the currently edited file.");

    private static final ArgumentDescriptor ARGUMENT_PREFIX =
	new ArgumentDescriptor("prefix",
			       ArgumentDescriptor.TYPE_STRING,
			       "The prefix of the returned keys.");

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
			       ArgumentDescriptor.TYPE_STRING,
			       "A space-separated list of prefix:namespace-name tuples for use in the XPath expressions for accessing the target documents."
			       + " This should regard the structure of the referred XML document.",
			       "t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
	ARGUMENT_URL,
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

	// The 'url' argument can be used to rewrite to another source
	// than the one given in systemId.
	if (args.get("url") == null) {
	    url = systemId;
	    System.out.println("Using URL " + systemId);
	} else {
	    try {
		String urlString = uriResolver.resolve(args.get("url"), systemId.toString()).getSystemId();
		url = new URL(urlString);
		System.err.println("Redirecting " + args.get("url") + " to " + urlString);
	    } catch (MalformedURLException e) {
		throw new ExtensionException("Error opening URL " + args.get("url") + "\n" + e);
	    } catch (TransformerException e) {
		throw new ExtensionException(e);
	    }
	}

	prefix = args.getOrDefault("prefix", "");

	selectionXPath = args.get("selection");
	if (selectionXPath == null) {
	    throw new ExtensionException("Argument 'selection' is required");
	}
	keyXPath = args.get("key");
	if (keyXPath == null) {
	    throw new ExtensionException("Argument 'key' is required");
	}
	labelXPath = args.get("label");
	if (labelXPath == null) {
	    throw new ExtensionException("Argument 'label' is required");
	}
	if (args.get("namespaces") != null) {
	    namespaceDecl = new NamespaceContextImpl(args.get("namespaces"));
	} else {
	    throw new ExtensionException("Argument 'namespaces' is required");
	}

	arguments = args;

    }

    public Map<String, String> getArguments() {
	return arguments;
    }

}

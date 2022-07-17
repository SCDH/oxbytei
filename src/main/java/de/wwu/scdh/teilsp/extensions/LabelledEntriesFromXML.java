package de.wwu.scdh.teilsp.extensions;

import java.util.Map;
import javax.xml.transform.URIResolver;
import javax.xml.namespace.NamespaceContext;

import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;
import de.wwu.scdh.teilsp.services.extensions.URLArgumentDescriptor;
import de.wwu.scdh.teilsp.xml.NamespaceContextImpl;
import de.wwu.scdh.teilsp.xml.TEINamespaceContext;


/**
 * {@link LabelledEntriesFromXML} is a plugin for reading a list of
 * {@link de.wwu.scdh.teilsp.services.extensions.LabelledEntry}
 * objects from a given XML file.
 *
 */
public class LabelledEntriesFromXML
    extends LabelledEntriesFromXMLReader
    implements ILabelledEntriesProvider {

    private Map<String, String> arguments;

    private static final URLArgumentDescriptor ARGUMENT_URL =
	new URLArgumentDescriptor
	("url",
	 "The URL pointing to the referatory."
	 + "\nIf not set, this defaults to the currently edited file.",
	 null);

    private static final ArgumentDescriptor<String> ARGUMENT_PREFIX =
	new ArgumentDescriptorImpl<String>
	(String.class, "prefix",
	 "The prefix of the returned keys.",
	 "");

    private static final ArgumentDescriptor<String> ARGUMENT_SELECTION =
	new ArgumentDescriptorImpl<String>
	(String.class, "selection",
	 "The XPath expression to use for finding selection values."
	 + " This should regard the structure of the referred XML document.",
	 "//*[@xml:id]");

    private static final ArgumentDescriptor<String> ARGUMENT_KEY =
	new ArgumentDescriptorImpl<String>
	(String.class, "key",
	 "The XPath expression to use for generating key values of the selection items."
	 + " This should regard the structure of the referred XML document."
	 + " Default: @xml:id",
	 "@xml:id");

    private static final ArgumentDescriptor<String> ARGUMENT_LABEL =
	new ArgumentDescriptorImpl<String>
	(String.class, "label",
	 "The XPath expression to use for generating the labels of the selection items."
	 + " This should regard the structure of the referred XML document.",
	 "self::*");

    private static final ArgumentDescriptor<? extends NamespaceContext> ARGUMENT_NAMESPACE =
	new ArgumentDescriptorImpl<NamespaceContextImpl>
	(NamespaceContextImpl.class, "namespace",
	 "A space-separated list of prefix:namespace-name tuples for"
	 + " use in the XPath expressions for accessing the target documents."
	 + " This should regard the structure of the referred XML document.",
	 new TEINamespaceContext());

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
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
    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    public void init(Map<String, String> args)
	throws ConfigurationException {

	prefix = ARGUMENT_PREFIX.getValue(args);
	selectionXPath = ARGUMENT_SELECTION.getValue(args);
	keyXPath = ARGUMENT_KEY.getValue(args);
	labelXPath = ARGUMENT_LABEL.getValue(args);
	namespaceDecl = ARGUMENT_NAMESPACE.getValue(args);

	arguments = args;
    }

    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document doc,
	 String systemId,
	 String context)
	throws ExtensionException {

	document = doc;
    }

    public Map<String, String> getArguments() {
	return arguments;
    }

}

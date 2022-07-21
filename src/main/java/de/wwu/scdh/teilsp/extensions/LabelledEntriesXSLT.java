package de.wwu.scdh.teilsp.extensions;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;
import de.wwu.scdh.teilsp.services.extensions.BooleanArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.URLArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentsExtractor;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;

/**
 * A provider for labelled entries that produces its collection of
 * entries through XSLT.
 *
 * <P>Only stylesheet parameters are taken from the
 * configuration. Variables from the current editing context or the
 * editor state will not be considered. This static setup makes things
 * faster and makes caching possible in the future.
 */
public class LabelledEntriesXSLT
    implements ILabelledEntriesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelledEntriesXSLT.class);

    private static final ArgumentDescriptor<URL> ARGUMENT_SCRIPT =
	new URLArgumentDescriptor("script", "The URL of the XSLT script.");

    private static final ArgumentDescriptor<Map<String, String>> ARGUMENT_PARAMETERS =
	new ArgumentsExtractor<Map<String,String>>
	("parameters",
	 "External parameters passed into the XSL transformation.",
	 new HashMap<String, String>());

    private static final ArgumentDescriptor<Boolean> ARGUMENT_DROP_EMPTY_KEYS =
	new BooleanArgumentDescriptor
	("dropEmptyKeys",
	 "Whether or not to drop entries with empty keys from the transformation result."
	 + " Defaults to true",
	 true);

    private static final ArgumentDescriptor<Boolean> ARGUMENT_FAIL_EMPTY_KEY =
	new BooleanArgumentDescriptor
	("failOnEmptyKey",
	 "Whether or not to fail when there are entries with empty keys in the XQuery result."
	 + " Defaults to true",
	 true);

    private static final ArgumentDescriptor<String> ARGUMENT_TEMPLATE_LNAME =
	new ArgumentDescriptorImpl<String>
	(String.class, "templateLName",
	 "Local name of the start template for generating the entries.",
	 "generate-entries");

    private static final ArgumentDescriptor<String> ARGUMENT_TEMPLATE_PREFIX =
	new ArgumentDescriptorImpl<String>
	(String.class, "templatePrefix",
	 "Prefix to the name of the start template for generating the entries.",
	 "obt");

    private static final ArgumentDescriptor<String> ARGUMENT_TEMPLATE_NAMESPACE =
	new ArgumentDescriptorImpl<String>
	(String.class, "templateNamespace",
	 "Namespace name of the start template for generating the entries.",
	 "http://scdh.wwu.de/oxbytei");


    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
	ARGUMENT_SCRIPT,
	ARGUMENT_PARAMETERS,
        ARGUMENT_FAIL_EMPTY_KEY,
	ARGUMENT_DROP_EMPTY_KEYS,
	ARGUMENT_TEMPLATE_LNAME,
	ARGUMENT_TEMPLATE_PREFIX,
	ARGUMENT_TEMPLATE_NAMESPACE};

    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    protected Map<String, String> arguments;

    protected URL script;
    protected Xslt30Transformer transformer;
    protected Map<String, String> parameters;
    protected Map<QName, XdmValue> xdmParameters = new HashMap<QName, XdmValue>();
    protected boolean failOnEmptyKey;
    protected boolean dropEmptyKeys;
    protected String templateLName, templatePrefix, templateNamespace;
    protected QName templateQName;

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	this.arguments = arguments;
	script = ARGUMENT_SCRIPT.getValue(arguments);
	parameters = ARGUMENT_PARAMETERS.getValue(arguments);
	dropEmptyKeys = ARGUMENT_DROP_EMPTY_KEYS.getValue(arguments);
	failOnEmptyKey = ARGUMENT_FAIL_EMPTY_KEY.getValue(arguments);
	templateLName = ARGUMENT_TEMPLATE_LNAME.getValue(arguments);
	templatePrefix = ARGUMENT_TEMPLATE_PREFIX.getValue(arguments);
	templateNamespace = ARGUMENT_TEMPLATE_NAMESPACE.getValue(arguments);

	templateQName = new QName(templatePrefix, templateNamespace, templateLName);

	// we can setup the XSL transformer
	Processor proc = new Processor(false);
	XsltCompiler comp = proc.newXsltCompiler();
	XsltExecutable exec;
	try {
	    InputStream scriptStream = script.openStream();
	    StreamSource scriptSource = new StreamSource(scriptStream);
	    exec = comp.compile(scriptSource);
	    transformer = exec.load30();
	} catch (SaxonApiException e) {
	    throw new ConfigurationException(e);
	} catch (IOException e) {
	    throw new ConfigurationException(e);
	}

	// make a map of parameters
	for (String key : parameters.keySet()) {
	    xdmParameters.put(new QName(key), new XdmAtomicValue(parameters.get(key)));
	}
    }

    public Map<String, String> getArguments() {
	return arguments;
    }

    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document document,
	 String systemId,
	 String context) {
	// nothing to do here
    }

    public List<LabelledEntry> getLabelledEntries(String userInput)
	throws ExtensionException {
	List<LabelledEntry> entries = new ArrayList<LabelledEntry>();

	XdmValue result;
	try {
	    // set stylesheet parameters
	    transformer.setStylesheetParameters(xdmParameters);
	    // run the transformation
	    result = transformer.callTemplate(templateQName);
	} catch (SaxonApiException e) {
	    throw new ExtensionException(e);
	}
	XdmAtomicValue key = new XdmAtomicValue("key");
	XdmAtomicValue label = new XdmAtomicValue("label");
	// read the result
	for (XdmItem item : result) {
	    Map<XdmAtomicValue, XdmValue> keyValue = item.asMap();
	    LabelledEntry entry =
		new LabelledEntryImpl(keyValue.get(key).toString(),
				      keyValue.get(label).toString());
	    if (!failOnEmptyKey) {
		entries.add(entry);
	    } else {
		if (null == entry.getKey() || "".equals(entry.getKey())) {
		    if (dropEmptyKeys) {
			LOGGER.info("Dropping entry with empty key. Label is: %s", entry.getLabel());
		    } else {
			entries.add(entry);
		    }
		} else {
		    entries.add(entry);
		}
	    }
	}
	return entries;
    }

}

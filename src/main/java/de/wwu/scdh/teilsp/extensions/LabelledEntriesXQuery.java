package de.wwu.scdh.teilsp.extensions;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.URIResolver;

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
 * entries through an XQuery.
 *
 * <P>Only external variables taken from the configuration are passed
 * into the XQuery. Variables from the current editing context or the
 * editor state will not be considered. This static setup makes things
 * faster and makes caching possible in the future.
 */
public class LabelledEntriesXQuery
    implements ILabelledEntriesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelledEntriesXQuery.class);

    private static final ArgumentDescriptor<URL> ARGUMENT_XQUERY =
	new URLArgumentDescriptor("xquery", "The URL of the XQuery file.");

    private static final ArgumentDescriptor<Map<String, String>> ARGUMENT_EXTERNAL =
	new ArgumentsExtractor<Map<String,String>>
	("external",
	 "External variables passed into the XQuery.",
	 new HashMap<String, String>());

    private static final ArgumentDescriptor<Boolean> ARGUMENT_DROP_EMPTY_KEYS =
	new BooleanArgumentDescriptor
	("dropEmptyKeys",
	 "Whether or not to drop entries with empty keys from the XQuery result."
	 + " Defaults to true",
	 true);

    private static final ArgumentDescriptor<Boolean> ARGUMENT_FAIL_EMPTY_KEY =
	new BooleanArgumentDescriptor
	("failOnEmptyKey",
	 "Whether or not to fail when there are entries with empty keys in the XQuery result."
	 + " Defaults to true",
	 true);

    private static final ArgumentDescriptor<String> ARGUMENT_FUNCTION_LNAME =
	new ArgumentDescriptorImpl<String>
	(String.class, "functionLName",
	 "Local name of the XQuery function to call for generating the entries.",
	 "generate-entries");

    private static final ArgumentDescriptor<String> ARGUMENT_FUNCTION_PREFIX =
	new ArgumentDescriptorImpl<String>
	(String.class, "functionPrefix",
	 "Prefix to the name of the XQuery function to call for generating the entries.",
	 "obt");

    private static final ArgumentDescriptor<String> ARGUMENT_FUNCTION_NAMESPACE =
	new ArgumentDescriptorImpl<String>
	(String.class, "functionNamespace",
	 "Namespace name of the XQuery function to call for generating the entries.",
	 "http://scdh.wwu.de/oxbytei");


    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
	ARGUMENT_XQUERY,
	ARGUMENT_EXTERNAL,
        ARGUMENT_FAIL_EMPTY_KEY,
	ARGUMENT_DROP_EMPTY_KEYS,
	ARGUMENT_FUNCTION_LNAME,
	ARGUMENT_FUNCTION_PREFIX,
	ARGUMENT_FUNCTION_NAMESPACE};

    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    protected Map<String, String> arguments;

    protected URL xq;
    protected XQueryEvaluator qe;
    protected Map<String, String> external;
    protected boolean failOnEmptyKey;
    protected boolean dropEmptyKeys;
    protected String functionLName, functionPrefix, functionNamespace;
    protected QName functionQName;

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	this.arguments = arguments;
	xq = ARGUMENT_XQUERY.getValue(arguments);
	external = ARGUMENT_EXTERNAL.getValue(arguments);
	dropEmptyKeys = ARGUMENT_DROP_EMPTY_KEYS.getValue(arguments);
	failOnEmptyKey = ARGUMENT_FAIL_EMPTY_KEY.getValue(arguments);
	functionLName = ARGUMENT_FUNCTION_LNAME.getValue(arguments);
	functionPrefix = ARGUMENT_FUNCTION_PREFIX.getValue(arguments);
	functionNamespace = ARGUMENT_FUNCTION_NAMESPACE.getValue(arguments);

	functionQName = new QName(functionPrefix, functionNamespace, functionLName);

	// we can setup the XQuery evaluator
	Processor proc = new Processor(false);
	XQueryCompiler comp = proc.newXQueryCompiler();
	XQueryExecutable exp;
	try {
	    exp = comp.compile(this.xq.openStream());
	} catch (SaxonApiException e) {
	    throw new ConfigurationException(e);
	} catch (IOException e) {
	    throw new ConfigurationException(e);
	}
	qe = exp.load();
	for (String key : external.keySet()) {
	    qe.setExternalVariable(new QName(key), new XdmAtomicValue(external.get(key)));
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
	    //result = qe.evaluate();
	    XdmValue[] params = new XdmValue[] {}; // empty array of function parameters
	    result = qe.callFunction(functionQName, params);
	} catch (SaxonApiException e) {
	    throw new ExtensionException(e);
	}
	XdmAtomicValue key = new XdmAtomicValue("key");
	XdmAtomicValue label = new XdmAtomicValue("label");
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

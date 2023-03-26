package de.wwu.scdh.teilsp.extensions;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.*;
// import net.sf.saxon.lib.ResourceResolver; // since Saxon 11
// import net.sf.saxon.lib.ResourceResolverWrappingURIResolver; // since Saxon 11
// import net.sf.saxon.lib.Feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;
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
    extends AbstractLabelledEntriesX
    implements ILabelledEntriesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelledEntriesXSLT.class);

    private static final ArgumentDescriptor<URL> ARGUMENT_SCRIPT =
	new URLArgumentDescriptor("script", "The URL of the XSLT script.");

    private static final ArgumentDescriptor<Map<String, String>> ARGUMENT_PARAMETERS =
	new ArgumentsExtractor<Map<String,String>>
	("parameters",
	 "External parameters passed into the XSL transformation.",
	 new HashMap<String, String>());

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
    protected String templateLName, templatePrefix, templateNamespace;
    protected QName templateQName;
    protected Processor processor;

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	super.init(arguments);
	this.arguments = arguments;
	script = ARGUMENT_SCRIPT.getValue(arguments);
	parameters = ARGUMENT_PARAMETERS.getValue(arguments);
	templateLName = ARGUMENT_TEMPLATE_LNAME.getValue(arguments);
	templatePrefix = ARGUMENT_TEMPLATE_PREFIX.getValue(arguments);
	templateNamespace = ARGUMENT_TEMPLATE_NAMESPACE.getValue(arguments);

	templateQName = new QName(templatePrefix, templateNamespace, templateLName);

	// make a map of parameters
	for (String key : parameters.keySet()) {
	    xdmParameters.put(new QName(key), new XdmAtomicValue(parameters.get(key)));
	}

	// we set up the saxon processor, i.e. the configuration
	processor = new Processor(false);
    }

    public Map<String, String> getArguments() {
	return arguments;
    }

    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document document,
	 String systemId,
	 String context)
	throws ExtensionException {
	// We do not use the URI resolver from the editor any more,
	//since the Configuration.setURIResolver() has gone in Saxon
	//11, and it is not possible to set an instance by the Feature
	//approach. Also, the setURIResolver() methods on the compiler
	//and on the transformer have changed in Saxon 11. Instead, we
	//use the URIResolver defined in the configration
	//(net.sf.saxon.lib.StandardURIResolver by default). An we
	//will provide a catalog argument in the future. Generally,
	//this way we distinguish between the two concepts of
	//resolving and the feature of an OASIS catalog.

	// // since Saxon 11
	// if (uriResolver != null) {
	//     ResourceResolver resourceResolver = new ResourceResolverWrappingURIResolver(uriResolver);
	//     processor.setConfigurationProperty(Feature.RESOURCE_RESOLVER, resourceResolver);
	// }
    }

    public List<LabelledEntry> getLabelledEntries(String userInput)
	throws ExtensionException {

	// setup the XSL transformer
	XsltCompiler comp = processor.newXsltCompiler();
	XsltExecutable exec;
	try {
	    InputStream scriptStream = script.openStream();
	    StreamSource scriptSource = new StreamSource(scriptStream, script.toString());
	    exec = comp.compile(scriptSource);
	    transformer = exec.load30();
	} catch (SaxonApiException e) {
	    throw new ExtensionException(e);
	} catch (IOException e) {
	    throw new ExtensionException(e);
	}

	XdmValue result;
	try {
	    // set stylesheet parameters
	    transformer.setStylesheetParameters(xdmParameters);
	    // run the transformation
	    result = transformer.callTemplate(templateQName);
	} catch (SaxonApiException e) {
	    throw new ExtensionException(e);
	}

	return getEntries(result);
    }

}

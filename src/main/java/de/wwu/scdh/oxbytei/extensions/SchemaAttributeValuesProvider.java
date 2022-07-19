package de.wwu.scdh.oxbytei.extensions;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.contentcompletion.xml.CIValue;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ConfiguredPluginLoader;


import de.wwu.scdh.oxbytei.commons.StaticSchemaManager;


/**
 * A provider of labelled entries that generates them from the
 * schema. This plugin is designed for use in oXygen. It will work
 * there, only.
 *
 * This implements {@link ILabelledEntriesProvider} and should be
 * registered for the SPI.
 */
public class SchemaAttributeValuesProvider
    implements ILabelledEntriesProvider {

    protected String nodeName;

    private static final ArgumentDescriptor<String> ARGUMENT_NODE_NAME =
	new ArgumentDescriptorImpl<String>
	(String.class,
	 // we use the special argument that's allways present!
	 ConfiguredPluginLoader.SPECIAL_ARGUMENT_NODE_NAME,
	 "The name of the attribute to get valid values for.");

    private static final ArgumentDescriptor<?>[] ARGUMENTS =
	new ArgumentDescriptor<?>[] {
	ARGUMENT_NODE_NAME};

    public ArgumentDescriptor<?>[] getArgumentDescriptor () {
	return ARGUMENTS;
    }

    private Map<String, String> arguments;

    // public SchemaAttributeValuesProvider() {
    // 	// nothing to do
    // 	System.out.println(SchemaAttributeValuesProvider.class.toString());
    // }

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	this.arguments = arguments;
	nodeName = ARGUMENT_NODE_NAME.getValue(arguments);
	System.out.println("SchemaAttributeValueProvider initialized for attribute " + nodeName);
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
	// nothing to do
	System.out.println("setup() called");
    }

    public List<LabelledEntry> getLabelledEntries(String userInput) {
	List<LabelledEntry> entries = new ArrayList<LabelledEntry>();

	System.out.println("getLabelledEntries() called");

	WSEditorPage page = StaticSchemaManager.getWsEditorPage();
	if (WSAuthorEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in author mode
	    System.out.println("in author mode");
	    if (false) {
		return entries;
	    }
	    List<CIValue> ciValues = StaticSchemaManager.whatPossibleValuesHasAttribute(nodeName);
	    System.out.print("Number of allowed values from schema: ");
	    System.out.println(ciValues.size());
	    for (CIValue ciValue : ciValues) {
		String label = ciValue.toString(); //+ "\t" + ciValue.getAnnotation();
		LabelledEntry entry = new LabelledEntry(ciValue.toString(), label);
		System.out.println(entry.getLabel());
		entries.add(entry);
	    }
	} else if (WSXMLTextEditorPage.class.isAssignableFrom(page.getClass())) {
	    // TODO: what to do for text mode? We might have the
	    // nodeName from a dialog! We would have to change the
	    // offset in this case.
	} else {
	    // TODO: what to do in grid mode
	}

	return entries;
    }

}

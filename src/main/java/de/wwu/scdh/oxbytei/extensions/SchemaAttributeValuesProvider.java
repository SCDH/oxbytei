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
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.extensions.LabelledEntryImpl;
import de.wwu.scdh.teilsp.services.extensions.ConfigurablePlugin;

import de.wwu.scdh.oxbytei.commons.OxygenBasedSchemaProvider;
import de.wwu.scdh.oxbytei.commons.StaticSchemaManager;


/**
 * A provider of labelled entries that generates them from the
 * schema. This plugin is designed for use in oXygen. It will work
 * there, only.
 *
 * This implements {@link ILabelledEntriesProvider} and should be
 * registered for the SPI.
 *
 * Note: Take care about not setting up infinite loops when this is
 * combined with a SchemaManagerFilter. See the implementation of
 * {@link de.wwu.scdh.oxbytei.OxbyteiSchemaManagerFilter} on how to
 * avoid this.
 */
public class SchemaAttributeValuesProvider
    implements ILabelledEntriesProvider, OxygenBasedSchemaProvider {

    protected String nodeName;

    private static final ArgumentDescriptor<?>[] ARGUMENTS =
	new ArgumentDescriptor<?>[] {
	ConfigurablePlugin.SPECIAL_ARGUMENT_NODE_NAME};

    public ArgumentDescriptor<?>[] getArgumentDescriptor () {
	return ARGUMENTS;
    }

    private Map<String, String> arguments;

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	this.arguments = arguments;
	nodeName = ConfigurablePlugin.SPECIAL_ARGUMENT_NODE_NAME.getValue(arguments);
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
    }

    public List<LabelledEntry> getLabelledEntries(String userInput) {
	List<LabelledEntry> entries = new ArrayList<LabelledEntry>();

	WSEditorPage page = StaticSchemaManager.getWsEditorPage();
	if (WSAuthorEditorPage.class.isAssignableFrom(page.getClass())) {
	    // we are in author mode
	    List<CIValue> ciValues = StaticSchemaManager.whatPossibleValuesHasAttribute(nodeName);
	    for (CIValue ciValue : ciValues) {
		String label = ciValue.toString() + "\t" + ciValue.getAnnotation();
		LabelledEntry entry = new LabelledEntryImpl(ciValue.toString(), label);
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

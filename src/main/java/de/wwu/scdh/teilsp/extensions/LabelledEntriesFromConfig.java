package de.wwu.scdh.teilsp.extensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;


public class LabelledEntriesFromConfig
    implements ILabelledEntriesProvider {

    private Map<String, String> arguments;

    private List<LabelledEntry> labelledEntries;

    private static final ArgumentDescriptor<String> ARGUMENT_KEYS =
	new ArgumentDescriptorImpl<String>(String.class, "keys", "A list of keys.");

    private static final ArgumentDescriptor<String> ARGUMENT_LABELS =
	new ArgumentDescriptorImpl<String>(String.class, "labels", "A list of labels.");

    private static final ArgumentDescriptor<String> ARGUMENT_SEPARATOR =
	new ArgumentDescriptorImpl<String>
	(String.class, "separator",
	 "The separator that delimits the keys and labels. Default to comma.", ",");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
	ARGUMENT_KEYS,
	ARGUMENT_LABELS,
	ARGUMENT_SEPARATOR
	};

    /**
     * See {@link ConfigurablePlugin#getArgumentDescriptor()}
     */
    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    public void init(Map<String, String> args)
	throws ConfigurationException {

	arguments = args;

	String sep = ARGUMENT_SEPARATOR.getValue(args);
	String[] keys = ARGUMENT_KEYS.getValue(args).split(sep);
	String[] labels = ARGUMENT_LABELS.getValue(args).split(sep);

	labelledEntries = new ArrayList<LabelledEntry>();

	int l = Math.min(keys.length, labels.length);
	for (int i = 0; i < l; i++) {
	    labelledEntries.add(new LabelledEntryImpl(keys[i].trim(), labels[i].trim()));
	}
    }

    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document doc,
	 String systemId,
	 String context) {
    }

    public Map<String, String> getArguments() {
	return arguments;
    }

    public List<LabelledEntry> getLabelledEntries(String userInput)
	throws ExtensionException {
	return labelledEntries;
    }
}

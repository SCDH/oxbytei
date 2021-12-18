/**
 * {@link AbstractPrefixURIOperation} - is a wrapper around the
 * selection user dialog from ediarum.JAR. It has the downside, that
 * no current value is displayed!
 */
package de.wwu.scdh.oxbytei.commons;

import java.util.Map;
import java.util.List;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;


public class OxygenSelectionDialog
    implements ISelectionDialog {

    AuthorAccess authorAccess;
    String title;
    String multiple;
    String currentValue;
    List<ConfiguredEntriesProvider>configuredEntriesProviders;


    public void init(AuthorAccess access,
		     String tit,
		     String multi,
		     String currentVal,
		     List<ConfiguredEntriesProvider> configured) {
	authorAccess = access;
	title = tit;
	multiple = multi;
	currentValue = currentVal;
	configuredEntriesProviders = configured;
    }

    /**
     * Do the user interaction part.
     *
     */
    public String doUserInteraction()
	throws AuthorOperationException {

	// FIXME
	//
	// the user dialogue from oxygen does not allow updates on
	// user input, but is rather static.

	// we need some iteration variables
	int i, j;

	LabelledEntry entry;
	String pairs = "";
	for (i = 0; i < configuredEntriesProviders.size(); i++) {
	    ConfiguredEntriesProvider configuredEntriesProvider = configuredEntriesProviders.get(i);
	    ILabelledEntriesProvider provider = configuredEntriesProvider.getProvider();
	    try {
		List<LabelledEntry> entries = provider.getLabelledEntries(configuredEntriesProvider.getArguments());
		for (j = 0; j < entries.size(); j++) {
		    entry = entries.get(j);
		    if (pairs != "") {
			pairs += ";";
		    }
		    pairs += "'" + entry.getKey() + "':'" + entry.getLabel() + "'";
		}
	    } catch (ExtensionException e) {
		String report = "";
		for (Map.Entry<String, String> argument : configuredEntriesProvider.getArguments().entrySet()) {
		    report += argument.getKey() + " = " + argument.getValue() + "\n";
		}
		throw new AuthorOperationException("Error reading entries\n\n"
						   + report + "\n\n" + e);
	    }

	    // String report = "";
	    // for (Map.Entry<String, String> argument : configuredEntriesProvider.arguments.entrySet()) {
	    // 	report += argument.getKey() + " = " + argument.getValue() + "\n";
	    // }
	    // System.err.println("Config of " + provider.getClass().getCanonicalName() + "\n" + report);

	}
	
	//AskDescriptor("combobox", title, keys, labels, currentValue);
	String ask = "${ask('" + title + "', combobox, (" + pairs + "), '" + currentValue + "')}";
	String selectedId = authorAccess.getUtilAccess().expandEditorVariables(ask, null, true);
	return selectedId;
    }

}

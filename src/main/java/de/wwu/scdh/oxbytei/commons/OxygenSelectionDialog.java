/**
 * {@link AbstractPrefixURIOperation} - is a wrapper around the
 * selection user dialog of oXygen's {@code ${ask('...', combobox, ...)}.}
 * It does not offer multiple selection.
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
    boolean multiple;
    String currentValue;
    List<ILabelledEntriesProvider> providers;


    public void init(AuthorAccess access,
		     String tit,
		     boolean multi,
		     String currentVal,
		     List<ILabelledEntriesProvider> configured)
    throws AuthorOperationException {
	authorAccess = access;
	title = tit;
	multiple = multi;
	currentValue = currentVal;
	providers = configured;
    }

    /**
     * Do the user interaction part.
     *
     */
    public String doUserInteraction()
	throws AuthorOperationException {

	// TODO
	//
	// the user dialogue from oxygen does not allow updates on
	// user input, but is rather static.

	// we need some iteration variables
	int i, j;

	LabelledEntry entry;
	String pairs = "";
	for (i = 0; i < providers.size(); i++) {
	    ILabelledEntriesProvider provider = providers.get(i);
	    try {
		// TODO: We just pass the empty string here.
		List<LabelledEntry> entries = provider.getLabelledEntries("");
		for (j = 0; j < entries.size(); j++) {
		    entry = entries.get(j);
		    if (pairs != "") {
			pairs += ";";
		    }
		    pairs += "'" + entry.getKey() + "':'" + entry.getLabel() + "'";
		}
	    } catch (ExtensionException e) {
		String report = "";
		for (Map.Entry<String, String> argument : provider.getArguments().entrySet()) {
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
	// When "Cancel" is pressed in the dialog, the unexpanded
	// string is returned. In this case we set the selection to
	// the empty string.
	if (selectedId.startsWith("${ask(")) {
	    selectedId = null;
	}
	return selectedId;
    }

}

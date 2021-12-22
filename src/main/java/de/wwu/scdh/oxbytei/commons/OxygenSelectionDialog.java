/**
 * {@link AbstractPrefixURIOperation} - is a wrapper around the
 * selection user dialog of oXygen's {@code ${ask('...', combobox, ...)}.}
 * It does not offer multiple selection.
 *
 * Bug: If the label of two selection items equal, then the value of
 * the duplicates is inaccessible. This is an oXygen bug.
 */
package de.wwu.scdh.oxbytei.commons;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
    List<String> currentValue;
    List<ILabelledEntriesProvider> providers;


    public void init(AuthorAccess access,
		     String tit,
		     boolean multi,
		     List<String> currentVal,
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
    public List<String> doUserInteraction()
	throws AuthorOperationException {

	// TODO
	//
	// the user dialogue from oxygen does not allow updates on
	// user input, but is rather static.

	// we need some iteration variables
	int i, j;
	int total = 0; // we count the absolute number of selection entries

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
		    total++;
		}
	    } catch (ExtensionException e) {
		String report = "";
		for (Map.Entry<String, String> argument : provider.getArguments().entrySet()) {
		    report += argument.getKey() + " = " + argument.getValue() + "\n";
		}
		throw new AuthorOperationException("Error reading entries\n\n"
						   + report + "\n\n" + e);
	    }
	}

	// if there are no selection items at all, we display a
	// message with usefull debugging information
	if (total == 0) {
	    String report = "";
	    report += "Plugins found for current editing context: " + providers.size() + "\n\n";
	    for (ILabelledEntriesProvider p : providers) {
		report += "Plugin: " + p.getClass().getCanonicalName() + "\nArguments:";
		for (Map.Entry<String, String> argument : p.getArguments().entrySet()) {
		    report += argument.getKey() + " = " + argument.getValue() + "\n";
		}
		report += "\n\n";
	    }
	    throw new AuthorOperationException(report);
	}

	// get first of current values
	String current = "";
	if (currentValue != null) {
	    if (currentValue.size() > 0) {
		current = currentValue.get(0);
	    }
	}
	
	//AskDescriptor("combobox", title, keys, labels, currentValue);
	String ask = "${ask('" + title + "', combobox, (" + pairs + "), '" + current + "')}";
	String selectedId = authorAccess.getUtilAccess().expandEditorVariables(ask, null, true);
	// When "Cancel" is pressed in the dialog, the unexpanded
	// string is returned. In this case we set the selection to
	// the empty string.
	if (selectedId.startsWith("${ask(")) {
	    return null;
	} else {
	    List<String> result = new ArrayList<String>();
	    result.add(selectedId);
	    return result;
	}
    }

}

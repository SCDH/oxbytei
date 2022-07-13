/**
 * {@link AbstractPrefixURIOperation} - is a wrapper around the
 * selection user dialog of oXygen's {@code ${ask('...', combobox, ...)}.}
 * It does not offer multiple selection.
 *
 * Bug: If the label of two selection items equal, then the value of
 * the duplicates is inaccessible. This is an oXygen bug.
 */
package de.wwu.scdh.oxbytei.commons;

import java.awt.Frame;
import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.PluginWorkspace;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.exceptions.UIException;
import de.wwu.scdh.teilsp.ui.ISelectionDialog;


public class OxygenSelectionDialog
    implements ISelectionDialog {

    AuthorAccess authorAccess;
    String title;
    boolean multiple;
    List<String> currentValue, result;
    List<ILabelledEntriesProvider> providers;

    public OxygenSelectionDialog(Frame frame) {}

    public OxygenSelectionDialog() {
    }

    public void init(Map<String, String> arguments) {
	if (arguments.containsKey("title")) {
	    title = arguments.get("title");
	} else {
	    title = "Select";
	}
    }

    public void setup(List<String> currentVal,
		      List<ILabelledEntriesProvider> configured) {
	currentValue = currentVal;
	providers = configured;
    }

    /**
     * Do the user interaction part.
     *
     */
    public void doUserInteraction()
	throws UIException, ExtensionException {

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
		    pairs += "'" + entry.getKey().replaceAll("'", "") + "':'" + entry.getLabel().replaceAll("'", "") + "'";
		    total++;
		}
	    } catch (ExtensionException e) {
		String report = "";
		for (Map.Entry<String, String> argument : provider.getArguments().entrySet()) {
		    report += argument.getKey() + " = " + argument.getValue() + "\n";
		}
		throw new ExtensionException("Error reading entries\n\n"
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
	    throw new UIException(report);
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
	PluginWorkspace ws = PluginWorkspaceProvider.getPluginWorkspace();
	String selectedId = ws.getUtilAccess().expandEditorVariables(ask, null, true);
	// When "Cancel" is pressed in the dialog, the unexpanded
	// string is returned. In this case we set the selection to
	// the empty string.
	if (selectedId.startsWith("${ask(")) {
	    result = null;
	} else {
	    result = new ArrayList<String>();
	    result.add(selectedId);
	}
    }

    public List<String> getSelection() {
    	return result;
    }
}

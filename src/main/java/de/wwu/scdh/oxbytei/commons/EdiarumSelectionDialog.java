/**
 * {@link AbstractPrefixURIOperation} - is a wrapper around the
 * selection user dialog from ediarum.JAR. It has the downside, that
 * no current value is displayed!
 */
package de.wwu.scdh.oxbytei.commons;

import java.awt.Frame;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import org.bbaw.telota.ediarum.InsertRegisterDialog;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;


public class EdiarumSelectionDialog
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
		     List<ILabelledEntriesProvider> configured) {
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
	// the user dialogue from ediarum we currently use
	// takes two static string arrays: keys and values
	//
	// so we call the plugins here. But they should be called from
	// UI code in order to allow updates.

	// we need some iteration variables
	int i, j, k;

	List<String> keys = new ArrayList<String>();
	List<String> labels = new ArrayList<String>();
	LabelledEntry entry;
	k = 0;
	for (i = 0; i < providers.size(); i++) {
	    ILabelledEntriesProvider provider = providers.get(i);
	    try {
		// TODO: We just pass the empty string here.
		List<LabelledEntry> entries = provider.getLabelledEntries("");
		for (j = 0; j < entries.size(); j++) {
		    entry = entries.get(j);
		    keys.add(entry.getKey());
		    labels.add(entry.getLabel());
		    k++;
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
	String[] keysArray = new String[k];
	String[] labelsArray = new String[k];
	for (i = 0; i < k; i++) {
	    keysArray[i] = keys.get(i);
	    labelsArray[i] = labels.get(i);
	}
	//System.err.println("Items: " + k);


	// Ask the user for selection
	InsertRegisterDialog dialog =
	    new InsertRegisterDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(),
				     labelsArray,
				     keysArray,
				     multiple);
	String selectedId = dialog.getSelectedID(); //"somewhere_out_there";

	return selectedId;
    }

}

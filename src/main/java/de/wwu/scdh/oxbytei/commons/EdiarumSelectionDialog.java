/**
 * {@link AbstractPrefixURIOperation} - is a wrapper around the
 * selection user dialog from ediarum.JAR.
 *
 * Downside/Bugs: 1) no current value is displayed; 2) {@code null} is
 * not returned on cancellation (It's not possible to distinguish
 * between zero selections and cancellation.)
 *
 * Cool features: multiple selection possible (Does it really work?)
 */
package de.wwu.scdh.oxbytei.commons;

import java.awt.Frame;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.bbaw.telota.ediarum.InsertRegisterDialog;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.ui.ISelectionDialog;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;


public class EdiarumSelectionDialog
    implements ISelectionDialog {

    private Frame frame;
    private boolean MULTIPLE = true;
    private String title;
    private List<String> currentValue, result;
    private List<ILabelledEntriesProvider> providers;
    private Map<String, String> arguments;

    public EdiarumSelectionDialog() {
	frame = new Frame();
    }

    public EdiarumSelectionDialog(Frame frame) {
	this.frame = frame;
    }

    private static final ArgumentDescriptor<String> ARGUMENT_TITLE =
	ISelectionDialog.ARGUMENT_TITLE;

    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
	ARGUMENT_TITLE
	    };

    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	this.arguments = arguments;
	title = ARGUMENT_TITLE.getValue(arguments);
    }

    public Map<String, String> getArguments() {
	return arguments;
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
	throws ExtensionException {

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
		throw new ExtensionException("Error reading entries\n\n"
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
	    new InsertRegisterDialog(frame, labelsArray, keysArray, MULTIPLE);

	// get selected value. Right on multiple?
	if (MULTIPLE) {
	    result = Arrays.asList(dialog.getSelectedIDs());
	} else {
	    result = new ArrayList<String>();
	    result.add(dialog.getSelectedID());
	}
    }

    public List<String> getSelection() {
    	return result;
    }
}

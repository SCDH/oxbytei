package de.wwu.scdh.teilsp.ui;

import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.awt.Frame;
import javax.swing.JComboBox;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;


/**
 * Editable version of the combo box select dialog
 */
public class EditableComboBoxSelectDialog
    extends ComboBoxSelectDialog {

    protected boolean EDITABLE = true;

    JComboBox<String> comboBox;

    public EditableComboBoxSelectDialog() {
	super();
    }

    public EditableComboBoxSelectDialog(Frame frame) {
	super(frame);
    }

    protected void setupComboBox()
	throws ExtensionException {
	int i;
	Vector<String> entriesVector = new Vector<String>();
	for (i = 0; i < providers.size(); i++) {
	    ILabelledEntriesProvider provider = providers.get(i);
	    try {
		// TODO: We just pass the empty string here.
		List<LabelledEntry> entries = provider.getLabelledEntries("");
		for (LabelledEntry entry : entries) {
		    entriesVector.add(entry.getKey());
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

	// set up current value
	String selected = null;
	for (int j = 0; j < currentValue.size(); j++) {
	    selected = currentValue.get(j);
	    if (j < currentValue.size() - 1) {
		selected = selected + " ";
	    }
	}

	comboBox = new JComboBox<String>(entriesVector);
	comboBox.setEditable(EDITABLE);
	comboBox.setSelectedItem(selected);

	// add combo box to pane
	comboBoxes.add(comboBox);
    }

    protected String getComboBoxSelection() {
	return (String) comboBox.getSelectedItem();
    }

}

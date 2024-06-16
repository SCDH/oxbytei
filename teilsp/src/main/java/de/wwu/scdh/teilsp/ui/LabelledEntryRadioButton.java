/*
 * A {@link JRadioButton} Radio Button from an {@link LabelledEntry}
 */
package de.wwu.scdh.teilsp.ui;

import javax.swing.JRadioButton;

import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;

public class LabelledEntryRadioButton extends JRadioButton {

    private LabelledEntry entry;

    public LabelledEntryRadioButton(LabelledEntry entry) {
	super(entry.getLabel());
	this.entry = entry;
    }

    public LabelledEntryRadioButton(LabelledEntry entry, boolean selected) {
	super(entry.getLabel(), selected);
	this.entry = entry;
    }

    public LabelledEntry getEntry() {
	return entry;
    }
}

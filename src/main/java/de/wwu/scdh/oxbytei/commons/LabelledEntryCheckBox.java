/*
 * A {@link JCheckBox} Check Box from an {@link LabelledEntry}
 */
package de.wwu.scdh.oxbytei.commons;

import javax.swing.JCheckBox;

import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;

public class LabelledEntryCheckBox extends JCheckBox {

    private LabelledEntry entry;

    public LabelledEntryCheckBox(LabelledEntry entry) {
	super(entry.getLabel());
	this.entry = entry;
    }

    public LabelledEntryCheckBox(LabelledEntry entry, boolean selected) {
	super(entry.getLabel(), selected);
	this.entry = entry;
    }

    public LabelledEntry getEntry() {
	return entry;
    }
}

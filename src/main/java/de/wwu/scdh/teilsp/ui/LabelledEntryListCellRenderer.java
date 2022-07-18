package de.wwu.scdh.teilsp.ui;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;

import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;


public class LabelledEntryListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent
	(JList<?> list,
	 Object value,
	 int index,
	 boolean isSelected,
	 boolean hasCellFocus) {
	// the base class inherits from JLabel. So we can call setText()!
	super.getListCellRendererComponent(list, value, index, isSelected, hasCellFocus);
	if (value != null) {
	    this.setText(((LabelledEntry) value).getLabel());
	}
	return this;
    }

}

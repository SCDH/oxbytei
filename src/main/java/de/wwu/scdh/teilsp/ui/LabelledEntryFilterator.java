package de.wwu.scdh.teilsp.ui;

import java.util.List;

import ca.odell.glazedlists.TextFilterator;

import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;

/**
 * A filter for an event list of {@link LabelledEntry}s.
 */
public class LabelledEntryFilterator implements TextFilterator<LabelledEntry> {

    public void getFilterStrings(List<String> baseList, LabelledEntry entry) {
	baseList.add(entry.getLabel());
	baseList.add(entry.getKey());
    }

}

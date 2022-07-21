package de.wwu.scdh.teilsp.extensions;

import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;

/**
 * {@link LabelledEntryImpl} is a simple implementation of 
 * {@link LabelledEntry} that stores a labelled entry.
 */
public class LabelledEntryImpl implements LabelledEntry {
    private final String label;
    private final String key;

    public LabelledEntryImpl(String k, String l) {
	key = k;
	label = l;
    }

    /**
     * If casting from String, this constructor is used.
     */
    public LabelledEntryImpl(String k) {
	key = k;
	label = k;
    }

    public String getKey() {
	return key;
    }

    public String getLabel() {
	return label;
    }

    @Override
    public String toString() {
	return key;
    }
}

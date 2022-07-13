/**
 * LabelledEntry is a simple class that stores labelled entries from
 * an external file.
 *
 */
package de.wwu.scdh.teilsp.services.extensions;

public class LabelledEntry {
    private final String label;
    private final String key;

    public LabelledEntry(String k, String l) {
	key = k;
	label = l;
    }

    /**
     * If casting from String, this constructor is used.
     */
    public LabelledEntry(String k) {
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

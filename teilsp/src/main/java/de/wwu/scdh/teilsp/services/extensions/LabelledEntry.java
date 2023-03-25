package de.wwu.scdh.teilsp.services.extensions;

/**
 * A {@link LabelledEntry} is basically a pair of key and label. The
 * key is the technical value used in the XML file and the label is a
 * human readable explanation about this value.
 *
 * Labelled entries are used e.g. for content completion.
 */
public interface LabelledEntry {

    /**
     * Get the key of the entry.
     */
    public String getKey();

    /**
     * Get the label of the entry.
     */
    public String getLabel();

    /**
     * This should even return the key of the entry.
     */
    public String toString();

}

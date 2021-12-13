package de.wwu.scdh.teilsp.services.extensions;

import java.util.List;
import java.util.Map;

import de.wwu.scdh.teilsp.completion.LabelledEntry;


public interface ILabelledEntriesProvider {

    /**
     * This method must return an ordered collection of labelled
     * entries. It takes a map of key value pairs as arguments.
     * @param  kwargs   key value pairs with configuration parameters
     * @return          an ordered sequence of labelled entries
     */
    List<LabelledEntry> getLabelledEntries(Map<String, String> kwargs) throws ExtensionException;
    
}

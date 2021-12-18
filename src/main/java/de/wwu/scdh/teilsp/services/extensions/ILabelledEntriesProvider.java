/**
 * {@link ILabelledEntriesProvider} - an interface for plugins, that
 * provide labelled entries.
 *
 */
package de.wwu.scdh.teilsp.services.extensions;

import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.transform.URIResolver;
import org.xml.sax.EntityResolver;


public interface ILabelledEntriesProvider {

    /**
     * This method initializes the provider with a setup from
     * configuration and the current editing context. It is once
     * called during the initialization phase.
     *
     * @param  kwargs      key value pairs with configuration parameters
     * @param  uriResolver URI resolver used in the editor
     * @param  entityResolver entity resolver used in the editor
     * @param  systemId    URL of the currently edited file
     * @return             an ordered sequence of labelled entries
     */
    void init(Map<String, String> kwargs,
	      URIResolver uriResolver,
	      EntityResolver entityResolver,
	      URL systemId) throws ExtensionException;
    
    /**
     * This method must return an ordered collection of labelled
     * entries. It takes a map of key value pairs as arguments.
     *
     * @param  userInput   what the user has typed so far	
     * @return             an ordered sequence of labelled entries
     */
    List<LabelledEntry> getLabelledEntries(String userInput) throws ExtensionException;
    
}

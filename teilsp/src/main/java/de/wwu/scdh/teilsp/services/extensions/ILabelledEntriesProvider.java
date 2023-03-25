package de.wwu.scdh.teilsp.services.extensions;

import java.util.List;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;


/**
 * {@link ILabelledEntriesProvider} - an interface for plugins, which
 * provide labelled entries.
 *
 * By separting the initialization/setup phase and the getting phase
 * which can send user input to the plugin, this should be sufficient
 * for plugins that need on-going user interaction to get labelled
 * entries from a huge collection of entries etc.
 *
 * Note, that credentials can be passed in as arguments in the
 * initialization phase,
 * see {@link ConfigurablePlugin#init(java.util.Map)}.
 */
public interface ILabelledEntriesProvider extends ConfigurablePlugin {

    /**
     * {@link setup} passes a setup of document and resolvers to the
     * plugin. This method should be called once called during the
     * initialization phase,
     * after {@link ConfigurablePlugin#init(java.util.Map)}.
     *
     * @param  uriResolver URI resolver used in the editor
     * @param  entityResolver entity resolver used in the editor
     * @param  document    the currently edited file as DOM object
     * @param  systemId    URL of the current document
     * @param  context     XPath to the current editing position
     */
    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document document,
	 String systemId,
	 String context)
	throws ExtensionException;

    /**
     * {@link getLabelledEntries} returns an ordered collection of labelled
     * entries. It takes a map of key value pairs as arguments.
     *
     * @param  userInput   what the user has typed so far
     * @return             an ordered sequence of labelled entries
     */
    public List<LabelledEntry> getLabelledEntries(String userInput) throws ExtensionException;

}

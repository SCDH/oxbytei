package de.wwu.scdh.oxbytei;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;

import org.w3c.dom.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.sync.contentcompletion.xml.SchemaManagerFilter;
import ro.sync.contentcompletion.xml.CIAttribute;
import ro.sync.contentcompletion.xml.CIElement;
import ro.sync.contentcompletion.xml.CIValue;
import ro.sync.contentcompletion.xml.Context;
import ro.sync.contentcompletion.xml.WhatAttributesCanGoHereContext;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.contentcompletion.xml.WhatPossibleValuesHasAttributeContext;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
//import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;


import de.wwu.scdh.teilsp.config.EditorVariablesExpander;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ConfiguredPluginLoader;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;
import de.wwu.scdh.oxbytei.commons.EditorVariablesExpanderImpl;
import de.wwu.scdh.oxbytei.commons.OxygenBasedSchemaProvider;


/**
 * A schema manager filter that provides the user with content
 * completion suggestions for the current editing context from
 * plugins.
 *
 * @author Christian Lück
 */
public class OxbyteiSchemaManagerFilter
    implements SchemaManagerFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OxbyteiSchemaManagerFilter.class);

    public String getDescription() {
	return "oXbytei content completer";
    }

    private static final String[] namespaces = null;
    // {"xml", "http://www.w3.org/XML/1998/namespace",
    //  "tei", "http://www.tei-c.org/ns/1.0",
    //  "", "http://www.tei-c.org/ns/1.0"};

    protected List<ILabelledEntriesProvider> providersForContext(Context context, String nodeType, String nodeName) {
	// get resolvers and url of the current editing context
	URIResolver uriResolver =
	    PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().getURIResolver();
	EntityResolver entityResolver =
	    PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().getEntityResolver();

	// try {
	//     // get URL of currently edited file
	//     URL currentFileURL = new URL(context.getSystemID());
	//     extensionsConfiguration = ConfigurationReader.getExtensionsConfiguration(currentFileURL, configFile);
	//     LOGGER.error("Expanded editor variables in {}, current edited URL: {}", configFile, currentFileURL.toString());
	// } catch (MalformedURLException e) {
	//     LOGGER.error("Path to configuration file is not a valid URL: {}", context.getSystemID());
	// } catch (ConfigurationException e) {
	//     LOGGER.error("Error reading config file {}\n{}", configFile, e);
	// }

	// get the URL of the configuration file
	String configFile = OxbyteiConstants.getConfigFile();
	try {
	    // get URL of currently edited file
	    URL currentFileURL = new URL(context.getSystemID());
	    // get an expander for editor variables
	    EditorVariablesExpander expander = new EditorVariablesExpanderImpl(currentFileURL, true);

	    List<Object> docNodes = context.executeXPath(OxbyteiConstants.DOCUMENT_XPATH, namespaces, true);
	    Document document = (Document) docNodes.get(0);
	    String ctx =
		context.computeContextXPathExpression().replaceAll("/", "/*:"); //.substring(1);

	    LOGGER.debug("Count of docNodes: {}", docNodes.size());
	    LOGGER.debug("Type of first in docNodes: {}, name of root element: {}",
			 docNodes.get(0).getClass().getCanonicalName(),
			 ((Document) docNodes.get(0)).getDocumentElement().getLocalName());

	    // get the initialized providers
	    ConfiguredPluginLoader<ILabelledEntriesProvider> providerLoader =
		new ConfiguredPluginLoader<ILabelledEntriesProvider>
		(ILabelledEntriesProvider.class, OxbyteiConstants.DEFAULT_LABELLED_ENTRIES_PROVIDER);
	    List<ILabelledEntriesProvider> providers =
		providerLoader.providersForContext
		(document,
		 ctx,
		 nodeType,
		 nodeName,
		 null,
		 configFile,
		 expander);

	    // setup each provider
	    for (ILabelledEntriesProvider provider : providers) {
		provider.setup
		    (uriResolver,
		     entityResolver,
		     document,
		     currentFileURL.toString(),
		     ctx);
	    }

	    return providers;
	} catch (MalformedURLException e) {
	    LOGGER.error("Path to current edited file is not a valid URL: {}", context.getSystemID());
	} catch (IndexOutOfBoundsException e) {
	    LOGGER.error("No document node found {}", e);
	} catch (ConfigurationException e) {
	    LOGGER.error("{}", e);
	} catch (ExtensionException e) {
	    LOGGER.error("{}", e);
	}
	return null;

    }

    private static WSEditorPage getWsEditorPage() {
	// we can get a schema manager either in text mode or in author mode
	PluginWorkspace ws = PluginWorkspaceProvider.getPluginWorkspace();
	WSEditorPage page = ws.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA).getCurrentPage();
	return page;
    }


    @Override
    public List<CIValue> filterAttributeValues(List<CIValue> list, WhatPossibleValuesHasAttributeContext context) {

	// only for text mode
	if (WSXMLTextEditorPage.class.isAssignableFrom(getWsEditorPage().getClass())) {
	    // get some relevant context information
	    String attributeName = context.getAttributeName();
	    String alreadyTyped = context.getCurrentValuePrefix();

	    List<ILabelledEntriesProvider> providers =
		providersForContext(context, ExtensionConfiguration.ATTRIBUTE_VALUE, attributeName);

	    List<CIValue> suggestions = new ArrayList<CIValue>();
	    for (ILabelledEntriesProvider provider : providers) {
		if (OxygenBasedSchemaProvider.class.isAssignableFrom(provider.getClass())) {
		    // we filter out all plugins based on oxygen
		    // schema manager because asking them for
		    // suggestions would result in an infinite loop.
		    continue;
		}
		try {
		    List<LabelledEntry> entries = provider.getLabelledEntries(alreadyTyped);
		    for (LabelledEntry entry : entries) {
			suggestions.add(new CIValue(entry.getKey(), entry.getLabel()));
		    }
		} catch (ExtensionException e) {
		    LOGGER.error("Error getting values from plugin {}:\n{}",
				 provider.getClass().getCanonicalName(), e);
		}
	    }
	    suggestions.addAll(list);

	    // // Dummy implementation
	    // suggestions.add(new CIValue("Hello", "There is a description.\n\n"
	    // 			    + contextXPath
	    // 			    + "\n\nYou typed: "
	    // 			    + alreadyTyped));

	    return suggestions;
	} else {
	    // in all other modes we do not add suggestions from the plugins
	    return list;
	}
    }

    @Override
    public List<CIElement> filterElements(final List<CIElement> list, final WhatElementsCanGoHereContext context) {
        return list;
    }

    @Override
    public List<CIAttribute> filterAttributes(final List<CIAttribute> list, final WhatAttributesCanGoHereContext context) {
        return list;
    }

    @Override
    public List<CIValue> filterElementValues(final List<CIValue> list, final Context context) {
        return list;
    }

}

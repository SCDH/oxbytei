package de.wwu.scdh.oxbytei;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.xml.xpath.XPath;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.util.UtilAccess;
import ro.sync.ecss.extensions.api.AuthorAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.teilsp.config.*;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;


public class ConfigurationReader {


    public static List<ExtensionConfiguration> getExtensionsConfiguration(URL currentEditedURL, String configURL)
	throws ConfigurationException {
	final Logger LOGGER = LoggerFactory.getLogger(ConfigurationReader.class);
	UtilAccess utilAccess = PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess();
	//List<ExtensionConfiguration> expandedPlugins = new ArrayList<ExtensionConfiguration>();
	List<ExtensionConfiguration> plugins =
	    ExtensionConfigurationReader.getExtensionsConfiguration(configURL);
	String expanded, unexpanded;
	Map<String, String> keyvals;
	for (ExtensionConfiguration extensionConfiguration : plugins) {
	    List<ArgumentsConditionsPair> specs = extensionConfiguration.getSpecification();
	    for (ArgumentsConditionsPair spec : specs) {
		keyvals = spec.getConditions();
		for (String name : keyvals.keySet()) {
		    unexpanded = keyvals.get(name);
		    expanded = utilAccess.expandEditorVariables(unexpanded, currentEditedURL, true);
		    while (! expanded.equals(unexpanded)) {
			LOGGER.error("Expanded {} from {} to {}", name, unexpanded, expanded);
			unexpanded = expanded;
			expanded = utilAccess.expandEditorVariables(unexpanded, currentEditedURL, true);
		    }
		    keyvals.put(name, expanded);
		}
		keyvals = spec.getArguments();
		for (String name : keyvals.keySet()) {
		    unexpanded = keyvals.get(name);
		    expanded = utilAccess.expandEditorVariables(unexpanded, currentEditedURL, true);
		    while (! expanded.equals(unexpanded)) {
			LOGGER.error("Expanded {} from {} to {}", name, unexpanded, expanded);
			unexpanded = expanded;
			expanded = utilAccess.expandEditorVariables(unexpanded, currentEditedURL, true);
		    }
		    keyvals.put(name, expanded);
		}
	    }
	}

	return plugins;
    }
}

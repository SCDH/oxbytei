package de.wwu.scdh.oxbytei;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.xml.xpath.XPath;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.wwu.scdh.teilsp.config.*;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;


public class ConfigurationReader {

    public static List<ExtensionConfiguration> getExtensionsConfiguration(String configURL)
	throws ConfigurationException {
	List<ExtensionConfiguration> expandedPlugins = new ArrayList<ExtensionConfiguration>();
	List<ExtensionConfiguration> plugins =
	    ExtensionConfigurationReader.getExtensionsConfiguration(configURL);
	for (ExtensionConfiguration extensionConfiguration : plugins) {
	    
	}

	return expandedPlugins;
    }
}

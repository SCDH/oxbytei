/**
 * {@link OxbyteiConstants}
 * Defining constants for the oXbytei extension
 *
 */
package de.wwu.scdh.oxbytei;

import java.io.File;
import java.nio.file.Paths;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerException;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;



public class OxbyteiConstants {

    /**
     * URL of the default plugin configuration
     */
    public static final String DEFAULT_CONFIG_FILE = "${framework}/config/default.xml";

    public static String getExtensionRoot() {
	// get the URL of the configuration file
	File jarFile = new File(OxbyteiConstants.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	String path = jarFile.getParent();
	return path;
    }

    public static String getConfigFile() {
	String defaultConfigFile = Paths.get(getExtensionRoot(), "config", "default.xml").toString();
	URIResolver resolver =
	    PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess().getURIResolver();
	String configFile = defaultConfigFile;
	try {
	    // use resolver with xml catalogs
	    configFile = resolver.resolve(defaultConfigFile, null).getSystemId();
	} catch (TransformerException e) {}
	return configFile;
    }

}

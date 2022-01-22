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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;



public class OxbyteiConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(OxbyteiConstants.class);

    public static final String DOCUMENT_XPATH = "root(/)";

    public static final String CONTEXT_XPATH =
	"string-join(for $node in ancestor-or-self::* return concat('*:', name($node), '[', count($node/preceding-sibling::*[name() eq name($node)]) + 1, ']'), '/')";

    // FIXME
    public static final String NAMESPACE_RE = "[a-zA-Z0-9]+";

    // FIXME
    public static final String XPATH_RE = "[\\[\\]\\(\\)\\s@/:.;!?|,*'=>#a-zA-Z0-9_-]+";

    public static final String XPATH_QUOTED = "\"(" + XPATH_RE + ")\"|("+ XPATH_RE.replaceAll(",", "") + ")";

    /**
     * URL of the default plugin configuration
     */
    public static final String DEFAULT_CONFIG_FILE = "${framework(oXbytei)}/config/default.xml";

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
	LOGGER.debug("Using oXbytei config file {}", configFile);
	return configFile;
    }

}

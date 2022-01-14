package de.wwu.scdh.oxbytei;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ro.sync.exml.workspace.api.util.EditorVariableDescription;
import ro.sync.exml.workspace.api.util.EditorVariablesResolver;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.util.UtilAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.teilsp.config.ExtensionConfigurationReader;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;

/**
 * {@link OxbyteiEditorVariables} is a resolver for editor variables
 * defined in oXbytei.
 */
public class OxbyteiEditorVariables
    extends EditorVariablesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(OxbyteiEditorVariables.class);
    
    public static final EditorVariableDescription VARIABLE_CONFIG_PROPERTY =
	new EditorVariableDescription("teilspProp",
				      "A property set in the configuration file.");

    public static final Pattern CONFIG_PROPERTY_PATTERN =
	Pattern.compile("\\$\\{" + VARIABLE_CONFIG_PROPERTY.getName() + "\\(([\\.a-zA-Z0-9]+)\\)\\}");

    public static final EditorVariableDescription VARIABLE_ANCHOR_START_ID =
	new EditorVariableDescription("startAnchorId",
				      "@xml:id of last inserted start anchor.");

    public static final Pattern START_ANCHOR_ID_PATTERN =
	Pattern.compile("\\$\\{" + VARIABLE_ANCHOR_START_ID.getName() + "\\}");

    public static final EditorVariableDescription VARIABLE_ANCHOR_END_ID =
	new EditorVariableDescription("endAnchorId",
				      "@xml:id of last inserted end anchor.");

    public static final Pattern END_ANCHOR_ID_PATTERN =
	Pattern.compile("\\$\\{" + VARIABLE_ANCHOR_END_ID.getName() + "\\}");

    public static final EditorVariableDescription VARIABLE_ANCHORS_CONTAINER =
	new EditorVariableDescription("anchorsContainer",
				      "XPath expression identifying the element node, that contains both anchors.");

    public static final Pattern ANCHORS_CONTAINER_PATTERN =
	Pattern.compile("\\$\\{" + VARIABLE_ANCHORS_CONTAINER.getName() + "\\}");

    public List<EditorVariableDescription> getCustomResolverEditorVariableDescriptions() {
	List<EditorVariableDescription> descriptions = new ArrayList<EditorVariableDescription>();
	descriptions.add(VARIABLE_CONFIG_PROPERTY);
	descriptions.add(VARIABLE_ANCHOR_START_ID);
	descriptions.add(VARIABLE_ANCHOR_END_ID);
	descriptions.add(VARIABLE_ANCHORS_CONTAINER);
	return descriptions;
    }

    public String resolveEditorVariables(String contentWithEditorVariables, String currentEditedFile) {
	LOGGER.debug("Trying to resolve editor variables in '{}'", contentWithEditorVariables);
	String resolved = contentWithEditorVariables;
	Matcher matcher;
	// resolve ${teilspProp(...)
	matcher = CONFIG_PROPERTY_PATTERN.matcher(resolved);
	while (matcher.matches()) {
	    String propertyName = matcher.group(1);
	    // we get the config file in the loop, because this method
	    // will be called very often without this editor
	    // variables, too.
	    String configFile = OxbyteiConstants.getConfigFile();
	    try {
		String property = ExtensionConfigurationReader.getProperty(propertyName, configFile);
		resolved = matcher.replaceFirst(Matcher.quoteReplacement(property));
		LOGGER.debug("Resolved {} to {}", contentWithEditorVariables, resolved);
	    } catch (ConfigurationException e) {
		LOGGER.error("Property '{}' not found in {}\n{}", contentWithEditorVariables, configFile, e);
		break; // avoid infinit loop
	    } catch (Exception e) {
		LOGGER.error("{}", e);
		break;
	    }
	    // resolve more ocurrences
	    matcher = CONFIG_PROPERTY_PATTERN.matcher(resolved);
	}

	// resolve ${startAnchorID
	matcher = START_ANCHOR_ID_PATTERN.matcher(resolved);
	while (matcher.find()) {
	    resolved = matcher.replaceFirst(GlobalState.startAnchorId);
	    matcher = START_ANCHOR_ID_PATTERN.matcher(resolved);
	}

	// resolve ${endAnchorID
	matcher = END_ANCHOR_ID_PATTERN.matcher(resolved);
	while (matcher.find()) {
	    resolved = matcher.replaceFirst(GlobalState.endAnchorId);
	    matcher = END_ANCHOR_ID_PATTERN.matcher(resolved);
	}

	// resolve ${anchorsContainer
	//LOGGER.error("anchors container: {}", GlobalState.anchorsContainer);
	matcher = ANCHORS_CONTAINER_PATTERN.matcher(resolved);
	while (matcher.find()) {
	    String str = GlobalState.anchorsContainer;
	    LOGGER.error("resolved anchors container: {}", str);
	    resolved = matcher.replaceFirst(Matcher.quoteReplacement(GlobalState.anchorsContainer));
	    matcher = ANCHORS_CONTAINER_PATTERN.matcher(resolved);
	}

	return resolved;
    }
}

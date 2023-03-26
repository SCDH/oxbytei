package de.wwu.scdh.teilsp.services.extensions;

import java.util.Map;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;


/**
 * {@link ConfigurablePlugin} - an interface for plugins, that
 * get configuration parameters from the configuration file.
 */
public interface ConfigurablePlugin {

    /**
     * {@link init} initializes the provider with a arguments from
     * configuration. It is once called during the initialization
     * phase.
     *
     * @param  kwargs      key value pairs with configuration parameters
     */
    public void init(Map<String, String> kwargs) throws ConfigurationException;

    /**
     * {@link getArguments} returns configuration arguments. This
     * might be informative for debugging a error messages.
     */
    public Map<String, String> getArguments();

    /**
     * This returns an array of argument descriptors implementing
     * {@link ArgumentDescriptor}s. These can be used to validate the
     * configuration, get the values or get a description of the
     * arguments.
     *
     * @return an array of argument descriptors
     */
    public ArgumentDescriptor<?>[] getArgumentDescriptor();

    /**
     * A descriptor for the special argument that is always present
     * and stores the node type.
     *
     * See {@link de.wwu.scdh.teilsp.config.ExtensionConfiguration#NODE_TYPES}
     * for node types.
     */
    public static final ArgumentDescriptor<String> SPECIAL_ARGUMENT_NODE_TYPE =
	new ArgumentDescriptorImpl<String>
	(String.class, "__oxbytei_nodeType__",
	 "The special argument that stores the node type.",
	 ExtensionConfiguration.NODE_TYPES);

    /**
     * A descriptor for the special argument that is always present
     * and stores the node name.
     */
    public static final ArgumentDescriptor<String> SPECIAL_ARGUMENT_NODE_NAME =
	new ArgumentDescriptorImpl<String>
	(String.class, "__oxbytei_nodeName__",
	 "The special argument that stores the node name.");

}

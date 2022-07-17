package de.wwu.scdh.teilsp.services.extensions;

import java.util.Map;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;


/**
 * {@link ConfigurablePlugin} - an interface for plugins, that
 * get configuration parameters from the configuration file.
 */
public interface ConfigurablePlugin {

    /**
     * {@link init} initializes the provider with a setup from
     * configuration and the current editing context. It is once
     * called during the initialization phase.
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
}

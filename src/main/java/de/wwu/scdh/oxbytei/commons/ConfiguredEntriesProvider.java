package de.wwu.scdh.oxbytei.commons;

import java.util.Map;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.tei.PrefixDef;

// a nested class to keep track of configured plugins
public class ConfiguredEntriesProvider {

    private ILabelledEntriesProvider provider;
    private Map<String, String> arguments;
    private PrefixDef prefixDef;

    public ConfiguredEntriesProvider(ILabelledEntriesProvider p, Map<String, String> args, PrefixDef prefix) {
	provider = p;
	arguments = args;
	prefixDef = prefix;
    }

    public ILabelledEntriesProvider getProvider() {
	return provider;
    }

    public Map<String, String> getArguments() {
	return arguments;
    }

    public PrefixDef getPrefixDef() {
	return prefixDef;
    }
}



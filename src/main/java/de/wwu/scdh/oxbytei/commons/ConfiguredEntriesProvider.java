package de.wwu.scdh.oxbytei.commons;

import java.util.Map;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.tei.PrefixDef;

// a nested class to keep track of configured plugins
public class ConfiguredEntriesProvider {
    public ILabelledEntriesProvider provider;
    public Map<String, String> arguments;
    public PrefixDef prefixDef;
    public ConfiguredEntriesProvider(ILabelledEntriesProvider p, Map<String, String> args, PrefixDef prefix) {
	provider = p;
	arguments = args;
	prefixDef = prefix;
    }
}



package de.wwu.scdh.oxbytei.commons;

import java.util.HashMap;
import java.util.Map;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;

/**
 * {@link UpdatableArgumentsMap} is an implementation of 
 * {@link ro.sync.ecss.extensions.api.ArgumentsMap}, that provides an
 * update method.
 */
public class UpdatableArgumentsMap implements ArgumentsMap {

    private HashMap<String, Object> map;

    /**
     * Constructor
     * @param args an existing arguments map
     * @param desc an argument descriptor array that has the names of
     * the arguments
     */
    public UpdatableArgumentsMap(ArgumentsMap args, ArgumentDescriptor[] desc) {
	// make a new map from old map
	map = new HashMap<String, Object>();
	// we know the attribute names from the argument descriptors
	for (int i = 0; i < desc.length; i++) {
		map.put(desc[i].getName(), args.getArgumentValue(desc[i].getName()));
	}
    }

    /**
     * {@see ro.sync.ecss.extensions.api.ArgumentsMap#getArgumentValue(String)}
     */
    public Object getArgumentValue(String name) {
	return map.get(name);
    }

    /**
     * Update an argument value.
     */
    public void update(String name, Object value) {
	map.put(name, value);
    }

}

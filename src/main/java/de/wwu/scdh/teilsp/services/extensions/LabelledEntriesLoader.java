package de.wwu.scdh.teilsp.services.extensions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import de.wwu.scdh.teilsp.exceptions.ProviderNotFoundException;

/**
 * {@link LabelledEntriesLoader} - a class for loading plugins
 * registered for {@link ILabelledEntriesProvider} through the service
 * provider interface (SPI).
 */
public final class LabelledEntriesLoader {

    private static final String DEFAULT_PROVIDER = "de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXML";

    /**
     * Returns all providers in the classpath.
     *
     */
    public static List<ILabelledEntriesProvider> providers() {
	List<ILabelledEntriesProvider> services = new ArrayList<>();
	ServiceLoader<ILabelledEntriesProvider> loader = ServiceLoader.load(ILabelledEntriesProvider.class);
	loader.forEach(labelledEntriesProvider -> {
		services.add(labelledEntriesProvider);
	});
	return services;
    }

    /**
     * Get provider by name.
     *
     */
    public static ILabelledEntriesProvider provider(String providerName)
	throws ProviderNotFoundException {
	ServiceLoader<ILabelledEntriesProvider> loader = ServiceLoader.load(ILabelledEntriesProvider.class);
	Iterator<ILabelledEntriesProvider> iter = loader .iterator();
	while (iter.hasNext()) {
	    ILabelledEntriesProvider provider = iter.next();
	    if (providerName.equals(provider.getClass().getName())) {
		return provider;
	    }
	}
	throw new ProviderNotFoundException("ILabelledEntriesProvider " + providerName + " not found");
    }
    
    /**
     * Overloaded method, that returns the default provider.
     *
     */
    public static ILabelledEntriesProvider provider()
	throws ProviderNotFoundException {
	return provider(DEFAULT_PROVIDER);
    }

}

package de.wwu.scdh.teilsp.services.extensions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import de.wwu.scdh.teilsp.exceptions.ProviderNotFoundException;


public class LabelledEntriesLoaderTest {

    @Test
    public void testAtLeastDefaultProvider() {
	List<ILabelledEntriesProvider> providers = LabelledEntriesLoader.providers();
	assertTrue(providers.size() > 0);
    }

    @Test
    public void testDefaultProvider() throws ExtensionException, ProviderNotFoundException {
	assertEquals("de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXML",
		     LabelledEntriesLoader.provider().getClass().getName());
    }

    @Test
    public void testProviderNotFound() throws ExtensionException {
	assertThrows(ProviderNotFoundException.class,
		     () -> { LabelledEntriesLoader.provider("unknown");
		     });
    }
}

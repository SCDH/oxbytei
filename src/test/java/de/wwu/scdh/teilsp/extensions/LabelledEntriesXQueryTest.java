package de.wwu.scdh.teilsp.extensions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

import de.wwu.scdh.teilsp.exceptions.*;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;


public class LabelledEntriesXQueryTest {

    LabelledEntriesXQuery plugin;

        Map<String, String> args;

    String resources;
    String personsUrl, keylabelUrl, missingUrl, personography;

    List<LabelledEntry> result;

    @BeforeEach
    void setup()
	throws MalformedURLException, IOException {
	plugin = new LabelledEntriesXQuery();
	args = new HashMap<String, String>();

	resources = Paths.get("src", "test", "resources").toFile().getAbsolutePath();
	missingUrl = "file:" + Paths.get("src", "test", "resources", "missing.xql").toFile().getAbsolutePath();
	personsUrl = "file:" + Paths.get("src", "test", "resources", "persons.xql").toFile().getAbsolutePath();
	personography = "file:" + Paths.get("src", "test", "resources", "teigraphy.xml").toFile().getAbsolutePath();
	keylabelUrl = "file:" + Paths.get("src", "test", "resources", "keylabel.xql").toFile().getAbsolutePath();
    }

    @AfterEach
    void tearDown()
	throws IOException {
    }

    @Test
    void testUrlRequired()
	throws ConfigurationException, ExtensionException {
	args = new HashMap<String, String>();
	Exception exception = assertThrows(ConfigurationException.class, () -> plugin.init(args));
	assertEquals("Required argument 'xquery' is missing", exception.getMessage());
    }

    @Test
    void testUrlMissing()
	throws ConfigurationException, ExtensionException {
	args = new HashMap<String, String>();
	args.put("xquery", missingUrl);
	Exception exception = assertThrows(ConfigurationException.class, () -> plugin.init(args));
	assertEquals("java.io.FileNotFoundException", exception.getMessage().substring(0, 29));
    }

    @Test
    void testMalformedExternal()
	throws ConfigurationException, ExtensionException {
	args = new HashMap<String, String>();
	args.put("xquery", personsUrl);
	args.put("external", "personography-" + personography);
	Exception exception = assertThrows(ConfigurationException.class, () -> plugin.init(args));
	assertEquals("Bad arguments: personography-", exception.getMessage().substring(0, 29));
    }

    @Test
    void testPersonsKeyLabel()
	throws ConfigurationException, ExtensionException {
	args.put("xquery", personsUrl);
	args.put("external", "personography=" + personography);
	plugin.init(args);
	plugin.setup(null, null, null, null, null);
	result = plugin.getLabelledEntries("");
	assertEquals(3, result.size());
	assertEquals("#FCSavigny", result.get(0).getKey());
	assertEquals("Friedrich Carl Savigny", result.get(0).getLabel());
    }

}

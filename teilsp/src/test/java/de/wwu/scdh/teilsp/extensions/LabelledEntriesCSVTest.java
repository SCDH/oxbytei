package de.wwu.scdh.teilsp.extensions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import de.wwu.scdh.teilsp.exceptions.*;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;


public class LabelledEntriesCSVTest {

    LabelledEntriesCSV csvReader;
    Map<String, String> args;

    String resources;
    String personsUrl, keylabelUrl, missingUrl;

    List<LabelledEntry> result;

    @BeforeEach
    void setup()
	throws MalformedURLException, IOException {
	csvReader = new LabelledEntriesCSV();
	args = new HashMap<String, String>();

	resources = Paths.get("src", "test", "resources").toFile().getAbsolutePath();
	personsUrl = "file:" + Paths.get("src", "test", "resources", "persons.csv").toFile().getAbsolutePath();
	keylabelUrl = "file:" + Paths.get("src", "test", "resources", "keylabel.csv").toFile().getAbsolutePath();
	missingUrl = "file:" + Paths.get("src", "test", "resources", "missing.csv").toFile().getAbsolutePath();
    }

    @AfterEach
    void tearDown()
	throws IOException {
    }

    @Test
    void testUrlRequired()
	throws ConfigurationException, ExtensionException {
	args = new HashMap<String, String>();
	Exception exception = assertThrows(ConfigurationException.class, () -> csvReader.init(args));
	assertEquals("Required argument 'url' is missing", exception.getMessage());
    }

    @Test
    void testFileMissing()
	throws ConfigurationException, ExtensionException {
	args.put("url", missingUrl);
	csvReader.init(args);
	csvReader.setup(null, null, null, null, null);
	Exception exception = assertThrows(ExtensionException.class, () -> csvReader.getLabelledEntries(""));
	assertEquals("Error reading from file:", exception.getMessage().substring(0, 24));
    }

    @Test
    void testPersonsDefaultKeyLabel()
	throws ConfigurationException, ExtensionException {
	args.put("url", personsUrl);
	csvReader.init(args);
	csvReader.setup(null, null, null, null, null);
	result = csvReader.getLabelledEntries("");
	assertEquals(2, result.size());
	assertEquals("null", result.get(1).getKey());
	assertEquals("null", result.get(1).getLabel());
    }

    @Test
    void testPersonsWithKeyLabel()
	throws ConfigurationException, ExtensionException {
	args.put("url", personsUrl);
	args.put("labelColumns", "first");
	args.put("keyColumns", "last");
	csvReader.init(args);
	csvReader.setup(null, null, null, null, null);
	result = csvReader.getLabelledEntries("");
	assertEquals(2, result.size());
	assertEquals("McCallister", result.get(0).getKey());
	assertEquals("Kevin", result.get(0).getLabel());
    }

    @Test
    void testPersonsWithKeyLabelDefaultInterconnector()
	throws ConfigurationException, ExtensionException {
	args.put("url", personsUrl);
	args.put("keyColumns", "first,age");
	args.put("labelColumns", "first,last,age");
	csvReader.init(args);
	csvReader.setup(null, null, null, null, null);
	result = csvReader.getLabelledEntries("");
	assertEquals(2, result.size());
	assertEquals("Kevin8", result.get(0).getKey());
	assertEquals("KevinMcCallister8", result.get(0).getLabel());
    }

    @Test
    void testPersonsWithKeyLabelWithInterconnectors()
	throws ConfigurationException, ExtensionException {
	args.put("url", personsUrl);
	args.put("keyColumns", "first,age");
	args.put("keySeparators", "-,");
	args.put("labelColumns", "first,last,age");
	args.put("labelSeparators", " , aged ");
	csvReader.init(args);
	csvReader.setup(null, null, null, null, null);
	result = csvReader.getLabelledEntries("");
	assertEquals(2, result.size());
	assertEquals("Kevin-8", result.get(0).getKey());
	assertEquals("Kevin McCallister aged 8", result.get(0).getLabel());
	assertEquals(2, result.size());
	assertEquals("Peter-", result.get(1).getKey());
	assertEquals("Peter McCallister aged ", result.get(1).getLabel());
    }

    @Test
    void testKeyLabelDefaultColumns()
	throws ConfigurationException, ExtensionException {
	args.put("url", keylabelUrl);
	csvReader.init(args);
	csvReader.setup(null, null, null, null, null);
	result = csvReader.getLabelledEntries("");
	assertEquals(2, result.size());
	assertEquals("5", result.get(0).getKey());
	assertEquals("3", result.get(0).getLabel());
    }

    @Test
    void testKeyLabelPrefix()
	throws ConfigurationException, ExtensionException {
	args.put("url", keylabelUrl);
	args.put("prefix", "kv:");
	args.put("labelPrefix", "Label = ");
	csvReader.init(args);
	csvReader.setup(null, null, null, null, null);
	result = csvReader.getLabelledEntries("");
	assertEquals(2, result.size());
	assertEquals("kv:5", result.get(0).getKey());
	assertEquals("Label = 3", result.get(0).getLabel());
    }

    @Test
    void testFormatUnkown()
	throws ConfigurationException, ExtensionException {
	args.put("url", keylabelUrl);
	args.put("format", "mine");
	Exception exception = assertThrows(ConfigurationException.class, () -> csvReader.init(args));
	assertEquals("Unknown CSV format mine", exception.getMessage());
    }

    @Test
    void testFormatExcel()
	throws ConfigurationException, ExtensionException {
	args.put("url", keylabelUrl);
	args.put("format", "Excel");
	csvReader.init(args);
    }


}

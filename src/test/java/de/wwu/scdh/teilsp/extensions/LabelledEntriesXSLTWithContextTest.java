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
import javax.xml.transform.URIResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import de.wwu.scdh.teilsp.exceptions.*;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.testutils.SimpleURIResolver;


public class LabelledEntriesXSLTWithContextTest {

    LabelledEntriesXSLTWithContext plugin;
    URIResolver uriResolver = new SimpleURIResolver();
    Document emptyDoc;

    Map<String, String> args;

    String resources;
    String persons, keylabel, missing, personography;

    List<LabelledEntry> result;

    @BeforeEach
    void setup()
	throws MalformedURLException, IOException, ParserConfigurationException {
	plugin = new LabelledEntriesXSLTWithContext();
	args = new HashMap<String, String>();

	resources = Paths.get("src", "test", "resources").toFile().getAbsolutePath();
	missing = "file:" + Paths.get("src", "test", "resources", "missing.xsl").toFile().getAbsolutePath();
	persons = "file:" + Paths.get("src", "test", "resources", "persons.xsl").toFile().getAbsolutePath();
	personography = "file:" + Paths.get("src", "test", "resources", "teigraphy.xml").toFile().getAbsolutePath();
	keylabel = "file:" + Paths.get("src", "test", "resources", "keylabel.xsl").toFile().getAbsolutePath();

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
	emptyDoc = builder.newDocument();
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
	assertEquals("Required argument 'script' is missing", exception.getMessage());
    }

    @Test
    void testUrlMissing()
	throws ConfigurationException, ExtensionException {
	args = new HashMap<String, String>();
	args.put("script", missing);
	plugin.init(args);
	plugin.setup(uriResolver, null, emptyDoc, "phantasy", "/*");
	Exception exception = assertThrows(ExtensionException.class, () -> plugin.getLabelledEntries(""));
	assertEquals("java.io.FileNotFoundException", exception.getMessage().substring(0, 29));
    }

    @Test
    void testMalformedExternal()
	throws ConfigurationException, ExtensionException {
	args = new HashMap<String, String>();
	args.put("script", persons);
	args.put("parameters", "personography-" + personography);
	Exception exception = assertThrows(ConfigurationException.class, () -> plugin.init(args));
	assertEquals("Bad arguments: personography-", exception.getMessage().substring(0, 29));
    }

    @Test
    void testPersonsKeyLabel()
	throws ConfigurationException, ExtensionException, Exception {
	args.put("script", persons);
	args.put("parameters", "personography=" + personography);
	plugin.init(args);
	plugin.setup(uriResolver, null, emptyDoc, "phantasy", "/*");
	result = plugin.getLabelledEntries("");
	assertEquals(3, result.size());
	assertEquals("#FCSavigny", result.get(0).getKey());
	assertEquals("Friedrich Carl von Savigny", result.get(0).getLabel());
    }

}

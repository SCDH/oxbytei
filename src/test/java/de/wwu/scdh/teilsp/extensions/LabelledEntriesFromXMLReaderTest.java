package de.wwu.scdh.teilsp.extensions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.wwu.scdh.teilsp.exceptions.DocumentReaderException;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;


public class LabelledEntriesFromXMLReaderTest {

    LabelledEntriesFromXMLReader reader;

    String resources;
    FileInputStream teigraphy;
    String teigraphyNs;
    String prefix;

    @BeforeEach
    void setup()
	throws FileNotFoundException, IOException {
	resources = Paths.get("src", "test", "resources").toFile().getAbsolutePath();
	String teigraphyPath = Paths.get("src", "test", "resources", "teigraphy.xml").toFile().getAbsolutePath();
	teigraphy = new FileInputStream(teigraphyPath);
	teigraphyNs = "t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace";
	prefix = "psn:";
    }

    @AfterEach
    void tearDown() throws IOException {
	teigraphy.close();
    }
	
    @Test
    @DisplayName("Do the tests run? Are testing resources present?")
    void testTestingItSelf() {
	assertTrue(true);
    }

    @Test
    @DisplayName("Test keys with teigraphy.xml")
    void testRegistryKeys()
	throws DocumentReaderException {
	reader = new LabelledEntriesFromXMLReader(prefix, teigraphy,
					     "//t:person",
					     "@xml:id",
					     "'const'",
					     teigraphyNs);
	assertEquals(3, reader.nodesCount());
	assertEquals(3, reader.getLength());
	LabelledEntry[] entries = reader.getEntries();
	assertEquals(3, entries.length);
	assertEquals("psn:FCSavigny", entries[0].getKey());
	assertEquals("psn:Puchta", entries[1].getKey());
	assertEquals("psn:JGrimm", entries[2].getKey());
	// test the constant xpath expression for the label:
	assertEquals("const", entries[0].getLabel());
    }

    @Test
    @DisplayName("Test labels with teigraphy.xml")
    void testRegistryLabels()
	throws DocumentReaderException {
	reader = new LabelledEntriesFromXMLReader(prefix, teigraphy,
					     "//t:person",
					     "@xml:id",
					     "normalize-space(t:persName)",
					     teigraphyNs);
	assertEquals(3, reader.nodesCount());
	assertEquals(3, reader.getLength());
	LabelledEntry[] entries = reader.getEntries();
	assertEquals(3, entries.length);
	assertEquals("Friedrich Carl von Savigny", entries[0].getLabel());
	assertEquals("Georg Friedrich Puchta", entries[1].getLabel());
	assertEquals("Jacob Grimm", entries[2].getLabel());
    }

    @Test
    @DisplayName("Test with teigraphy.xml xpath with arbitrary namespace *. This does not work!")
    void testRegistryArbitraryNamespace()
	throws DocumentReaderException {
	assertThrows(DocumentReaderException.class,
		     () -> new LabelledEntriesFromXMLReader(prefix, teigraphy, "//*:person", "@xml:id", "*", ""));
    }

    @Test
    @DisplayName("Test with teigraphy.xml with default namespace")
    // This does not work with NamespaceContext
    void testRegistryDefaultNamespace()
	throws DocumentReaderException {
	reader = new LabelledEntriesFromXMLReader(prefix, teigraphy,
					     "//person", "@xml:id", "*", "http://www.tei-c.org/ns/1.0");
	assertEquals(0, reader.nodesCount());
    }

}

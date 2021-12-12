package de.wwu.scdh.teilsp.completion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.wwu.scdh.teilsp.exceptions.DocumentReaderException;


public class SelectionItemsXMLReaderTest {

    SelectionItemsXMLReader reader;

    String resources;
    String teigraphy;

    @BeforeEach
    void setup() {
	resources = Paths.get("src", "test", "resources").toFile().getAbsolutePath();
	teigraphy = Paths.get("src", "test", "resources", "teigraphy.xml").toFile().getAbsolutePath();
    }

    @Test
    @DisplayName("Do the tests run? Are testing resources present?")
    void testTestingItSelf() {
	assertTrue(true);
	assertTrue(new File(teigraphy).exists());
    }

    @Test
    @DisplayName("Test keys with teigraphy.xml")
    void testRegistryKeys()
	throws FileNotFoundException, IOException, DocumentReaderException {
	FileInputStream input = new FileInputStream(teigraphy);
	System.out.println(input);
	PrefixDef prefix = new PrefixDef("([a-zA-Z0-9_-]+)", "teigraphy.xml#$1", "psn");
	reader = new SelectionItemsXMLReader(prefix, input,
					     "//t:person",
					     "$XPATH{@xml:id}",
					     "$XPATH{'const'}",
					     "t:http://www.tei-c.org/ns/1.0");
	assertEquals(3, reader.nodesCount());
	assertEquals(3, reader.getLength());
	LabelledEntry[] entries = reader.getEntries();
	assertEquals(3, entries.length);
	assertEquals("psn:FCSavigny", entries[0].getKey());
	assertEquals("psn:Puchta", entries[1].getKey());
	assertEquals("psn:JGrimm", entries[2].getKey());
	input.close();
    }

    @Test
    @Disabled("This test is pending: Labels do not work yet.")
    @DisplayName("Test labels with teigraphy.xml")
    void testRegistryLabels()
	throws FileNotFoundException, IOException, DocumentReaderException {
	FileInputStream input = new FileInputStream(teigraphy);
	System.out.println(input);
	PrefixDef prefix = new PrefixDef("([a-zA-Z0-9_-]+)", "teigraphy.xml#$1", "psn");
	reader = new SelectionItemsXMLReader(prefix, input,
					     "//t:person",
					     "$XPATH{@xml:id}",
					     "$XPATH{normalize-space(t:persName)}",
					     "t:http://www.tei-c.org/ns/1.0");
	assertEquals(3, reader.nodesCount());
	assertEquals(3, reader.getLength());
	LabelledEntry[] entries = reader.getEntries();
	assertEquals(3, entries.length);
	assertEquals("Friedrich Carl von Savigny ", entries[0].getLabel());
	input.close();
    }

    @Test
    @DisplayName("Test with teigraphy.xml xpath with arbitrary namespace *. This does not work!")
    void testRegistryArbitraryNamespace()
	throws FileNotFoundException, IOException, DocumentReaderException {
	FileInputStream input = new FileInputStream(teigraphy);
	System.out.println(input);
	PrefixDef prefix = new PrefixDef("([a-zA-Z0-9_-]+)", "teigraphy.xml#$1", "psn");
	assertThrows(DocumentReaderException.class,
		     () -> new SelectionItemsXMLReader(prefix, input, "//*:person", "@xml:id", "*", ""));
	input.close();
    }

    @Test
    @DisplayName("Test with teigraphy.xml with default namespace")
    // This does not work with NamespaceContext
    void testRegistryDefaultNamespace()
	throws FileNotFoundException, IOException, DocumentReaderException {
	FileInputStream input = new FileInputStream(teigraphy);
	System.out.println(input);
	PrefixDef prefix = new PrefixDef("([a-zA-Z0-9_-]+)", "teigraphy.xml#$1", "psn");
	reader = new SelectionItemsXMLReader(prefix, input,
					     "//person", "@xml:id", "*", "http://www.tei-c.org/ns/1.0");
	assertEquals(0, reader.nodesCount());
	input.close();
    }

}

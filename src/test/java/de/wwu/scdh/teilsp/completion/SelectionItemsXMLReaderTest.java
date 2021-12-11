package de.wwu.scdh.teilsp.completion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
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
    @DisplayName("Test with teigraphy.xml")
    void testRegistry()
	throws FileNotFoundException, IOException, DocumentReaderException {
	FileInputStream input = new FileInputStream(teigraphy);
	System.out.println(input);
	PrefixDef prefix = new PrefixDef("([a-zA-Z0-9_-]+)", "teigraphy.xml#$1", "psn");
	reader = new SelectionItemsXMLReader(prefix, input,
					     "//t:person", "@xml:id", "*", "t:http://www.tei-c.org/ns/1.0");
	assertEquals(3, reader.nodesCount());
	input.close();
    }

    @Test
    @DisplayName("Test with teigraphy.xml with empty namespace")
    void testRegistryEmptyNamespace()
	throws FileNotFoundException, IOException, DocumentReaderException {
	FileInputStream input = new FileInputStream(teigraphy);
	System.out.println(input);
	PrefixDef prefix = new PrefixDef("([a-zA-Z0-9_-]+)", "teigraphy.xml#$1", "psn");
	assertThrows(StringIndexOutOfBoundsException.class,
		     () -> new SelectionItemsXMLReader(prefix, input, "//person", "@xml:id", "*", ""));
	input.close();
    }

    @Test
    @Disabled("This test is pending")
    @DisplayName("Test with teigraphy.xml with default namespace")
    void testRegistryDefaultNamespace()
	throws FileNotFoundException, IOException, DocumentReaderException {
	FileInputStream input = new FileInputStream(teigraphy);
	System.out.println(input);
	PrefixDef prefix = new PrefixDef("([a-zA-Z0-9_-]+)", "teigraphy.xml#$1", "psn");
	reader = new SelectionItemsXMLReader(prefix, input,
					     "//person", "@xml:id", "*", "http://www.tei-c.org/ns/1.0");
	assertEquals(3, reader.nodesCount());
	input.close();
    }

}

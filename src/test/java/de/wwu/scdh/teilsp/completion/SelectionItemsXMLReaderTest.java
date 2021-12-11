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
    void testRegistry() {
	try {
	    FileInputStream input = new FileInputStream(teigraphy);
	    System.out.println(input);
	    // add tests here
	    input.close();
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}

    }

}

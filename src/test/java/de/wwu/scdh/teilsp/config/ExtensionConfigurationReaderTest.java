package de.wwu.scdh.teilsp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;


public class ExtensionConfigurationReaderTest {

    InputStream input;
    List<ExtensionConfiguration> configs;
    ExtensionConfiguration config;
    EditorVariablesExpander expander;
    
    @BeforeEach
    void setup() throws FileNotFoundException, IOException {
	String configFile = Paths.get("src", "test", "resources", "config.xml").toFile().getAbsolutePath();
	input = new FileInputStream(configFile);
	expander = new NoExpander();
    }

    @AfterEach
    void tearDown() throws IOException {
	input.close();
    }
    
    @Test
    void testConfigReader() throws ConfigurationException {
	configs = ExtensionConfigurationReader.getExtensionsConfiguration(input, expander);
	assertEquals(1, configs.size());
	config = configs.get(0);
	assertEquals("de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXMLByPrefixDef", config.getClassName());
	assertEquals("de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider", config.getType());
	assertEquals(2, config.getSpecification().size());
	
	assertEquals(6, config.getSpecification().get(0).getArguments().size());
	assertEquals("//t:text/descendant::t:person[@xml:id]", config.getSpecification().get(0).getArguments().get("selection"));
	assertEquals("@xml:id", config.getSpecification().get(0).getArguments().get("key"));
	assertEquals(2, config.getSpecification().size());
	assertEquals("10", config.getSpecification().get(0).getConditions().get("priority"));

	config = configs.get(0);
	assertEquals(4, config.getSpecification().get(1).getConditions().size());
    }

    @Test
    void testPropertyReader() throws ConfigurationException {
	String property = ExtensionConfigurationReader.getProperty("test.property", input);
	assertEquals("TestPropertyValue", property);
    }

}

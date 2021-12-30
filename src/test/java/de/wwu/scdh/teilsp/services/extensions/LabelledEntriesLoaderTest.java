package de.wwu.scdh.teilsp.services.extensions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.exceptions.ProviderNotFoundException;
import de.wwu.scdh.teilsp.testutils.SimpleURIResolver;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;


public class LabelledEntriesLoaderTest {

    String configFile, currentFile;
    Document document;

    @BeforeEach
    void setup() throws ParserConfigurationException, SAXException, IOException {
	configFile = "file:" + Paths.get("src", "test", "resources", "config.xml").toFile().getAbsolutePath();
	currentFile = "file:" + Paths.get("src", "test", "resources", "fatherandson.xml").toFile().getAbsolutePath();

	// prepare dom builder
	DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	domFactory.setNamespaceAware(true);
	DocumentBuilder builder = domFactory.newDocumentBuilder();
	// parse the input document
	InputSource inputSource = new InputSource(currentFile);
	document = builder.parse(inputSource);
    }

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

    @Test
    public void testProvidersForContextPersNameRef() throws ExtensionException, ConfigurationException {
	List<ILabelledEntriesProvider> providers =
	    LabelledEntriesLoader.providersForContext(document,
						      currentFile,
						      "/*:TEI[1]/*:text[1]/*:body[1]/*:p[1]/*:persName[1]",
						      ExtensionConfiguration.ATTRIBUTE_VALUE,
						      "ref",
						      new SimpleURIResolver(), null, null,
						      configFile);
	// one provider for this context
	assertEquals(1, providers.size());
	// there are 3 persons suggested
	assertEquals(3, providers.get(0).getLabelledEntries("").size());
    }

    @Test
    public void testProvidersForContextPersNameType() throws ExtensionException, ConfigurationException {
	List<ILabelledEntriesProvider> providers =
	    LabelledEntriesLoader.providersForContext(document,
						      currentFile,
						      "/*:TEI[1]/*:text[1]/*:body[1]/*:p[1]/*:persName[1]",
						      ExtensionConfiguration.ATTRIBUTE_VALUE,
						      "type",
						      new SimpleURIResolver(), null, null,
						      configFile);
	// one provider for this context
	assertEquals(0, providers.size());
    }

    @Test
    public void testProvidersForContextPersonRef() throws ExtensionException, ConfigurationException {
	List<ILabelledEntriesProvider> providers =
	    LabelledEntriesLoader.providersForContext(document,
						      currentFile,
						      "/*:TEI[1]/*:text[1]/*:body[1]/*:p[1]/*:person[1]",
						      ExtensionConfiguration.ATTRIBUTE_VALUE,
						      "ref",
						      new SimpleURIResolver(), null, null,
						      configFile);
	// one provider for this context
	assertEquals(0, providers.size());
    }

}

package de.wwu.scdh.teilsp.services.extensions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.nio.file.Path;
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
import de.wwu.scdh.teilsp.config.EditorVariablesExpander;
import de.wwu.scdh.teilsp.config.NoExpander;


public class LabelledEntriesLoaderTest {

    private static final String DEFAULT_PLUGIN = "de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXML";

    private static final ConfiguredPluginLoader<ILabelledEntriesProvider> loader =
	new ConfiguredPluginLoader<>(ILabelledEntriesProvider.class, DEFAULT_PLUGIN);

    String configFile, currentFile;
    Document document;
    EditorVariablesExpander expander;

    @BeforeEach
    void setup() throws ParserConfigurationException, SAXException, IOException {
	configFile = Paths.get("src", "test", "resources", "config.xml").toUri().toURL().toString();
	Path currentPath = Paths.get("src", "test", "resources", "fatherandson.xml");
	currentFile = currentPath.toUri().toString();

	// prepare dom builder
	DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	domFactory.setNamespaceAware(true);
	DocumentBuilder builder = domFactory.newDocumentBuilder();
	// parse the input document
	InputSource inputSource = new InputSource(currentPath.toFile().getAbsolutePath());
	document = builder.parse(inputSource);
 	expander = new NoExpander();
    }

    @Test
    public void testAtLeastDefaultProvider() {
	List<ILabelledEntriesProvider> providers = loader.providers();
	assertTrue(providers.size() > 0);
    }

    @Test
    public void testDefaultProvider() throws ExtensionException, ProviderNotFoundException {
	assertEquals("de.wwu.scdh.teilsp.extensions.LabelledEntriesFromXML",
		     loader.provider().getClass().getName());
    }

    @Test
    public void testProviderNotFound() throws ExtensionException {
	assertThrows(ProviderNotFoundException.class,
		     () -> { loader.provider("unknown");
		     });
    }

    // FIXME: make runable on windows
    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void testProvidersForContextPersNameRef() throws ExtensionException, ConfigurationException {
	String context = "/*:TEI[1]/*:text[1]/*:body[1]/*:p[1]/*:persName[1]";
	List<ILabelledEntriesProvider> providers =
	    loader.providersForContext
	    (document,
	     context,
	     ExtensionConfiguration.ATTRIBUTE_VALUE,
	     "ref",
	     null,
	     configFile,
	     expander);
	// one provider for this context
	assertEquals(1, providers.size());
	// there are 3 persons suggested
	ILabelledEntriesProvider p0 = providers.get(0);
	p0.setup(new SimpleURIResolver(), null, document, currentFile, context);
	assertEquals(3, providers.get(0).getLabelledEntries("").size());
    }

    // FIXME: make runable on windows
    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void testProvidersForContextPersNameType() throws ExtensionException, ConfigurationException {
	List<ILabelledEntriesProvider> providers =
	    loader.providersForContext
	    (document,
	     "/*:TEI[1]/*:text[1]/*:body[1]/*:p[1]/*:persName[1]",
	     ExtensionConfiguration.ATTRIBUTE_VALUE,
	     "type",
	     null,
	     configFile,
	     expander);
	// one provider for this context
	assertEquals(0, providers.size());
    }

    // FIXME: make runable on windows
    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void testProvidersForContextPersonRef() throws ExtensionException, ConfigurationException {
	List<ILabelledEntriesProvider> providers =
	    loader.providersForContext
	    (document,
	     "/*:TEI[1]/*:text[1]/*:body[1]/*:p[1]/*:person[1]",
	     ExtensionConfiguration.ATTRIBUTE_VALUE,
	     "ref",
	     null,
	     configFile,
	     expander);
	// one provider for this context
	assertEquals(0, providers.size());
    }

}

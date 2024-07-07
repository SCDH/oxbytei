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


public class LabelledEntriesSparqlTest {

    LabelledEntriesSparql provider;
    Map<String, String> args;

    String resources;
    String sparqlUrl, datasetlUrl0, missing;

    List<LabelledEntry> result;

    @BeforeEach
    void setup()
	throws MalformedURLException, IOException {
	provider = new LabelledEntriesSparql();
	args = new HashMap<String, String>();

	resources = Paths.get("src", "test", "resources").toFile().getAbsolutePath();
	sparqlUrl = "file:" + Paths.get("src", "test", "resources", "entries-specifiedBy.sparql").toFile().getAbsolutePath();
	datasetlUrl0 = "file:" + Paths.get("src", "test", "resources", "ontology.ttl").toFile().getAbsolutePath();
	missing = "file:" + Paths.get("src", "test", "resources", "missing").toFile().getAbsolutePath();
    }

    @AfterEach
    void tearDown()
	throws IOException {
    }

    @Test
    void testNoArgs()
	throws ConfigurationException, ExtensionException {
	Exception exception = assertThrows(ConfigurationException.class, () -> provider.init(args));
	assertEquals(String.format("Required argument '%s' is missing", LabelledEntriesSparql.ARGUMENT_SPARQL.getName()),
		     exception.getMessage());
    }

    @Test
    void testSparqlUrlRequired()
	throws ConfigurationException, ExtensionException {
	args.put(LabelledEntriesSparql.ARGUMENT_DATASETS.getName(), datasetlUrl0);
	Exception exception = assertThrows(ConfigurationException.class, () -> provider.init(args));
	assertEquals(String.format("Required argument '%s' is missing", LabelledEntriesSparql.ARGUMENT_SPARQL.getName()),
		     exception.getMessage());
    }

    @Test
    void testDatasetsUrlRequired()
	throws ConfigurationException, ExtensionException {
	args.put(LabelledEntriesSparql.ARGUMENT_SPARQL.getName(), sparqlUrl);
	Exception exception = assertThrows(ConfigurationException.class, () -> provider.init(args));
	assertEquals(String.format("Required argument '%s' is missing", LabelledEntriesSparql.ARGUMENT_DATASETS.getName()),
		     exception.getMessage());
    }

    @Test
    void testSparqlMissing()
	throws ConfigurationException, ExtensionException {
	args.put(LabelledEntriesSparql.ARGUMENT_SPARQL.getName(), missing);
	args.put(LabelledEntriesSparql.ARGUMENT_DATASETS.getName(), datasetlUrl0);
	Exception exception = assertThrows(ConfigurationException.class, () -> provider.init(args));
	assertTrue(exception.getMessage().startsWith("Not found"));
	assertTrue(exception.getMessage().endsWith("teilsp/src/test/resources/missing"));
    }

    @Test
    void testDatasetsMissing()
	throws ConfigurationException, ExtensionException {
	args.put(LabelledEntriesSparql.ARGUMENT_SPARQL.getName(), sparqlUrl);
	args.put(LabelledEntriesSparql.ARGUMENT_DATASETS.getName(), missing);
	Exception exception = assertThrows(ConfigurationException.class, () -> provider.init(args));
	assertTrue(exception.getMessage().startsWith("Not found"));
	assertTrue(exception.getMessage().endsWith("teilsp/src/test/resources/missing"));
    }

    @Test
    void testSparql()
	throws ConfigurationException, ExtensionException {
	args.put(LabelledEntriesSparql.ARGUMENT_SPARQL.getName(), sparqlUrl);
	args.put(LabelledEntriesSparql.ARGUMENT_DATASETS.getName(), datasetlUrl0);
	provider.init(args);
	result = provider.getLabelledEntries("");
	assertEquals(result.size(), 2);
	assertEquals("https://scdh.zivgitlabpages.uni-muenster.de/schnocks-ijob/edition/ontology#Uebersetzung",
		     result.get(0).getKey());
	assertEquals("Übersetzungen: Eine Übersetzung ist eine Art intertextueller Relation.",
		     result.get(0).getLabel());
    }

}

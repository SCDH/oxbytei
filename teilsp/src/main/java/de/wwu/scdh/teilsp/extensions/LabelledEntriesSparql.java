package de.wwu.scdh.teilsp.extensions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;

import org.w3c.dom.Document;
import javax.xml.transform.URIResolver;
import org.xml.sax.EntityResolver;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.QuerySolution;

import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.extensions.LabelledEntryImpl;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;
import de.wwu.scdh.teilsp.services.extensions.ListArgumentDescriptor;

/**
 * Make a list of {@link LabelledEntry}s from SPARQL query on dataset
 * given by URLs of RDF files to be parsed.<P>
 *
 * Note, that this does not query an remote SPARQL endpoint.<P>
 *
 * This plugin expects two variables to be returned from a SPARQL
 * SELECT query: <code>entry</code> and <code>label</code>. So the
 * query should look like this:<P>
 *
 * <code>SELECT DISTINCT ?entry ?label WHERE { ... }</code>.
 */
public class LabelledEntriesSparql implements ILabelledEntriesProvider {

    public static String[] RDF_TYPES = {"node", "resource", "literal"};

    protected Map<String, String> arguments = new HashMap<String, String>();
    protected Dataset dataset;
    protected Query query;

    private URL sparql;

    public static final ArgumentDescriptor<URL> ARGUMENT_SPARQL =
	new ArgumentDescriptorImpl<URL>(URL.class, "sparql", "The SPARQL query file URL.");


    private List<URL> datasets;

    public static final ArgumentDescriptor<List<URL>> ARGUMENT_DATASETS =
	new ListArgumentDescriptor<URL>
	(URL.class,
	 "datasets",
	 "URLs of RDF files to make the graph from. The first serves as the default graph, subsequent as named graphs. The syntax of each file is guessed.",
	 "\\s+",
	 1);

    String entryType;

    private static final ArgumentDescriptor<String> ARGUMENT_ENTRY_TYPE =
	new ArgumentDescriptorImpl<String>(String.class, "entryType", "The RDF type of the entry variable", RDF_TYPES, "resource");

    String labelType;

    public static final ArgumentDescriptor<String> ARGUMENT_LABEL_TYPE =
	new ArgumentDescriptorImpl<String>(String.class, "entryType", "The RDF type of the entry variable", RDF_TYPES, "literal");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
	ARGUMENT_SPARQL,
	ARGUMENT_DATASETS,
	ARGUMENT_ENTRY_TYPE,
	ARGUMENT_LABEL_TYPE};

    /**
     * {@link getArguments} returns configuration arguments. This
     * might be informative for debugging a error messages.
     */
    public Map<String, String> getArguments() {
	return this.arguments;
    }

    /**
     * {@link init} initializes the provider with a arguments from
     * configuration. It is once called during the initialization
     * phase.
     *
     * @param  kwargs      key value pairs with configuration parameters
     */
    public void init(Map<String, String> arguments) throws ConfigurationException {
	this.arguments = arguments;
	this.sparql = ARGUMENT_SPARQL.getValue(arguments);
	this.datasets = ARGUMENT_DATASETS.getValue(arguments);
	this.entryType = ARGUMENT_ENTRY_TYPE.getValue(arguments);
	this.labelType = ARGUMENT_LABEL_TYPE.getValue(arguments);

	try {
	    // make a RDF dataset
	    List<String> namedGraphs = new ArrayList<String>();
	    for (int i = 1; i < datasets.size(); i++) {
		namedGraphs.add(datasets.get(i).toString());
	    }
	    dataset = DatasetFactory.create(datasets.get(0).toString(), namedGraphs);

	    // parse the SPARQL query from the file
	    query = QueryFactory.read(sparql.toString());
	} catch (Exception e) {
	    throw new ConfigurationException(e.getMessage());
	}
    }

    /**
     * This returns an array of argument descriptors implementing
     * {@link ArgumentDescriptor}s. These can be used to validate the
     * configuration, get the values or get a description of the
     * arguments.
     *
     * @return an array of argument descriptors
     */
    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }


    /**
     * {@link setup} passes a setup of document and resolvers to the
     * plugin. This method should be called once called during the
     * initialization phase,
     * after {@link ConfigurablePlugin#init(java.util.Map)}.
     *
     * @param  uriResolver URI resolver used in the editor
     * @param  entityResolver entity resolver used in the editor
     * @param  document    the currently edited file as DOM object
     * @param  systemId    URL of the current document
     * @param  context     XPath to the current editing position
     */
    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document document,
	 String systemId,
	 String context)
	throws ExtensionException {
    }

    /**
     * {@link getLabelledEntries} returns an ordered collection of labelled
     * entries. It takes a map of key value pairs as arguments.
     *
     * @param  userInput   what the user has typed so far
     * @return             an ordered sequence of labelled entries
     */
    public List<LabelledEntry> getLabelledEntries(String userInput) throws ExtensionException {
	try {
	    QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
	    ResultSet results = qexec.execSelect();

	    List<LabelledEntry> entries = new ArrayList<LabelledEntry>();
	    while (results.hasNext()) {
		QuerySolution solution = results.nextSolution();
		String entry = getSparqlVariable("entry", entryType, solution);
		String label = getSparqlVariable("label", labelType, solution);
		entries.add(new LabelledEntryImpl(entry, label)) ;
	    }
	    return entries;
	} catch(Exception e) {
	    throw new ExtensionException(e.getMessage());
	}
    }

    protected String getSparqlVariable(String name, String rdfType, QuerySolution solution) throws Exception {
	if (rdfType.equals("resource")) return solution.getResource(name).toString();
	else if (rdfType.equals("literal")) return solution.getLiteral(name).toString();
	else return solution.get(name).toString();
    }


}

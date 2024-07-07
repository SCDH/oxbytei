package de.wwu.scdh.teilsp.extensions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URL;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.QuerySolution;

import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.extensions.LabelledEntryImpl;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;

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
public class LabelledEntriesSparqlEndpoint extends LabelledEntriesSparql {


    private URL sparql;


    public static final ArgumentDescriptor<URL> ARGUMENT_ENDPOINT =
	new ArgumentDescriptorImpl<URL>
	(URL.class,
	 "endpoint",
	 "URL of SPARQL endpoint.");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
	ARGUMENT_SPARQL,
	ARGUMENT_ENDPOINT,
	ARGUMENT_ENTRY_TYPE,
	ARGUMENT_LABEL_TYPE};

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
	this.entryType = ARGUMENT_ENTRY_TYPE.getValue(arguments);
	this.labelType = ARGUMENT_LABEL_TYPE.getValue(arguments);

	try {
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
     * {@link getLabelledEntries} returns an ordered collection of labelled
     * entries. It takes a map of key value pairs as arguments.
     *
     * @param  userInput   what the user has typed so far
     * @return             an ordered sequence of labelled entries
     */
    public List<LabelledEntry> getLabelledEntries(String userInput) throws ExtensionException {
	try {
	    String endpt = ARGUMENT_ENDPOINT.getValue(this.arguments).toString();
	    QueryExecution qexec = QueryExecution.service(endpt, query);
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

}

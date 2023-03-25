package de.wwu.scdh.teilsp.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.transform.URIResolver;


import org.xml.sax.EntityResolver;
import org.w3c.dom.Document;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptorImpl;
import de.wwu.scdh.teilsp.services.extensions.URLArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;


/**
 * A {@link ILabelledEntriesProvider} provider that reads CSV data.
 *
 * Multiple CSV formats are supported. See {@link CSVFormat.Predefined}
 */
public class LabelledEntriesCSV implements ILabelledEntriesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelledEntriesCSV.class);

    /**
     * The CSV format.
     */
    protected static final ArgumentDescriptor<CSVFormat> ARGUMENT_FORMAT =
	new ArgumentDescriptorImpl<CSVFormat>
	(CSVFormat.class,
	 "format",
	 "The format of the CSV file. This defaults to the Default format of org.apache.commons.csv",
	 CSVFormat.DEFAULT) {
	    @Override
	    public CSVFormat fromString(String name)
		throws ConfigurationException {
		try {
		    return CSVFormat.Predefined.valueOf(name).getFormat();
		} catch (Exception e) {
		    throw new ConfigurationException("Unknown CSV format " + name);
		}
	    }
	};

    protected static final ArgumentDescriptor<URL> ARGUMENT_URL =
	new URLArgumentDescriptor("url", "The URL of the CSV file.");

    protected static final ArgumentDescriptor<String> ARGUMENT_DELIMITER =
	new ArgumentDescriptorImpl<String>
	(String.class,
	 "delimiter",
	 "The delimiter used in the CSV file.",
	 ARGUMENT_FORMAT.getDefaultValue().getDelimiterString());

    protected static final ArgumentDescriptor<String> ARGUMENT_HEADER =
	new ArgumentDescriptorImpl<String>
	(String.class, "header",
	 "A list of column names. If this is given, the header row of the file is skipped. The column names must be separated by the same delimiter as in the CSV file.",
	 "");

    protected static final ArgumentDescriptor<String> ARGUMENT_KEY_COLUMNS =
	new ArgumentDescriptorImpl<String>
	(String.class, "keyColumns",
	 "The columns that go into the key. Multiple columns are possible. They must be delimited with the same delimiter as the columns in the file.",
	 "key");

    protected static final ArgumentDescriptor<String> ARGUMENT_KEY_INTERS =
	new ArgumentDescriptorImpl<String>
	(String.class, "keySeparators",
	 "The interconnectors that go between the key's columns. They must be delimited with comma. If there are less separators that columns minus 1, then the empty string is used at the according position",
	 "");

    protected static final ArgumentDescriptor<String> ARGUMENT_LABEL_COLUMNS =
	new ArgumentDescriptorImpl<String>
	(String.class, "labelColumns",
	 "The columns that go into the label. Multiple columns are possible. They must be delimited with the same delimiter as the columns in the file.",
	 "label");

    protected static final ArgumentDescriptor<String> ARGUMENT_LABEL_INTERS =
	new ArgumentDescriptorImpl<String>
	(String.class, "labelSeparators",
	 "The interconnectors that go between the label's columns. They must be delimited with comma. If there are less separators that columns minus 1, then the empty string is used at the according position",
	 "");

    protected static final ArgumentDescriptor<String> ARGUMENT_PREFIX =
	new ArgumentDescriptorImpl<String>
	(String.class, "prefix",
	 "A prefix added to each key. E.g. '#' or 'persons:'.",
	 "");

    protected static final ArgumentDescriptor<String> ARGUMENT_LABEL_PREFIX =
	new ArgumentDescriptorImpl<String>
	(String.class, "labelPrefix",
	 "A prefix added to each label. This may be useful for a provenience hint if more than one plugin provides labelled entries.",
	 "");

    /**
     * The array of arguments, this author operation takes.
     */
    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
	ARGUMENT_URL,
	ARGUMENT_DELIMITER,
	ARGUMENT_HEADER,
	ARGUMENT_KEY_COLUMNS,
	ARGUMENT_KEY_INTERS,
	ARGUMENT_LABEL_COLUMNS,
	ARGUMENT_LABEL_INTERS,
	ARGUMENT_PREFIX,
	ARGUMENT_LABEL_PREFIX};

    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    protected Map<String, String> arguments;
    protected URL csvUrl;
    protected String prefix;
    protected String labelPrefix;
    protected String[] keyColumns;
    protected String[] keyInters;
    protected int keyCount;
    protected String[] labelColumns;
    protected String[] labelInters;
    protected int labelCount;

    protected CSVFormat csvFormat;
    protected CSVFormat.Builder builder;

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	this.arguments = arguments;
	csvUrl = ARGUMENT_URL.getValue(arguments);
	prefix = ARGUMENT_PREFIX.getValue(arguments);
	labelPrefix = ARGUMENT_LABEL_PREFIX.getValue(arguments);

	// create a builder to collect arguments that further specify the CSV format
	builder = CSVFormat.Builder.create(ARGUMENT_FORMAT.getValue(arguments));

	String delimiter = ARGUMENT_DELIMITER.getValue(arguments);
	builder.setDelimiter(delimiter);

	String header = ARGUMENT_HEADER.getValue(arguments);
	if (!"".equals(header) && header != null) {
	    builder.setHeader(header.split(delimiter));
	    builder.setSkipHeaderRecord(true);
	} else {
	    builder.setHeader().setSkipHeaderRecord(true);
	}

	// generate the csv format from the builder
	csvFormat = builder.build();

	setKeyColumns();
	setLabelColumns();
    }

    protected void setKeyColumns()
	throws ConfigurationException {
	// for faster genration of the key in getLabelledEntries we
	// set the first interconnector to "" and shift them by 1.
	// So we can then simply do
	//
	// key = key + keyInters[i] + values.get(keyColumns[i]);
	keyColumns = ARGUMENT_KEY_COLUMNS.getValue(arguments).split(",");
	keyCount = keyColumns.length;
	keyInters = new String[keyCount];
	String[] inters = ARGUMENT_KEY_INTERS.getValue(arguments).split(",");
	int intersCount = inters.length;
	// set the first interconnector to the empty string.
	if (keyCount > 0) {
	    keyInters[0] = "";
	}
	// We start with i=1 for shifting the position. We also assert
	// that we have an interconnector.
	for (int i = 1; i < keyCount; i++) {
	    if (i-1 < intersCount) {
		keyInters[i] = inters[i-1];
	    } else {
		keyInters[i] = "";
	    }
	}
    }

    protected void setLabelColumns()
	throws ConfigurationException {
	labelColumns = ARGUMENT_LABEL_COLUMNS.getValue(arguments).split(",");
	labelCount = labelColumns.length;
	labelInters = new String[labelCount];
	String[] inters = ARGUMENT_LABEL_INTERS.getValue(arguments).split(",");
	int intersCount = inters.length;
	// for faster genration of the label in getLabelledEntries we
	// set the first interconnector to "" and shift them by 1. So
	// we start with i=1. We also assert that we have an
	// interconnector.
	if (labelCount > 0) {
	    labelInters[0] = "";
	}
	for (int i = 1; i < labelCount; i++) {
	    if (i-1 < intersCount) {
		labelInters[i] = inters[i-1];
	    } else {
		labelInters[i] = "";
	    }
	}
    }


    public Map<String, String> getArguments() {
	return arguments;
    }

    public void setup
	(URIResolver uriResolver,
	 EntityResolver entityResolver,
	 Document document,
	 String systemId,
	 String context) {
	// nothing to do here
    }

    public List<LabelledEntry> getLabelledEntries(String userInput)
	throws ExtensionException {

	List<LabelledEntry> entries = new ArrayList<LabelledEntry>();

	LOGGER.info("Reading CSV of format {} data from {}", csvFormat.toString(), csvUrl.toString());

	try {
	    InputStream inputStream = csvUrl.openStream();
	    InputStreamReader reader = new InputStreamReader(inputStream);

	    Iterable<CSVRecord> records = csvFormat.parse(reader);
	    for (CSVRecord record : records) {
		// a map where the key is the column name and the
		// value is the column value
		Map<String, String> values = record.toMap();

		// generate the key from arbitrary columns
		String key = prefix; // start with prefix
		for (int i = 0; i < keyCount; i++) {
		    key = key + keyInters[i] + values.get(keyColumns[i]);
		}

		// generate the label from arbitrary columns
		String label = labelPrefix;
		for (int j = 0; j < labelCount; j++) {
		    label = label + labelInters[j] + values.get(labelColumns[j]);
		}

		LabelledEntry entry = new LabelledEntryImpl(key, label);
		entries.add(entry);
	    }

	} catch (IOException e) {
	    throw new ExtensionException("Error reading from " + csvUrl.toString());
	}

	return entries;
    }

}

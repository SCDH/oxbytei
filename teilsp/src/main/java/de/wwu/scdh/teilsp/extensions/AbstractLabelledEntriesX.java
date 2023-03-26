package de.wwu.scdh.teilsp.extensions;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import net.sf.saxon.s9api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.BooleanArgumentDescriptor;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;


/**
 * An abstract base class for providers for labelled entries that
 * produces their collection using the s9api.
 *
 * It implements binding XQuery/XSLT types to Java types.
 */
public abstract class AbstractLabelledEntriesX {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLabelledEntriesX.class);

    protected static final ArgumentDescriptor<Boolean> ARGUMENT_DROP_EMPTY_KEYS =
	new BooleanArgumentDescriptor
	("dropEmptyKeys",
	 "Whether or not to drop entries with empty keys from the transformation result."
	 + " Defaults to true",
	 true);

    protected static final ArgumentDescriptor<Boolean> ARGUMENT_FAIL_EMPTY_KEY =
	new BooleanArgumentDescriptor
	("failOnEmptyKey",
	 "Whether or not to fail when there are entries with empty keys in the XQuery result."
	 + " Defaults to true",
	 true);


    protected boolean failOnEmptyKey;
    protected boolean dropEmptyKeys;

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	dropEmptyKeys = ARGUMENT_DROP_EMPTY_KEYS.getValue(arguments);
	failOnEmptyKey = ARGUMENT_FAIL_EMPTY_KEY.getValue(arguments);
    }

    /**
     * get a list of simple {@link LabelledEntryImpl} objects from
     * the XdmValue returned by XSLT or XQuery.
     *
     */
    protected List<LabelledEntry> getEntries(XdmValue result) {
	List<LabelledEntry> entries = new ArrayList<LabelledEntry>();
	XdmAtomicValue key = new XdmAtomicValue("key");
	XdmAtomicValue label = new XdmAtomicValue("label");
	// read the result
	for (XdmItem item : result) {
	    Map<XdmAtomicValue, XdmValue> keyValue = item.asMap();
	    LabelledEntry entry =
		new LabelledEntryImpl(keyValue.get(key).toString(),
				      keyValue.get(label).toString());
	    if (!failOnEmptyKey) {
		entries.add(entry);
	    } else {
		if (null == entry.getKey() || "".equals(entry.getKey())) {
		    if (dropEmptyKeys) {
			LOGGER.info("Dropping entry with empty key. Label is: %s", entry.getLabel());
		    } else {
			entries.add(entry);
		    }
		} else {
		    entries.add(entry);
		}
	    }
	}
	return entries;
    }

}

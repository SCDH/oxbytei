package de.wwu.scdh.oxbytei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.sync.ecss.extensions.commons.operations.MoveCaretOperation;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;


import de.wwu.scdh.oxbytei.commons.UpdatableArgumentsMap;
import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;

/**
 * Find markup that referes the the text at the current caret position
 * and goto it. A selection dialogue is presented.
 */
public class FindReferersOperation extends MoveCaretOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(FindReferersOperation.class);

    @Override
    public String getDescription() {
	return "Find referers to the current caret offset and let the user select one.";
    }

    public static final Pattern XPATH_PATTERN = Pattern.compile(OxbyteiConstants.XPATH_RE);

    public static final Pattern REFERING_ENTRY_PATTERN =
	Pattern.compile("(?<name>"
			+ OxbyteiConstants.XPATH_QUOTED
			+ ")\\s*,\\s*(?<from>"
			+ OxbyteiConstants.XPATH_QUOTED
			+ ")\\s*,\\s*(?<to>"
			+ OxbyteiConstants.XPATH_QUOTED
			+ ")\\s*,\\s*(?<label>"
			+ OxbyteiConstants.XPATH_QUOTED
			+ ")\\s*,\\s*(?<namespace>"
			+ OxbyteiConstants.NAMESPACE_RE
			+")\\s*,?");

    private static final ArgumentDescriptor ARGUMENT_REFERERS =
	new ArgumentDescriptor("referers",
			      ArgumentDescriptor.TYPE_STRING,
			      "The form of this is:\n\n"
			       + "SELECTION, FROM, TO, LABEL, NAMESPACE(, SELECTION, FROM, TO, LABEL, NAMESPACE)*\n\n"
			       + "All of them but NAMESPACE are XPath expressions."
			       + "If there's a comma in an XPath expression, the whole expression must be quoted with double quotes.");

    public static final ArgumentDescriptor ARGUMENT_MESSAGE =
	new ArgumentDescriptor("message",
			       ArgumentDescriptor.TYPE_STRING,
			       "The message in the user dialog.");

    private class ReferingEntry {
	public String name;
	public String namespace;
	public String fromXPath;
	public String toXPath;
	public String labelXPath;
    }

    private class Span {
	String entryXPath;
	int from;
	int to;
	String label;
    }

    @Override
    public final ArgumentDescriptor[] getArguments() {
	ArgumentDescriptor[] local = new ArgumentDescriptor[] {
	    ARGUMENT_MESSAGE,
	    ARGUMENT_REFERERS
	};
	ArgumentDescriptor[] move = super.getArguments();
	// TODO: Is there a guarantee, that a missing 'value' argument
	// does not cause an exception? I.e. is it guaranteed, thtere
	// is no validation, before the arugments are passed to the
	// operation?
	ArgumentDescriptor[] all = Arrays.copyOf(move, move.length + local.length);
	System.arraycopy(local, 0, all, move.length, local.length);
	return all;
    }

    @Override
    public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
	throws AuthorOperationException {

	String message =
	    OperationArgumentValidator.validateStringArgument(ARGUMENT_MESSAGE.getName(), args);

	// get referers argument and parse it to a list of referers
	String referersString = OperationArgumentValidator.validateStringArgument(ARGUMENT_REFERERS.getName(), args);
	List<ReferingEntry> referingEntries = new ArrayList<ReferingEntry>();
	Matcher matcher = REFERING_ENTRY_PATTERN.matcher(referersString);
	Matcher xpathMatcher;
	while (matcher.find()) {
	    ReferingEntry entry = new ReferingEntry();
	    xpathMatcher = XPATH_PATTERN.matcher(matcher.group("name"));
	    xpathMatcher.find();
	    entry.name = xpathMatcher.group();
	    entry.namespace = matcher.group("namespace");
	    xpathMatcher = XPATH_PATTERN.matcher(matcher.group("from"));
	    xpathMatcher.find();
	    entry.fromXPath = xpathMatcher.group();
	    xpathMatcher = XPATH_PATTERN.matcher(matcher.group("to"));
	    xpathMatcher.find();
	    entry.toXPath = xpathMatcher.group();
	    xpathMatcher = XPATH_PATTERN.matcher(matcher.group("label"));
	    xpathMatcher.find();
	    entry.labelXPath = xpathMatcher.group();
	    referingEntries.add(entry);
	    LOGGER.debug("Parsed entry node name from the arguments: {}, {}, {}", entry.name, entry.fromXPath, entry.toXPath);
	}
	LOGGER.debug("Parsed {} entry setups from the arguments.", referingEntries.size());

	// get the position of the caret
	int currentOffset = authorAccess.getEditorAccess().getCaretOffset();

	// get the list of referers
	List<Span> referers = new ArrayList<Span>();
	// get all nodes known as possible referers
	// by first iterating over the XPaths for referers
	for (ReferingEntry referingEntry : referingEntries) {
	    AuthorNode[] authorNodes =
		authorAccess.getDocumentController().findNodesByXPath(referingEntry.name, true, false, false, true);
	    LOGGER.debug("Found {} potential referers from {}", authorNodes.length, referingEntry.name);
	    // then iterate over found entry nodes and get their anchors in order make Span objects
	    for (AuthorNode authorNode : authorNodes) {
		AuthorElement context = (AuthorElement) authorNode;
		Span span = new Span();
		// get the current editing context as an XPath expression
		Object[] contextPath =
		    authorAccess.getDocumentController().evaluateXPath(OxbyteiConstants.CONTEXT_XPATH, context, false, false, false, true);
		span.entryXPath = "/" + (String) contextPath[0];
		LOGGER.debug("Found potential referer at {}", span.entryXPath);

		// make xpaths to anchors
		String fromAnchorXPath =
		    "let $id := " + span.entryXPath + "/" + referingEntry.fromXPath + " return //*[@xml:id eq substring($id, 2)]";
		String toAnchorXPath =
		    "let $id := " + span.entryXPath + "/" + referingEntry.toXPath + " return //*[@xml:id eq substring($id, 2)]";

		// get the offsets of the anchors
		span.from =
		    authorAccess.getDocumentController().getXPathLocationOffset(fromAnchorXPath, AuthorConstants.POSITION_BEFORE, true);
		span.to =
		    authorAccess.getDocumentController().getXPathLocationOffset(toAnchorXPath, AuthorConstants.POSITION_AFTER, true);
		// get the label
		String labelXPath = referingEntry.labelXPath;
		Object[] lables =
		    authorAccess.getDocumentController().evaluateXPath(labelXPath, context, true, true, true, true);
		span.label = (String) lables[0];

		// add it to the list of referers, if the caret offset is within the span
		if (span.from <= currentOffset && span.to >= currentOffset) {
		    referers.add(span);
		    LOGGER.debug("Found real referer at {}", span.entryXPath);
		}
	    }
	}
	LOGGER.debug("Found {} referers", referers.size());

	if (referers.size() == 0) {
	    // inform the author, that there's nothing present
	    throw new AuthorOperationException("Nothing found");
	}

	// prepare the alternatives for the ask editor variable
	String pairs = "";
	for (Span referer : referers) {
	    if (! pairs.isEmpty()) {
		pairs += "; ";
	    }
	    pairs += "'" + referer.entryXPath + "':'" + referer.label + "'";
	}

	//AskDescriptor("combobox", title, keys, labels, currentValue);
	String ask = "${ask('" + message + "', combobox, (" + pairs + "), '" + referers.get(0).entryXPath + "')}";
	String selectedLocation = authorAccess.getUtilAccess().expandEditorVariables(ask, null, true);
	// When "Cancel" is pressed in the dialog, the unexpanded
	// string is returned. In this case we set the selection to
	// the empty string.
	if (! selectedLocation.startsWith("${ask(")) {

	    // expand editor variables
	    UpdatableArgumentsMap newArgs = new UpdatableArgumentsMap(args, getArguments());
	    newArgs.update("xpathLocation", selectedLocation);
	    newArgs.expand("position", authorAccess, true);
	    newArgs.expand("selection", authorAccess, true);

	    // do the move!
	    super.doOperation(authorAccess, newArgs);
	}

    }

}

package de.wwu.scdh.oxbytei;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.commons.operations.ChangeAttributeOperation;

import de.wwu.scdh.oxbytei.commons.OperationArgumentValidator;
import de.wwu.scdh.oxbytei.commons.UpdatableArgumentsMap;
import de.wwu.scdh.teilsp.config.ExtensionConfiguration;


/**
 * {@link SelectAttributeValueOperation} is an oXygen author operation
 * for selecting an attribute value of a list of suggestions. To make
 * these suggestions, the plugins registered for the current editing
 * context are called. The generation of the suggestions is managed by
 * the plugins and the configuration alone. This class only calls the
 * plugin loader.
 *
 * Which selection dialogue is presented to the user can be defined by
 * the operation's arguments.
 *
 * @author Christian Lück
 */
public class SelectAttributeValueOperation
    extends ChangeAttributeOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectAttributeValueOperation.class);

    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#getArguments()
     */
    public ArgumentDescriptor[] getArguments() {
	ArgumentDescriptor[] help = SelectLabelledEntryHelper.getArguments();
	ArgumentDescriptor[] sup = super.getArguments();
	// TODO: Is there a guarantee, that a missing 'value' argument
	// does not cause an exception? I.e. is it guaranteed, thtere
	// is no validation, before the arugments are passed to the
	// operation?
	ArgumentDescriptor[] all = Arrays.copyOf(sup, sup.length + help.length);
	System.arraycopy(help, 0, all, sup.length, help.length);
	return all;
    }

    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#getDescription()
     */
    public String getDescription() {
	return "Set an attribute by presenting the user a selection generated by plugins for the current file context.";
    }
    
    /**
     * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation()
     */
    public void doOperation(final AuthorAccess authorAccess, ArgumentsMap args)
	throws AuthorOperationException, IllegalArgumentException {

	// Validate arguments that are passed to helper class
	String attributeName = OperationArgumentValidator.validateStringArgument("name", args);
	String attributeNamespace = OperationArgumentValidator.validateStringArgument("namespace", args, null);
	String location = OperationArgumentValidator.validateStringArgument("elementLocation", args);

	// use helper class to load the plugins und initialize them
	InteractiveOperation contextInteraction =
	    new SelectLabelledEntryHelper(authorAccess,
					  ExtensionConfiguration.ATTRIBUTE_VALUE,
					  attributeName,
					  attributeNamespace,
					  location,
					  args);

	// do the user interaction
	String selection = contextInteraction.doUserInteraction();
	LOGGER.error("Selected value {}", selection);

	// write to the argument value by passing it to super class
	if (selection != null) {
	    UpdatableArgumentsMap newArgs = new UpdatableArgumentsMap(args, super.getArguments());
	    newArgs.update("value", (Object) selection);
	    super.doOperation(authorAccess, newArgs);
	}
    }

}

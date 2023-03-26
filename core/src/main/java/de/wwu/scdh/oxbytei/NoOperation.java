package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;

/**
 * There are situations, in which it is disered to do nothing at all.
 */
public class NoOperation implements AuthorOperation {

    @Override
    public String getDescription() {
	return "Do nothing at all.";
    }
    
    @Override
    public ArgumentDescriptor[] getArguments() {
	return new ArgumentDescriptor[] {
	    NAMESPACE_ARGUMENT_DESCRIPTOR,
	    SCHEMA_AWARE_ARGUMENT_DESCRIPTOR
	};
    }

    @Override
    public void  doOperation(AuthorAccess authorAccess, ArgumentsMap args) {
	// do nothing
    }
}

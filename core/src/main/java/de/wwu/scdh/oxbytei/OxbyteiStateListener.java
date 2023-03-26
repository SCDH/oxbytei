package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link OxbyteiStateListener} is a {@link AuthorExtensionStateListener}
 * for the oXbytei extension.
 */
public class OxbyteiStateListener
    implements AuthorExtensionStateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OxbyteiStateListener.class);

    @Override
    public String getDescription() {
	return "This activates utilities for the oXbytei framework.";
    }

    private OxbyteiEditorVariables oxbyteiEditorVariables;

    public OxbyteiStateListener() {
	oxbyteiEditorVariables = new OxbyteiEditorVariables();
    }

    @Override
    public void activated(AuthorAccess authorAccess) {
	LOGGER.debug("Activating oXbytei state listener");
	authorAccess.getUtilAccess().addCustomEditorVariablesResolver(oxbyteiEditorVariables);
    }

    @Override
    public void deactivated(AuthorAccess authorAccess) {
	authorAccess.getUtilAccess().removeCustomEditorVariablesResolver(oxbyteiEditorVariables);
    }
}

package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.AuthorOperationException;

/**
 * An interface for an interactive (author mode) operation.
 *
 * @author Christian Lück
 */
public interface InteractiveOperation {

    /**
     * Calling {@code doUserInteraction()} actually does the user
     * interaction.
     */
    public String doUserInteraction() throws AuthorOperationException;

}

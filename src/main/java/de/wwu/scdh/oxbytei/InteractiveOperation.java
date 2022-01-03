package de.wwu.scdh.oxbytei;

import ro.sync.ecss.extensions.api.AuthorOperationException;

public interface InteractiveOperation {

    public String doUserInteraction() throws AuthorOperationException;

}

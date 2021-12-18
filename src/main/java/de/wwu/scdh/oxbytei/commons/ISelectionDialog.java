package de.wwu.scdh.oxbytei.commons;

import java.util.List;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;


public interface ISelectionDialog {

    public void init(AuthorAccess access,
		     String multi,
		     String currentVal,
		     List<ConfiguredEntriesProvider> configured);

    public String doUserInteraction() throws AuthorOperationException;

}

package de.wwu.scdh.oxbytei.commons;

import java.util.List;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;

public interface ISelectionDialog {

    public void init(AuthorAccess access,
		     String title,
		     String multi,
		     String currentVal,
		     List<ILabelledEntriesProvider> configured);

    public String doUserInteraction() throws AuthorOperationException;

}

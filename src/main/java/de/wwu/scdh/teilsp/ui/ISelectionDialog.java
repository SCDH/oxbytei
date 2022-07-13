/**
 * {@link ISelectionDialog} is an interface for user dialogues which
 * offer a selection from a list pairs of key and label generated by
 * an {@link ILabelledEntriesProvider}.
 */
package de.wwu.scdh.teilsp.ui;

import java.util.List;
import java.util.Map;

import de.wwu.scdh.teilsp.exceptions.UIException;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;


public interface ISelectionDialog {

    /**
     * This must be called after constructor in order to pass
     * initialization data.
     *
     * @param title the title of the dialog
     * @param icon an URL to the icon displayed in the dialog
     */
    public void init(Map<String, String> arguments);

    /**
     * This must be called after {@link init} in order to pass
     * in content completion data.
     *
     * @param currentVal current value
     * @param labelledEntriesProviders a list of initialized {@link ILabelledEntriesProvider}s
     */
    public void setup(List<String> currentVal,
		      List<ILabelledEntriesProvider> labelledEntriesProviders)
	throws UIException, ExtensionException;

    /**
     * This actually does the user interaction. To get its result,
     * call {@link getSelection()}.
     */
    public void doUserInteraction() throws UIException, ExtensionException;

    /**
     * This returns the key selected by the user.
     * {@link doUserInteraction} must be called first.
     *
     * @return The keys of the items selected by the user. It must
     * return {@code null} in case of cancellation by the user.
     */
    public List<String> getSelection() throws UIException;

}

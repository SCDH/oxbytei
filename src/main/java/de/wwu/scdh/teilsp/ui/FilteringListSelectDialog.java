/**
 *
 * This class is designed after {@code ListDialog} in the Swing
 * tutorial, see
 * {@link https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html}.
 * For the filtering part see
 * {@link https://glazedlists.github.io/glazedlists-tutorial/#text-filtering}.
 */
package de.wwu.scdh.teilsp.ui;

import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.Box;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import static ca.odell.glazedlists.swing.GlazedListsSwing.eventListModelWithThreadProxyList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;



/**
 * A dialog using list selection and a filter field.
 */
public class FilteringListSelectDialog
    extends ListSelectDialog {

    protected boolean EDITABLE = false;

    static Dimension MINIMUM_SIZE = new Dimension(400, 300);
    static Dimension MAXIMUM_SIZE = new Dimension(800, 400);

    public FilteringListSelectDialog() {
	super();
    }

    public FilteringListSelectDialog(Frame frame) {
	super(frame);
    }

    protected void setupSelectionComponent()
	throws ExtensionException {

	entriesEventList = new BasicEventList<LabelledEntry>();
	for (ILabelledEntriesProvider provider : providers) {
	    try {
		// TODO: We just pass the empty string here.
		List<LabelledEntry> entries = provider.getLabelledEntries("");
		entriesEventList.addAll(entries);
	    } catch (ExtensionException e) {
		String report = "";
		for (Map.Entry<String, String> argument : provider.getArguments().entrySet()) {
		    report += argument.getKey() + " = " + argument.getValue() + "\n";
		}
		throw new ExtensionException("Error reading entries\n\n"
					     + report + "\n\n" + e);
	    }
	}

	// sorted list
	Comparator<LabelledEntry> comparator = Comparator.comparing((entry) -> entry.getLabel());
	SortedList<LabelledEntry> sortedEntries =
	    new SortedList<>(entriesEventList, comparator); //new LabelledEntryComparator());

	// after sorting, get selected indices
	int i = 0;
	// we use data structures to gain performance
	Set<Integer> selectedIndicesSet = new HashSet<Integer>();
	Set<String> selectedValues = new HashSet<String>(currentValue);
	for (LabelledEntry entry : sortedEntries) {
	    if (selectedValues.contains(entry.getKey())) {
		selectedIndicesSet.add(i);
	    }
	    i++;
	}
	// cast set to array
	int[] selectedIndices = selectedIndicesSet.stream().mapToInt(Integer::intValue).toArray();

	// set up filtering
	JTextField filterEdit = new JTextField(20);
	filterEdit.setToolTipText("Filter");
	LabelledEntryFilterator filterator = new LabelledEntryFilterator();
	MatcherEditor<LabelledEntry> matcherEditor =
	    new TextComponentMatcherEditor<>(filterEdit, filterator);
	FilterList<LabelledEntry> textFilteredEntries = new FilterList<>(sortedEntries, matcherEditor);

	DefaultEventListModel<LabelledEntry> listModel =
	    eventListModelWithThreadProxyList(textFilteredEntries);

	// create the JList component
	jList = new JList<LabelledEntry>(listModel);
	jList.setSelectedIndices(selectedIndices);
	jList.setCellRenderer(new LabelledEntryListCellRenderer());
	if (isMultipleAllowed) {
	    jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	} else {
	    jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	// add the component to the pane
	selectionComponent.add(filterEdit);
	selectionComponent.add(Box.createRigidArea(new Dimension(0, 10)));
	selectionComponent.add(jList);

    	// ensure that selected is visible
	// FIXME: this does not work!
	jList.ensureIndexIsVisible(jList.getSelectedIndex());

    }

}

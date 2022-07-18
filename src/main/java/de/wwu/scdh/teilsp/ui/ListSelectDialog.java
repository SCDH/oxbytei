/**
 *
 * This class is designed after {@code ListDialog} in the Swing
 * tutorial, see
 * {@link https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html}.
 */
package de.wwu.scdh.teilsp.ui;

import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import static ca.odell.glazedlists.swing.GlazedListsSwing.eventListModelWithThreadProxyList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;



/**
 * A dialog using a combo box to select a single value.
 */
public class ListSelectDialog
    extends JDialog
    implements ActionListener, ISelectionDialog {

    protected boolean EDITABLE = false;

    static Dimension MINIMUM_SIZE = new Dimension(400, 300);
    static Dimension MAXIMUM_SIZE = new Dimension(800, 400);

    List<String> currentValue, selection;
    protected List<ILabelledEntriesProvider> providers;
    protected EventList<LabelledEntry> entriesEventList;
    protected JLabel label;
    protected JPanel comboBoxes;
    protected Map<String, String> arguments;
    protected JList<LabelledEntry> jList;


    public ListSelectDialog() {
	super();
    }

    public ListSelectDialog(Frame frame) {
	super(frame, true);
    }

    private static final ArgumentDescriptor<String> ARGUMENT_TITLE =
	ISelectionDialog.ARGUMENT_TITLE;

    private static final ArgumentDescriptor<URL> ARGUMENT_ICON =
	ISelectionDialog.ARGUMENT_ICON;

    public Map<String, String> getArguments() {
	return arguments;
    }

    private static final ArgumentDescriptor<?>[] ARGUMENTS = new ArgumentDescriptor<?>[] {
	ARGUMENT_TITLE,
        ARGUMENT_ICON};

    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	this.arguments = arguments;
	String title;
	title = ARGUMENT_TITLE.getValue(arguments);
	try {
	    URL icon = ARGUMENT_ICON.getValue(arguments);
	    ImageIcon askIcon = new ImageIcon(icon);
	    label = new JLabel(title, askIcon, SwingConstants.LEFT);
	} catch (ConfigurationException e) {
	    label = new JLabel(title);
	}

	setTitle(title);
	setLocationRelativeTo(null);
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	//setMinimumSize(MINIMUM_SIZE);
	//setMaximumSize(MAXIMUM_SIZE); // TODO: y is exceeded to screen height

	// ESC key
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			"Escape");
	getRootPane().getActionMap().put("Escape", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
		selection = null;
		dispose();
            }
       });
    }

    public void setup(List<String> currentVal,
		      List<ILabelledEntriesProvider> configured) {
	currentValue = currentVal;
	providers = configured;
    }

    /**
     * Do the user interaction part.
     *
     */
    public void doUserInteraction()
	throws ExtensionException {

	// create buttons
	JButton ok = new JButton("OK");
	// make OK the default button
	getRootPane().setDefaultButton(ok);
	ok.setActionCommand("OK");
	ok.addActionListener(this);
	JButton cancel = new JButton("Cancel");
	cancel.setActionCommand("Cancel");
	cancel.addActionListener(this);

	// a container for all our entries
	comboBoxes = new JPanel();
	comboBoxes.setLayout(new BoxLayout(comboBoxes, BoxLayout.Y_AXIS));

	setupSelectionComponent();

	// put the check boxes into a scroll pane
	JScrollPane entryScroller = new JScrollPane(comboBoxes);
	//entryScroller.setMaximumSize(MAXIMUM_SIZE);
	entryScroller.setAlignmentX(LEFT_ALIGNMENT);

	// label and scroller into the entry pane
	JPanel entryPane = new JPanel();
	entryPane.setLayout(new BoxLayout(entryPane, BoxLayout.PAGE_AXIS));
	label.setLabelFor(entryPane);
	entryPane.add(label);
	entryPane.add(Box.createRigidArea(new Dimension(0,5)));
	entryPane.add(entryScroller);
	entryPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

	// arrange buttons in buttons pane
	JPanel buttons = new JPanel();
	buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
	buttons.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	buttons.add(Box.createHorizontalGlue());
	buttons.add(cancel);
	buttons.add(Box.createRigidArea(new Dimension(10, 0)));
	buttons.add(ok);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(entryPane, BorderLayout.CENTER);
        contentPane.add(buttons, BorderLayout.PAGE_END);

	this.pack();
	setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
	if ("OK".equals(e.getActionCommand())) {
	    this.selection = getSelectionComponentSelection();
	    dispose();
	} else if ("Cancel".equals(e.getActionCommand())) {
	    selection = null;
	    dispose();
	}
    }

    protected List<String> getSelectionComponentSelection() {
	List<String> sel = new ArrayList<String>();
	for (LabelledEntry entry : jList.getSelectedValuesList()) {
	    sel.add(entry.getKey());
	}
	return sel;
    }

    protected void setupSelectionComponent()
	throws ExtensionException {

	entriesEventList = new BasicEventList<LabelledEntry>();
	int i = 0;
	Set<Integer> selectedIndicesSet = new HashSet<Integer>();
	Set<String> selectedValues = new HashSet<String>(currentValue);
	for (ILabelledEntriesProvider provider : providers) {
	    try {
		// TODO: We just pass the empty string here.
		List<LabelledEntry> entries = provider.getLabelledEntries("");
		entriesEventList.addAll(entries);
		for (LabelledEntry entry : entries) {
		    if (selectedValues.contains(entry.getKey())) {
			selectedIndicesSet.add(i);
		    }
		    i++;
		}
	    } catch (ExtensionException e) {
		String report = "";
		for (Map.Entry<String, String> argument : provider.getArguments().entrySet()) {
		    report += argument.getKey() + " = " + argument.getValue() + "\n";
		}
		throw new ExtensionException("Error reading entries\n\n"
					     + report + "\n\n" + e);
	    }
	}
	// look up current value
	//Integer[] selectedIndices = selectedIndicesSet.toArray(Integer[]::new);
	int[] selectedIndices = selectedIndicesSet.stream().mapToInt(Integer::intValue).toArray();

	DefaultEventListModel<LabelledEntry> listModel = eventListModelWithThreadProxyList(entriesEventList);
	jList = new JList<LabelledEntry>(listModel);
	jList.setSelectedIndices(selectedIndices);
	jList.setCellRenderer(new LabelledEntryListCellRenderer());
	comboBoxes.add(jList);
    }

    public List<String> getSelection() {
    	return selection;
    }
}

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
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;


/**
 * A multiple select dialog using check boxes.
 */
public class CheckBoxSelectDialog
    extends JDialog
    implements ActionListener, ISelectionDialog {

    static Dimension MINIMUM_SIZE = new Dimension(400, 300);
    static Dimension MAXIMUM_SIZE = new Dimension(800, 400);

    String title;
    URL icon;
    List<String> currentValue, selection;
    List<ILabelledEntriesProvider> providers;

    public CheckBoxSelectDialog() {
	super();
    }

    public CheckBoxSelectDialog(Frame frame) {
	super(frame, true);
    }

    public void init(String tit,
		     URL icon,
		     List<String> currentVal,
		     List<ILabelledEntriesProvider> configured) {
	title = tit;
	this.icon = icon;
	currentValue = currentVal;
	providers = configured;

	setTitle(title);
	setLocationRelativeTo(null);
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	setMinimumSize(MINIMUM_SIZE);
	setMaximumSize(MAXIMUM_SIZE); // TODO: y is exceeded to screen height

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
	JPanel checkBoxes = new JPanel();
	checkBoxes.setLayout(new BoxLayout(checkBoxes, BoxLayout.Y_AXIS));
	int height = 0; // checkBoxes.getHeight();
	int width = 0;
	int i;
	for (i = 0; i < providers.size(); i++) {
	    ILabelledEntriesProvider provider = providers.get(i);
	    try {
		// TODO: We just pass the empty string here.
		List<LabelledEntry> entries = provider.getLabelledEntries("");
		for (LabelledEntry entry : entries) {
		    // test if this item is among the current values
		    boolean selected = currentValue.contains(entry.getKey());
		    LabelledEntryCheckBox cb = new LabelledEntryCheckBox(entry, selected);
		    checkBoxes.add(cb);
		    height = height + cb.getHeight();
		    width = java.lang.Math.max(width, cb.getWidth());
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

	// put the check boxes into a scroll pane
	JScrollPane entryScroller = new JScrollPane(checkBoxes);
	//entryScroller.setMaximumSize(MAXIMUM_SIZE);
	entryScroller.setAlignmentX(LEFT_ALIGNMENT);

	// label and scroller into the entry pane
	JPanel entryPane = new JPanel();
	entryPane.setLayout(new BoxLayout(entryPane, BoxLayout.PAGE_AXIS));
	JLabel label;
	try {
	    ImageIcon askIcon = new ImageIcon(icon);
	    label = new JLabel(title, askIcon, SwingConstants.LEFT);
	} catch (Exception e) {
	    label = new JLabel(title);
	}
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
	    selection = new ArrayList<String>();
	    List<LabelledEntryCheckBox> cbs =
		SwingUtils.getDescendantsOfType(LabelledEntryCheckBox.class, this);
	    for (LabelledEntryCheckBox cb : cbs) {
		if (cb.isSelected()) {
		    selection.add(cb.getEntry().getKey());
		}
	    }
	    dispose();
	} else if ("Cancel".equals(e.getActionCommand())) {
	    selection = null;
	    dispose();
	}
    }

    public List<String> getSelection() {
    	return selection;
    }
}

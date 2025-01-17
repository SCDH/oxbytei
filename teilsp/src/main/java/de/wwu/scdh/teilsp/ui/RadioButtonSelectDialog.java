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

import de.wwu.scdh.teilsp.exceptions.ConfigurationException;
import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor;


/**
 * A multiple select dialog using check boxes.
 */
public class RadioButtonSelectDialog
    extends JDialog
    implements ActionListener, ISelectionDialog {

    static Dimension MINIMUM_SIZE = new Dimension(400, 300);
    static Dimension MAXIMUM_SIZE = new Dimension(800, 400);

    List<String> currentValue, selection;
    List<ILabelledEntriesProvider> providers;
    Map<String, String> arguments;

    JLabel label;

    public RadioButtonSelectDialog() {
	super();
    }

    public RadioButtonSelectDialog(Frame frame) {
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
	    ARGUMENT_ICON
	    };
    
    public ArgumentDescriptor<?>[] getArgumentDescriptor() {
	return ARGUMENTS;
    }	

    public void init(Map<String, String> arguments)
	throws ConfigurationException {
	this.arguments = arguments;
	String title = ARGUMENT_TITLE.getValue(arguments);
	URL icon = ARGUMENT_ICON.getValue(arguments);
	if (arguments.containsKey("icon")) {
	    try {
		ImageIcon askIcon = new ImageIcon(icon);
		label = new JLabel(title, askIcon, SwingConstants.LEFT);
	    } catch (Exception e) {
		label = new JLabel(title);
	    }
	} else {
	    label = new JLabel(title);
	}

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

    

    public void setup(List<String> currentVal,
		      List<ILabelledEntriesProvider> providers) {
	currentValue = currentVal;
	this.providers = providers;
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
	JPanel radioButtons = new JPanel();
	radioButtons.setLayout(new BoxLayout(radioButtons, BoxLayout.Y_AXIS));
	int height = 0; // radioButtons.getHeight();
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
		    LabelledEntryRadioButton cb = new LabelledEntryRadioButton(entry, selected);
		    radioButtons.add(cb);
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
	JScrollPane entryScroller = new JScrollPane(radioButtons);
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
	    selection = new ArrayList<String>();
	    List<LabelledEntryRadioButton> cbs =
		SwingUtils.getDescendantsOfType(LabelledEntryRadioButton.class, this);
	    for (LabelledEntryRadioButton cb : cbs) {
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

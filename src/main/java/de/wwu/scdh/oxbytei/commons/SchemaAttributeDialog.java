/**
 *
 * This class is designed after {@code ListDialog} in the Swing
 * tutorial, see
 * {@link https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html}.
 */
package de.wwu.scdh.oxbytei.commons;

import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import ro.sync.contentcompletion.xml.CIAttribute;

import de.wwu.scdh.teilsp.services.extensions.ILabelledEntriesProvider;
import de.wwu.scdh.teilsp.services.extensions.LabelledEntry;
import de.wwu.scdh.teilsp.services.extensions.ExtensionException;
import de.wwu.scdh.teilsp.ui.ISelectionDialog;
import de.wwu.scdh.teilsp.ui.KeySelectionRenderer;
import de.wwu.scdh.oxbytei.commons.WSDocumentReader;


/**
 * A dialog using a combo box to select a single value.
 */
public class SchemaAttributeDialog
    extends JDialog
    implements ActionListener {

    static Dimension MINIMUM_SIZE = new Dimension(400, 300);
    static Dimension MAXIMUM_SIZE = new Dimension(800, 400);
    static String TITLE = "Attribute Editor";

    URL icon;
    Vector<CIAttribute> attributes;
    String attributeName, attributeNamespace, attributePrefix, currentValue;
    List<String> attributeValue;
    boolean attributeRemoved;

    // UI components that we want to access
    JComboBox<CIAttribute> comboBox;
    JTextArea valueArea;
    JScrollPane valueScroller;

    WSDocumentReader documentReader;
    String location;

    public SchemaAttributeDialog() {
	super();
    }

    public SchemaAttributeDialog
	(Frame frame,
	 WSDocumentReader documentReader,
	 String location,
	 URL icon,
	 Vector<CIAttribute> attributes) {
	super(frame, true);
	this.documentReader = documentReader;
	this.location = location;
	this.icon = icon;
	this.attributes = attributes;

	// default values
	this.attributeName = null;
	this.attributeNamespace = null;
	this.attributePrefix = null;
	this.attributeValue = null;
	this.attributeRemoved = false;

	setTitle(TITLE);
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
		attributeName = null;
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
	JButton edit = new JButton("Edit");
	// make OK the default button
	getRootPane().setDefaultButton(edit);
	edit.setActionCommand("Edit");
	edit.addActionListener(this);
	JButton remove = new JButton("Remove");
	remove.setActionCommand("Remove");
	remove.addActionListener(this);
	JButton cancel = new JButton("Cancel");
	cancel.setActionCommand("Cancel");
	cancel.addActionListener(this);

	// a container for all our entries
	comboBox = new JComboBox<CIAttribute>(attributes);
	comboBox.addActionListener(this);

	KeySelectionRenderer renderer = new KeySelectionRenderer(comboBox) {
		@Override
		public String getDisplayValue(Object value)
		{
		    CIAttribute attr = (CIAttribute) value;
		    String prefix = "";
		    if (attr.getPrefix() != null) {
		    	if (!attr.getPrefix().isEmpty()) {
		    	    prefix = ":" + attr.getPrefix();
		    	}
		    }
		    return prefix + attr.getName();
		}
	    };

	// text area for displaying the value, make it scrollable
	// the scroll bar takes up a row, so we intialize with 2 rows
	valueArea = new JTextArea("no attribute selected", 1, 30);
	valueScroller = new JScrollPane(valueArea);
	valueArea.setEditable(false);
	valueArea.setBackground(this.getBackground()); // Color.LIGHT_GRAY);
	valueArea.setForeground(Color.LIGHT_GRAY);
	//entryScroller.setMaximumSize(MAXIMUM_SIZE);
	//valueScroller.setAlignmentX(LEFT_ALIGNMENT);
	// invisible as long as no attribute is selected
	//valueScroller.setVisible(false);

	// label and scroller into the entry pane
	JPanel entryPane = new JPanel();
	entryPane.setLayout(new BoxLayout(entryPane, BoxLayout.PAGE_AXIS));
	JLabel label;
	try {
	    ImageIcon askIcon = new ImageIcon(icon);
	    label = new JLabel(TITLE, askIcon, SwingConstants.LEFT);
	} catch (Exception e) {
	    label = new JLabel(TITLE);
	}
	label.setLabelFor(entryPane);
	entryPane.add(label);
	entryPane.add(Box.createRigidArea(new Dimension(0,5)));
	entryPane.add(comboBox);
	entryPane.add(Box.createRigidArea(new Dimension(0,5)));
	entryPane.add(valueScroller);
	entryPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

	// arrange buttons in buttons pane
	JPanel buttons = new JPanel();
	buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
	buttons.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	buttons.add(Box.createHorizontalGlue());
	buttons.add(cancel);
	buttons.add(Box.createRigidArea(new Dimension(10, 0)));
	buttons.add(edit);
	buttons.add(Box.createRigidArea(new Dimension(10, 0)));
	buttons.add(remove);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(entryPane, BorderLayout.CENTER);
        contentPane.add(buttons, BorderLayout.PAGE_END);

	// set values according to default state of combo box
	updateSelectedAttribute();

	this.pack();
	setVisible(true);
    }

    protected void updateSelectedAttribute() {
	CIAttribute attr = (CIAttribute) comboBox.getSelectedItem();
	attributeName = attr.getName();
	attributeNamespace = attr.getNamespace();
	attributePrefix = attr.getPrefix();
	if (attributeName.contains(":")) {
	    attributeNamespace = null;
	}
	currentValue = documentReader.lookupAttributeValue(location, attributeName, attributeNamespace);
	if (currentValue == null) {
	    currentValue = "";
	}
	valueArea.setText(currentValue);
	valueArea.setForeground(Color.DARK_GRAY);
	valueArea.validate();
    }

    public void actionPerformed(ActionEvent e) {
	if (comboBox.equals(e.getSource())) {
	    updateSelectedAttribute();
	    //valueScroller.setVisible(true);
	    this.pack();
	    this.validate();
	    System.out.println("Attribute selected: " + attributeName
			       + " in namespace " + attributeNamespace
			       + " with prefix " + attributePrefix);
	} else if ("Edit".equals(e.getActionCommand())) {
	    // TODO
	    dispose();
	} else if ("Remove".equals(e.getActionCommand())) {
	    attributeRemoved = true;
	    dispose();
	} else if ("Cancel".equals(e.getActionCommand())) {
	    attributeValue = null;
	    attributeRemoved = false;
	    dispose();
	}
    }

    public String getAttributeName() {
	return attributeName;
    }

    public String getAttributeNamespace() {
	return attributeNamespace;
    }

    public String getAttributePrefix() {
	return attributePrefix;
    }

    public List<String> getAttributeValue() {
    	return attributeValue;
    }

    public boolean getAttributeRemoved() {
	return attributeRemoved;
    }

}

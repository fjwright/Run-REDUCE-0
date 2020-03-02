package fjwright.runreduce;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class provides the basis for a modal dialog to select open output files to shut or packages to load.
 */
abstract class RunREDUCEListDialog<E> extends JDialog implements ActionListener {
    protected Frame frame;
    protected JList<E> list = new JList<>();

    RunREDUCEListDialog(Frame frame, String dialogTitle, String listLabel, String buttonText) {
        // Create a modal dialog:
        super(frame, dialogTitle, true);
        this.frame = frame;

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(listLabel);
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0, 5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create and initialize the control buttons:
        JButton shutButton = new JButton(buttonText);
        shutButton.setActionCommand(buttonText);
        shutButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        // Lay out the buttons horizontally and right-justified:
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(shutButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);

        // Lay out the dialog contents:
        add(listPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }
}

/**
 * This class provides a modal dialog to select open output files to shut.
 */
class ShutOutputFilesDialog extends RunREDUCEListDialog<File> {
    private static int[] fileIndices;

    ShutOutputFilesDialog(Frame frame) {
        super(frame,
                "Shut Output Files...",
                "Select one or more open output files to shut...",
                "Shut");
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(5);
    }

    int[] showDialog(List<File> outputFileList) {
        list.setListData(outputFileList.toArray(new File[0]));
        pack();                       // must be done dynamically
        setLocationRelativeTo(frame); // ditto
        setVisible(true);
        return fileIndices;
    }

    public void actionPerformed(ActionEvent e) {
        if ("Shut".equals(e.getActionCommand()))
            fileIndices = list.getSelectedIndices();
        else
            fileIndices = new int[0];
        setVisible(false);
    }
}

/**
 * This class provides a modal dialog to select packages to load.
 */
class LoadPackagesDialog extends RunREDUCEListDialog<String> {
    private List<String> selectedPackages;

    LoadPackagesDialog(Frame frame) {
        super(frame,
                "Load Packages...",
                "Select one or more packages to load...",
                "Load");
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(20);
    }

    List<String> showDialog(List<String> packageList) {
        list.setListData(packageList.toArray(new String[0]));
        pack();                       // must be done dynamically
        setLocationRelativeTo(frame); // ditto
        setVisible(true);
        return selectedPackages;
    }

    public void actionPerformed(ActionEvent e) {
        if ("Load".equals(e.getActionCommand()))
            selectedPackages = list.getSelectedValuesList();
        else
            selectedPackages = new ArrayList<>(0);
        setVisible(false);
    }
}

/**
 * This class provides a modal dialog to change the REDUCE I/O font size.
 */
class FontSizeDialog extends JDialog implements ActionListener, ChangeListener {
    private Frame frame;
    private JTextField currentSizeDemoTextField;
    private JTextField currentSizeValueTextField;
    private JTextField newSizeDemoTextField;
    private JSpinner newSizeValueSpinner;
    private Font currentFont;
    private Font newFont;
    private float newFontSize;

    FontSizeDialog(Frame frame) {
        // Create a modal dialog:
        super(frame, "Font Size...", true);
        this.frame = frame;

        JPanel fontPane = new JPanel(new GridBagLayout()); // Layout(2, 2, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.insets = new Insets(5,5,5,5);

        JTextField defaultFontSizeTextField = new JTextField("Default font size is 12.");
        defaultFontSizeTextField.setEditable(false);
        defaultFontSizeTextField.setHorizontalAlignment(JTextField.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        fontPane.add(defaultFontSizeTextField, c);

        currentSizeDemoTextField = new JTextField("Text at current size");
        currentSizeDemoTextField.setEditable(false);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        fontPane.add(currentSizeDemoTextField, c);

        currentSizeValueTextField = new JTextField("12");
        currentSizeValueTextField.setEditable(false);
        currentSizeValueTextField.setHorizontalAlignment(JTextField.CENTER);
        c.gridx = 1;
        c.gridy = 1;
        fontPane.add(currentSizeValueTextField, c);

        newSizeDemoTextField = new JTextField("Text at new size");
        newSizeDemoTextField.setEditable(false);
        c.gridx = 0;
        c.gridy = 2;
        fontPane.add(newSizeDemoTextField, c);

        SpinnerModel spinnerModel = new SpinnerNumberModel(10, 5, 30, 1);
        newSizeValueSpinner = new JSpinner(spinnerModel);
        c.gridx = 1;
        c.gridy = 2;
        fontPane.add(newSizeValueSpinner, c);
        fontPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create and initialize the control buttons:
        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        // Lay out the buttons horizontally and right-justified:
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(okButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);

        // Lay out the dialog contents:
        add(fontPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    void showDialog() {
        currentFont = RunREDUCE.outputTextArea.getFont();
        int currentSize = currentFont.getSize();
        currentSizeDemoTextField.setFont(currentFont);
        currentSizeValueTextField.setText(String.valueOf(currentSize));
        newSizeDemoTextField.setFont(currentFont);
        newSizeValueSpinner.setValue(currentSize);
        //Listen for changes on the spinner.
        newSizeValueSpinner.addChangeListener(this);
        pack();                       // must be done dynamically
        setLocationRelativeTo(frame); // ditto
        setVisible(true);
    }

    public void stateChanged(ChangeEvent e) {
        newFontSize = (int) newSizeValueSpinner.getValue();
        newFont = currentFont.deriveFont(newFontSize);
        newSizeDemoTextField.setFont(newFont);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        // Update the font size
        if ("OK".equals(e.getActionCommand())) {
            RunREDUCE.outputTextArea.setFont(newFont);
            RunREDUCE.inputTextArea.setFont(newFont);
            FindREDUCE.prefs.putFloat("fontSize", newFontSize);
        }
        setVisible(false);
    }
}

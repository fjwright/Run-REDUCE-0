package fjwright.runreduce;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class REDUCEConfigDialog extends JDialog {
    private final Frame frame;
    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JButton resetAllDefaultsButton;
    private JTextField defaultRootDirTextField;
    private JTextField packagesRootDirTextField;
    static JList<PlainDocument> versionsJList;
    private JButton deleteVersionButton;
    private JButton duplicateVersionButton;
    private JButton addVersionButton;
    private JTextField versionNameTextField;
    private JTextField versionRootDirTextField;
    private JTextField commandPathNameTextField;
    private Color backgroundColor;
    private final Insets textInsets = new Insets(0, 0, 3, 0);
    private final Font textAreaFont = new JTextField().getFont();

    static final int nArgs = 5;
    private final JLabel[] argLabels = new JLabel[nArgs];
    private final JTextField[] args = new JTextField[nArgs];
    static REDUCECommandDocumentsList reduceCommandDocumentsList;
    private REDUCEConfigData reduceConfigData;

    public REDUCEConfigDialog(Frame frame) {
        super(frame, "Configure REDUCE Directories and Commands", true);
        this.frame = frame;
        createUIComponents();
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonSave);

        buttonSave.addActionListener(e -> onSave());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        resetAllDefaultsButton.addActionListener(e -> resetAllDefaults());
        deleteVersionButton.addActionListener(e -> deleteVersion());
        duplicateVersionButton.addActionListener(e -> duplicateVersion());
        addVersionButton.addActionListener(e -> addVersion());
    }

    private void onCancel() {
        setVisible(false);
    }

    private void createUIComponents() {
        contentPane = new JPanel(new BorderLayout());
        backgroundColor = contentPane.getBackground();

        int textFieldColumns = 40;
        GridBagConstraints mainPaneGBC, gbc;
        Insets insets = new Insets(5, 5, 5, 5);
        // Preserve descenders in JTextArea and JTextField components:

        // buttonPane contains the Save and Cancel buttons.
        final JPanel buttonPane = new JPanel();
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        buttonSave = new JButton("Save");
        buttonPane.add(buttonSave);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonCancel = new JButton("Cancel");
        buttonPane.add(buttonCancel);

        // mainPane contains everything above the Save and Cancel buttons:
        final JPanel mainPane = new JPanel(new GridBagLayout());
        contentPane.add(mainPane, BorderLayout.CENTER);
        mainPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPaneGBC = new GridBagConstraints();
        mainPaneGBC.weightx = 1.0;
        mainPaneGBC.weighty = 1.0;
        mainPaneGBC.fill = GridBagConstraints.BOTH;
        mainPaneGBC.insets = new Insets(5, 5, 5, 5);

        Border border = BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Color.black, 2, true),
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                BorderFactory.createEmptyBorder(5, 5, 5, 5));

        resetAllDefaultsButton = new JButton("Reset All Defaults");
        mainPaneGBC.gridx = 0;
        mainPaneGBC.gridy = 0;
        mainPaneGBC.fill = GridBagConstraints.NONE;
        mainPane.add(resetAllDefaultsButton, mainPaneGBC);

        // rootDirsPane contains the default and packages root directories:
        final JPanel rootDirsPane = new JPanel();
        rootDirsPane.setLayout(new GridBagLayout());
        mainPaneGBC.gridx = 1;
        mainPaneGBC.gridy = 0;
        mainPaneGBC.fill = GridBagConstraints.BOTH;
        mainPane.add(rootDirsPane, mainPaneGBC);
        rootDirsPane.setBorder(border);

        String text1 = "If 'Default Root Dir' is blank on start-up, it takes " +
                "the value of the environment variable named REDUCE if it is set.";
        if (RRPreferences.windowsOS)
            text1 += " Otherwise, Run-REDUCE searches for a standard installation folder (on Windows only).";
        final JTextArea textArea1 = newJTextArea(text1);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        rootDirsPane.add(textArea1, gbc);

        final JLabel defaultRootDirLabel = new JLabel("Default Root Dir");
        defaultRootDirLabel.setLabelFor(defaultRootDirTextField);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        rootDirsPane.add(defaultRootDirLabel, gbc);
        defaultRootDirTextField = newJTextField(textFieldColumns);
        defaultRootDirTextField.setToolTipText("As an optional convenience, " +
                "specify a root directory path that provides a default for all other root directories.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        rootDirsPane.add(defaultRootDirTextField, gbc);

        final JTextArea textArea2 = newJTextArea(
                "If 'Packages Root Dir' is blank on start-up, it takes the platform default value.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        rootDirsPane.add(textArea2, gbc);

        final JLabel packagesRootDirLabel = new JLabel("Packages Root Dir");
        packagesRootDirLabel.setLabelFor(packagesRootDirTextField);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        rootDirsPane.add(packagesRootDirLabel, gbc);
        packagesRootDirTextField = newJTextField(textFieldColumns);
        packagesRootDirTextField.setToolTipText("A directory containing a standard REDUCE packages directory, which defaults to 'Default Root Dir'.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        rootDirsPane.add(packagesRootDirTextField, gbc);

        // versionsPane contains the list of REDUCE versions:
        final JPanel versionsPane = new JPanel();
        versionsPane.setLayout(new BoxLayout(versionsPane, BoxLayout.PAGE_AXIS));
        mainPaneGBC.gridx = 0;
        mainPaneGBC.gridy = 1;
        mainPane.add(versionsPane, mainPaneGBC);
//        versionsPane.setBorder(border);
        versionsPane.add(Box.createVerticalGlue());
        final JLabel versionsLabel = new JLabel("Select REDUCE Version:");
        versionsLabel.setLabelFor(versionsJList);
        versionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(versionsLabel);
        versionsPane.add(Box.createVerticalGlue());
        versionsJList = new JList<>();
        versionsJList.setCellRenderer(new VersionsJListCellRenderer());
        versionsJList.setBorder(border);
        versionsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versionsJList.setVisibleRowCount(0);
        versionsJList.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(versionsJList);
        versionsPane.add(Box.createVerticalGlue());
        deleteVersionButton = new JButton("Delete Selected Version");
        deleteVersionButton.setToolTipText("Delete the configuration for the selected version of REDUCE.");
        deleteVersionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(deleteVersionButton);
        versionsPane.add(Box.createVerticalStrut(10));
        duplicateVersionButton = new JButton("Duplicate Selected Version");
        duplicateVersionButton.setToolTipText("Duplicate the configuration for the selected version of REDUCE below it.");
        duplicateVersionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(duplicateVersionButton);
        versionsPane.add(Box.createVerticalStrut(10));
        addVersionButton = new JButton("Add a New Version");
        addVersionButton.setToolTipText("Add a configuration for a new version of REDUCE.");
        addVersionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(addVersionButton);
        versionsPane.add(Box.createVerticalGlue());

        // commandPane contains the command for a version of REDUCE:
        final JPanel commandPane = new JPanel();
        commandPane.setLayout(new GridBagLayout());
        mainPaneGBC.gridx = 1;
        mainPaneGBC.gridy = 1;
        mainPane.add(commandPane, mainPaneGBC);
        commandPane.setBorder(border);

        final JLabel versionNameLabel = new JLabel("Version Name");
        versionNameLabel.setLabelFor(versionNameTextField);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        commandPane.add(versionNameLabel, gbc);
        versionNameTextField = newJTextField(textFieldColumns);
        versionNameTextField.setToolTipText("An arbitrary name used to identify this version of REDUCE.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        commandPane.add(versionNameTextField, gbc);

        final JLabel versionRootDirLabel = new JLabel("Version Root Dir");
        versionRootDirLabel.setLabelFor(versionRootDirTextField);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        commandPane.add(versionRootDirLabel, gbc);
        versionRootDirTextField = newJTextField(textFieldColumns);
        versionRootDirTextField.setToolTipText("As an optional convenience, " +
                "specify a root directory path that can be referenced as $REDUCE in the command path name and arguments below.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        commandPane.add(versionRootDirTextField, gbc);

        final JTextArea textArea3 = newJTextArea("$REDUCE below is replaced by 'Version Root Dir' " +
                "if it is set, otherwise by 'Default Root Dir'.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        commandPane.add(textArea3, gbc);

        final JLabel commandPathNameLabel = new JLabel("Command Path Name");
        commandPathNameLabel.setLabelFor(commandPathNameTextField);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        commandPane.add(commandPathNameLabel, gbc);
        commandPathNameTextField = newJTextField(textFieldColumns);
        commandPathNameTextField.setToolTipText("The filename part of the command to run REDUCE, which can optionally begin with $REDUCE.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        commandPane.add(commandPathNameTextField, gbc);

        for (int i = 0; i < nArgs; i++) {
            argLabels[i] = new JLabel("Argument " + (i + 1));
            argLabels[i].setLabelFor(args[i]);
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 4 + i;
            gbc.weightx = 0;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = insets;
            commandPane.add(argLabels[i], gbc);
            args[i] = newJTextField(textFieldColumns);
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 4 + i;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = insets;
            commandPane.add(args[i], gbc);
        }
    }

    private JTextArea newJTextArea(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setMargin(textInsets);
        textArea.setBackground(backgroundColor);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        // This seems necessary when not using the default look and feel:
        textArea.setFont(textAreaFont);
        return textArea;
    }

    private JTextField newJTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setMargin(textInsets);
        return textField;
    }

    public void showDialog() {
        reduceConfigData = new REDUCEConfigData(RunREDUCE.reduceConfiguration);
        updateDialog(reduceConfigData);
        pack();                       // must be done dynamically
        setLocationRelativeTo(frame); // ditto
        /*
         * setVisible(true): If a modal dialog is not already visible, this call will *not return*
         * until the dialog is hidden by calling setVisible(false) or dispose.
         * So it must be last in this method!
         */
        setVisible(true);
    }

    private void resetAllDefaults() {
        reduceConfigData = new REDUCEConfigData(RunREDUCE.reduceConfigurationDefault);
        updateDialog(reduceConfigData);
    }

    public void updateDialog(REDUCEConfigData reduceConfigData) {
        defaultRootDirTextField.setDocument(reduceConfigData.reduceRootDir);
        packagesRootDirTextField.setDocument(reduceConfigData.packagesRootDir);
        reduceCommandDocumentsList = reduceConfigData.reduceCommandDocumentsList;
        versionsJList.setModel(reduceCommandDocumentsList);
        versionsJList.setSelectedIndex(0);
        showREDUCECommand(reduceCommandDocumentsList.get(0));
        versionsJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // user has finished selecting
                int selectedIndex = versionsJList.getSelectedIndex();
                // ListSelectionListener gets fired by list update when SelectedIndex is invalid, so...
                if (0 <= selectedIndex && selectedIndex < reduceCommandDocumentsList.size())
                    showREDUCECommand(reduceCommandDocumentsList.get(selectedIndex));
            }
        });
    }

    private void showREDUCECommand(REDUCECommandDocuments cmd) {
        versionNameTextField.setDocument(cmd.version);
        versionRootDirTextField.setDocument(cmd.versionRootDir);
        ArrayList<PlainDocument> command = cmd.command;
        commandPathNameTextField.setDocument(command.get(0));
        for (int i = 0; i < nArgs; i++) args[i].setDocument(command.get(i + 1));
    }

    private void onSave() {
        // Write form data back to REDUCEConfiguration:
        reduceConfigData.save();
        RunREDUCE.reduceConfiguration.save();
        setVisible(false);
    }

    private void deleteVersion() {
        int selectedIndex = versionsJList.getSelectedIndex();
        if (selectedIndex >= 0) {
            reduceCommandDocumentsList.remove(selectedIndex);
            reduceCommandDocumentsList.removeUpdate(selectedIndex);
            int size = reduceCommandDocumentsList.size(); // new size!
            if (size > 0) {
                if (selectedIndex >= size) selectedIndex = 0;
                versionsJList.setSelectedIndex(selectedIndex);
                showREDUCECommand(reduceCommandDocumentsList.get(selectedIndex));
            } else
                addVersion();
        }
    }

    private void duplicateVersion() {
        int selectedIndex = versionsJList.getSelectedIndex();
        REDUCECommandDocuments oldCmd = reduceCommandDocumentsList.get(selectedIndex++);
        // selectedIndex is now incremented to the index of the duplicate entry.
        REDUCECommandDocuments newCmd = new REDUCECommandDocuments(
                oldCmd.version.getText() + " NEW",
                oldCmd.versionRootDir.getText(),
                oldCmd.command.stream().map(PlainDocument::getText).toArray(String[]::new));
        reduceCommandDocumentsList.add(selectedIndex, newCmd);
        reduceCommandDocumentsList.insertUpdate(selectedIndex);
        versionsJList.setSelectedIndex(selectedIndex);
        showREDUCECommand(newCmd);
    }

    private void addVersion() {
        REDUCECommandDocuments newCmd = new REDUCECommandDocuments("NEW VERSION");
        reduceCommandDocumentsList.add(newCmd);
        int newIndex = reduceCommandDocumentsList.size() - 1;
        reduceCommandDocumentsList.insertUpdate(newIndex);
        versionsJList.setSelectedIndex(newIndex);
        showREDUCECommand(newCmd);
    }
}

/**
 * Render a cell of versionsJList by displaying a string for each document in the list;
 * see the JList API documentation for the example on which I based this class.
 */
class VersionsJListCellRenderer extends JLabel implements ListCellRenderer<Object> {
    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.
    public Component getListCellRendererComponent(
            JList<?> list,           // the list
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // does the cell have focus
    {
        setText(((PlainDocument) value).getText());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}

class PlainDocument extends javax.swing.text.PlainDocument {
    String getText() {
        String text = "";
        try {
            text = getText(0, getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return text;
    }

    void insertString(String str) {
        try {
            insertString(0, str, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}

class REDUCECommandDocuments {
    PlainDocument version;
    PlainDocument versionRootDir;
    ArrayList<PlainDocument> command;

    REDUCECommandDocuments(String version) {
        this(version, "", "");
    }

    REDUCECommandDocuments(String version, String versionRootDir, String... command) {
        this.version = new PlainDocument();
        this.version.insertString(version);
        this.version.addDocumentListener(new VersionDocumentListener());
        this.versionRootDir = new PlainDocument();
        this.versionRootDir.insertString(versionRootDir);
        this.command = new ArrayList<>(REDUCEConfigDialog.nArgs + 1);
        for (int i = 0; i <= REDUCEConfigDialog.nArgs; i++) {
            PlainDocument doc = new PlainDocument();
            doc.insertString(i < command.length ? command[i] : "");
            this.command.add(doc);
        }
    }

    private static class VersionDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            changedUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            changedUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            changedUpdate();
        }

        private void changedUpdate() {
            int selectedIndex = REDUCEConfigDialog.versionsJList.getSelectedIndex();
            REDUCEConfigDialog.reduceCommandDocumentsList.changedUpdate(selectedIndex);
        }
    }
}

class REDUCECommandDocumentsList extends AbstractListModel<PlainDocument> implements Iterable<REDUCECommandDocuments> {
    private final ArrayList<REDUCECommandDocuments> reduceCommandDocumentsList = new ArrayList<>();

    REDUCECommandDocumentsList(REDUCEConfigurationType reduceConfiguration) {
        for (REDUCECommand cmd : reduceConfiguration.reduceCommandList)
            reduceCommandDocumentsList.add(new REDUCECommandDocuments(cmd.version, cmd.versionRootDir, cmd.command));
    }

    // ArrayList methods that I use:

    // Inserts the specified element at the specified position in this list.
    void add(int index, REDUCECommandDocuments element) {
        reduceCommandDocumentsList.add(index, element);
    }

    // Appends the specified element to the end of this list.
    void add(REDUCECommandDocuments element) {
        reduceCommandDocumentsList.add(element);
    }

    // Returns the element at the specified position in this list.
    REDUCECommandDocuments get(int index) {
        return reduceCommandDocumentsList.get(index);
    }

    // Removes the element at the specified position in this list.
    void remove(int index) {
        reduceCommandDocumentsList.remove(index);
    }

    // Returns the number of elements in this list.
    int size() {
        return reduceCommandDocumentsList.size();
    }

    // AbstractListModel methods that need to be overridden or called:

    @Override
    public int getSize() {
        return reduceCommandDocumentsList.size();
    }

    @Override
    public PlainDocument getElementAt(int index) {
        return reduceCommandDocumentsList.get(index).version;
    }

    public void insertUpdate(int index) {
        // AbstractListModel subclasses must call this method after adding one or more elements to the model:
        fireIntervalAdded(this, index, index);
    }

    public void removeUpdate(int index) {
        // AbstractListModel subclasses must call this method after removing one or more elements from the model:
        fireIntervalRemoved(this, index, index);
    }

    public void changedUpdate(int index) {
        // AbstractListModel subclasses must call this method after changing one or more elements of the model:
        fireContentsChanged(this, index, index);
    }

    // Iterable methods that need to be overridden to use the enhanced for statement:

    @Override
    public Iterator<REDUCECommandDocuments> iterator() {
        return reduceCommandDocumentsList.iterator();
    }

    @Override
    public void forEach(Consumer<? super REDUCECommandDocuments> action) {
        reduceCommandDocumentsList.forEach(action);
    }

    @Override
    public Spliterator<REDUCECommandDocuments> spliterator() {
        return reduceCommandDocumentsList.spliterator();
    }
}

/**
 * The constructor initialises this class to represent the current status, editing the above dialogue updates it, and
 * closing the dialogue via the Save button copies it back to the relevant data structures.
 */
class REDUCEConfigData {
    PlainDocument reduceRootDir;
    PlainDocument packagesRootDir;
    REDUCECommandDocumentsList reduceCommandDocumentsList;

    REDUCEConfigData(REDUCEConfigurationType reduceConfiguration) {
        try {
            reduceRootDir = new PlainDocument();
            reduceRootDir.insertString(0, reduceConfiguration.reduceRootDir, null);
            packagesRootDir = new PlainDocument();
            packagesRootDir.insertString(0, reduceConfiguration.packagesRootDir, null);
            reduceCommandDocumentsList = new REDUCECommandDocumentsList(reduceConfiguration);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void save() {
        // Write form data back to REDUCEConfiguration:
        RunREDUCE.reduceConfiguration.reduceRootDir = reduceRootDir.getText().trim();
        RunREDUCE.reduceConfiguration.packagesRootDir = packagesRootDir.getText().trim();
        RunREDUCE.reduceConfiguration.reduceCommandList = new REDUCECommandList();
        for (REDUCECommandDocuments cmd : reduceCommandDocumentsList) {
            RunREDUCE.reduceConfiguration.reduceCommandList.add(new REDUCECommand(
                    cmd.version.getText().trim(),
                    cmd.versionRootDir.getText().trim(),
                    // Do not save blank arguments:
                    cmd.command.stream().map(e -> e.getText().trim()).
                            filter(e -> !e.isEmpty()).toArray(String[]::new)));
        }
        // Rebuild submenus that depend on RunREDUCECommandList.
        // Only really need to do this if the version list changes in some way!
        RRMenuBar.runREDUCESubmenuBuild();
        RRMenuBar.autoRunREDUCESubmenuBuild();
    }
}

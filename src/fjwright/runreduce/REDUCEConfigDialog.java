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

public class REDUCEConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JButton resetAllDefaultsButton;
    private JTextField defaultRootDirTextField;
    private JTextField packagesRootDirTextField;
    static DefaultListModel<String> listModel;
    static JList<String> versionsJList;
    private JButton deleteVersionButton;
    private JButton addVersionButton;
    private JTextField versionNameTextField;
    private JTextField versionRootDirTextField;
    private JTextField commandPathNameTextField;
    static final int nArgs = 5;
    private final JLabel[] argLabels = new JLabel[nArgs];
    private final JTextField[] args = new JTextField[nArgs];
    private REDUCECommandDocumentsList reduceCommandDocumentsList;
    private REDUCEConfigData reduceConfigData;

    public REDUCEConfigDialog(Frame frame) {
        super(frame, "Configure REDUCE Directories and Commands", true);
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
        addVersionButton.addActionListener(e -> addVersion());
    }

    private void onCancel() {
        setVisible(false);
    }

    // Only for testing!
    public static void main(String[] args) {
        REDUCEConfigDialog dialog = new REDUCEConfigDialog(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        contentPane = new JPanel(new BorderLayout());
        Color backgroundColor = contentPane.getBackground();

        int textFieldColumns = 40;
        GridBagConstraints mainPaneGBC, gbc;
        Insets insets = new Insets(5, 5, 5, 5);

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

        final JTextArea textArea1 = new JTextArea("'Default Root Dir' defaults " +
                "to the value of the environment variable named REDUCE if it is set. " +
                "Otherwise, on Windows only, Run-REDUCE searches for a standard installation folder.");
        textArea1.setBackground(backgroundColor);
        textArea1.setEditable(false);
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);
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
        defaultRootDirTextField = new JTextField(textFieldColumns);
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

        final JTextArea textArea2 = new JTextArea("If 'Packages Root Dir' is empty, it is replaced by 'Default Root Dir'.");
        textArea2.setBackground(backgroundColor);
        textArea2.setEditable(false);
        textArea2.setLineWrap(true);
        textArea2.setWrapStyleWord(true);
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
        packagesRootDirTextField = new JTextField(textFieldColumns);
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
        versionsPane.setBorder(border);
        final JLabel versionsLabel = new JLabel("Select REDUCE Version:");
        versionsLabel.setLabelFor(versionsJList);
        versionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(versionsLabel);
        versionsPane.add(Box.createVerticalGlue());
        versionsJList = new JList<>();
        versionsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versionsJList.setVisibleRowCount(0);
        versionsJList.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(versionsJList);
        versionsPane.add(Box.createVerticalGlue());
        deleteVersionButton = new JButton("Delete Selected Version");
        deleteVersionButton.setToolTipText("Delete the configuration for the selected version of REDUCE.");
        deleteVersionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(deleteVersionButton);
        addVersionButton = new JButton("Add a New Version");
        addVersionButton.setToolTipText("Add a configuration for a new version of REDUCE.");
        addVersionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionsPane.add(addVersionButton);

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
        versionNameTextField = new JTextField(textFieldColumns);
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
        versionRootDirTextField = new JTextField(textFieldColumns);
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

        final JTextArea textArea3 = new JTextArea("$REDUCE below is replaced by 'Version Root Dir' " +
                "if it is set, otherwise by 'Default Root Dir'.");
        textArea3.setBackground(backgroundColor);
        textArea3.setEditable(false);
        textArea3.setLineWrap(true);
        textArea3.setWrapStyleWord(true);
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
        commandPathNameTextField = new JTextField(textFieldColumns);
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
            args[i] = new JTextField(textFieldColumns);
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

    public void showDialog() {
        reduceConfigData = new REDUCEConfigData(RunREDUCE.reduceConfiguration);
        updateDialog(reduceConfigData);
        pack(); // must be here!
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
        listModel = new DefaultListModel<>();
        versionsJList.setModel(listModel);
        try {
            for (REDUCECommandDocuments reduceCommandDocuments : reduceCommandDocumentsList)
                listModel.addElement(reduceCommandDocuments.version.getText());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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
        PlainDocument[] command = cmd.command;
        commandPathNameTextField.setDocument(command[0]);
        for (int i = 0; i < nArgs; i++) args[i].setDocument(command[i + 1]);
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
            listModel.remove(selectedIndex);
            reduceCommandDocumentsList.remove(selectedIndex);
            int size = listModel.size(); // new size!
            if (size > 0) {
                if (selectedIndex >= size) selectedIndex = 0;
                versionsJList.setSelectedIndex(selectedIndex);
                showREDUCECommand(reduceCommandDocumentsList.get(selectedIndex));
            } else
                addVersion();
        }
    }

    private void addVersion() {
        listModel.addElement("NEW VERSION");
        versionsJList.setSelectedIndex(listModel.size() - 1);
        REDUCECommandDocuments reduceCommandDocuments = new REDUCECommandDocuments("NEW VERSION");
        reduceCommandDocumentsList.add(reduceCommandDocuments);
        showREDUCECommand(reduceCommandDocuments);
    }
}

class PlainDocument extends javax.swing.text.PlainDocument {
    String getText() throws BadLocationException {
        return getText(0, getLength());
    }

    void insertString(String str) throws BadLocationException {
        insertString(0, str, null);
    }

//    void replace(String text) throws BadLocationException {
//        replace(0, getLength(), text, null);
//    }
}

class REDUCECommandDocuments {
    PlainDocument version;
    PlainDocument versionRootDir;
    PlainDocument[] command;

    REDUCECommandDocuments(String version) {
        this(version, "", "");
    }

    REDUCECommandDocuments(String version, String versionRootDir, String... command) {
        try {
            this.version = new PlainDocument();
            this.version.insertString(version);
            this.version.addDocumentListener(new VersionDocumentListener());
            this.versionRootDir = new PlainDocument();
            this.versionRootDir.insertString(versionRootDir);
            this.command = new PlainDocument[REDUCEConfigDialog.nArgs + 1];
            int i;
            for (i = 0; i < command.length; i++) {
                this.command[i] = new PlainDocument();
                this.command[i].insertString(command[i]);
            }
            for (; i <= REDUCEConfigDialog.nArgs; i++) {
                this.command[i] = new PlainDocument();
                this.command[i].insertString("");
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}

class VersionDocumentListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) {
        updateVersionList(e);
    }
    public void removeUpdate(DocumentEvent e) {
        updateVersionList(e);
    }
    public void changedUpdate(DocumentEvent e) {
        updateVersionList(e);
    }
    private void updateVersionList(DocumentEvent e) {
        // This seems a bit ugly, but it also seems to work!
        try {
            String s = ((PlainDocument) e.getDocument()).getText();
            int selectedIndex = REDUCEConfigDialog.versionsJList.getSelectedIndex();
            REDUCEConfigDialog.listModel.set(selectedIndex, s);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }
}

class REDUCECommandDocumentsList extends ArrayList<REDUCECommandDocuments> {
    REDUCECommandDocumentsList(REDUCEConfigurationType reduceConfiguration) {
        for (RunREDUCECommand cmd : reduceConfiguration.runREDUCECommandList)
            add(new REDUCECommandDocuments(cmd.version, cmd.versionRootDir, cmd.command));
    }
}

/*
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
        try {
            RunREDUCE.reduceConfiguration.reduceRootDir = reduceRootDir.getText().trim();
            RunREDUCE.reduceConfiguration.packagesRootDir = packagesRootDir.getText().trim();
            RunREDUCE.reduceConfiguration.runREDUCECommandList = new RunREDUCECommandList();
            for (REDUCECommandDocuments cmd : reduceCommandDocumentsList) {
                // Do not save blank arguments:
                ArrayList<String> commandList = new ArrayList<>();
                for (int i = 0; i < cmd.command.length; i++) {
                    String s = cmd.command[i].getText().trim();
                    if (!s.isEmpty()) commandList.add(s);
                }
                RunREDUCE.reduceConfiguration.runREDUCECommandList.add(new RunREDUCECommand(
                        cmd.version.getText().trim(),
                        cmd.versionRootDir.getText().trim(),
                        commandList.toArray(new String[0])));
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}

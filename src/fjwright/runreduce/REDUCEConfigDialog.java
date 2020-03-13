package fjwright.runreduce;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;

public class REDUCEConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton resetAllDefaultsButton;
    private JTextField defaultRootDirTextField;
    private JTextField packagesRootDirTextField;
    private JList versionsJList;
    private JButton deleteVersionButton;
    private JButton addVersionButton;
    private JTextField versionNameTextField;
    private JTextField specificRootDirTextField;
    private JTextField commandPathNameTextField;

    public REDUCEConfigDialog(Frame frame) {
        super(frame, "Configure REDUCE Directories and Commands", true);
        createUIComponents();
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

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
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void showDialog() {
        // TODO Populate the fields here!!!
        this.pack();
        this.setVisible(true);
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

        // buttonPane contains the OK and Cancel buttons.
        final JPanel buttonPane = new JPanel();
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        buttonOK = new JButton("OK");
        buttonPane.add(buttonOK);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonCancel = new JButton("Cancel");
        buttonPane.add(buttonCancel);

        // mainPane contains everything above the OK and Cancel buttons:
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

        final JTextArea textArea1 = new JTextArea("If 'Default Root Dir' is empty, " +
                "it is replaced by the value of the environment variable named REDUCE if it is set, " +
                "otherwise on Windows Run-REDUCE searches for a standard installation folder.");
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
        versionsJList = new JList();
        versionsJList.setEnabled(true);
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

        final JLabel specificRootDirLabel = new JLabel("Specific Root Dir");
        specificRootDirLabel.setLabelFor(specificRootDirTextField);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        commandPane.add(specificRootDirLabel, gbc);
        specificRootDirTextField = new JTextField(textFieldColumns);
        specificRootDirTextField.setToolTipText("As an optional convenience, " +
                "specify a root directory path that can be referenced as $REDUCE in the command path name and arguments below.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        commandPane.add(specificRootDirTextField, gbc);

        final JTextArea textArea3 = new JTextArea("$REDUCE below is replaced by 'Specific Root Dir' " +
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
        commandPathNameTextField.setToolTipText("The file name part of the command to run REDUCE, which can optionally begin with $REDUCE.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        commandPane.add(commandPathNameTextField, gbc);

        int nArgs = 3;
        final JLabel[] argLabels = new JLabel[nArgs];
        final JTextField[] args = new JTextField[nArgs];
        for (int i = 0; i < nArgs; i++) {
            argLabels[i] = new JLabel("Argument " + (i+1));
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
}

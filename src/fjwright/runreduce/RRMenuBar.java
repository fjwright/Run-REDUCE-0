package fjwright.runreduce;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides the RunREDUCE menu bar.
 * <p>
 * Note that the current directory for REDUCE is the directory from
 * which this GUI was run, so filenames must be relative to that
 * directory or absolute; I currently use the latter.
 */
class RRMenuBar extends JMenuBar {
    private static Frame frame = null;

    static final JMenuItem inputFileMenuItem = new JMenuItem("Input from Files...");
    static final JMenuItem outputFileMenuItem = new JMenuItem("Output to File...");
    static final JMenuItem outputHereMenuItem = new JMenuItem("Output Here");
    static final JMenuItem shutFileMenuItem = new JMenuItem("Shut Output Files...");
    static final JMenuItem shutLastMenuItem = new JMenuItem("Shut Last Output File");
    static final JMenuItem loadPackagesMenuItem = new JMenuItem("Load Packages...");
    static final JMenu runREDUCESubmenu = new JMenu("Run REDUCE...  ");
    static final JMenu autoRunREDUCESubmenu = new JMenu("Auto-run REDUCE...  ");
    static final JMenuItem stopREDUCEMenuItem = new JMenuItem("Stop REDUCE");

    static final JFileChooser fileChooser = new JFileChooser();
    static final FileNameExtensionFilter inputFileFilter =
            new FileNameExtensionFilter("REDUCE Input Files (*.red, *.tst, *.txt)", "red", "tst", "txt");
    static final FileNameExtensionFilter outputFileFilter =
            new FileNameExtensionFilter("REDUCE Output Files (*.rlg, *.txt)", "rlg", "txt");
    static final JCheckBox echoCheckBox = new JCheckBox("Echo");
    static final JCheckBox appendCheckBox = new JCheckBox("Append");
    static ShutOutputFilesDialog shutOutputFilesDialog;
    static final List<File> outputFileList = new ArrayList<>();
    static LoadPackagesDialog loadPackagesDialog;
    static List<String> packageList;
    static FontSizeDialog fontSizeDialog;
    static REDUCEConfigDialog reduceConfigDialog;
    static private boolean runningREDUCE;

    RRMenuBar(JFrame frame) {
        RRMenuBar.frame = frame;
        frame.setJMenuBar(this);
        // menuBar.setOpaque(true);

        /* ************* *
         * The File menu *
         * ************* */
        JMenu fileMenu = new JMenu("File");
        this.add(fileMenu);

        // Input from one or more files with echo control.
        fileMenu.add(inputFileMenuItem);
        inputFileMenuItem.setToolTipText
                ("Select and input from one or more REDUCE source files.");
        inputFileMenuItem.addActionListener(e -> {
            fileChooser.setDialogTitle("Input from Files...");
            fileChooser.resetChoosableFileFilters();
            fileChooser.setFileFilter(inputFileFilter);
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setApproveButtonText("Input");
            fileChooser.setApproveButtonToolTipText("Input from selected files");
            // Add a Packages button and Echo CheckBox:
            Box accessoryBox = Box.createVerticalBox();
            accessoryBox.add(Box.createVerticalGlue());
            JButton packagesDirButton = new JButton("Packages");
            packagesDirButton.setMargin(new Insets(0, 0, 0, 0));
            packagesDirButton.setToolTipText("Go to the REDUCE packages directory.");
            File packagesDir = new File(RunREDUCE.reduceConfiguration.packagesRootDir, "packages");
            packagesDirButton.setEnabled(packagesDir.exists());
            packagesDirButton.addActionListener(e1 -> fileChooser.setCurrentDirectory(packagesDir));
            accessoryBox.add(packagesDirButton);
            accessoryBox.add(Box.createVerticalStrut(10));
            accessoryBox.add(echoCheckBox);
            echoCheckBox.setSelected(true);
            packagesDirButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            echoCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
            accessoryBox.add(Box.createVerticalGlue());
            fileChooser.setAccessory(accessoryBox);
            // Process the return value:
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                StringBuilder text = new StringBuilder("in \"");
                text.append(files[0].toString());
                for (int i = 1; i < files.length; i++) {
                    text.append("\", \"");
                    text.append(files[i].toString());
                }
                text.append(echoCheckBox.isSelected() ? "\";\n" : "\"$\n");
                RunREDUCE.reducePanel.sendStringToREDUCE(text.toString());
            }
        });

        // Output to a file.
        // ToDo Output to a new/existing/previous output file
        fileMenu.add(outputFileMenuItem);
        outputFileMenuItem.setToolTipText
                ("Select and output to a text file. Append if the file is already open.");
        outputFileMenuItem.addActionListener(e -> {
            fileChooser.setDialogTitle("Output to File...");
            fileChooser.resetChoosableFileFilters();
            fileChooser.setFileFilter(outputFileFilter);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setAccessory(null);
            fileChooser.setApproveButtonText("Output");
            fileChooser.setApproveButtonToolTipText("Output to selected file");
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                RunREDUCE.reducePanel.sendStringToREDUCE("out \"" + file.toString() + "\"$\n");
                outputFileList.remove(file); // in case it was already open
                outputFileList.add(file);
                shutLastMenuItem.setEnabled(true);
                shutFileMenuItem.setEnabled(true);
                outputHereMenuItem.setEnabled(true);
            }
        });

        // Output to this GUI.
        fileMenu.add(outputHereMenuItem);
        outputHereMenuItem.setEnabled(false);
        outputHereMenuItem.setToolTipText("Switch output back to this GUI.");
        outputHereMenuItem.addActionListener(e -> {
            RunREDUCE.reducePanel.sendStringToREDUCE("out t$\n");
            outputHereMenuItem.setEnabled(false);
        });

        // Shut one or more output files.
        fileMenu.add(shutFileMenuItem);
        shutFileMenuItem.setEnabled(false);
        shutFileMenuItem.setToolTipText("Select and shut one or more output files.");
        shutFileMenuItem.addActionListener(e -> {
            if (shutOutputFilesDialog == null)
                shutOutputFilesDialog = new ShutOutputFilesDialog(frame);
            if (!outputFileList.isEmpty()) { // not strictly necessary
                // Select output files to shut:
                int[] fileIndices = shutOutputFilesDialog.showDialog();
                int length = fileIndices.length;
                if (length != 0) {
                    // Process backwards to avoid remove() changing subsequent indices:
                    StringBuilder text = new StringBuilder(outputFileList.remove(fileIndices[--length]).toString());
                    text.append("\"$\n");
                    for (int i = --length; i >= 0; i--) {
                        text.insert(0, "\", \"");
                        text.insert(0, outputFileList.remove(fileIndices[i]).toString());
                    }
                    text.insert(0, "shut \"");
                    RunREDUCE.reducePanel.sendStringToREDUCE(text.toString());
                }
            }
            if (outputFileList.isEmpty()) {
                shutLastMenuItem.setEnabled(false);
                shutFileMenuItem.setEnabled(false);
                outputHereMenuItem.setEnabled(false);
            }
        });

        // Shut the last output file used.
        fileMenu.add(shutLastMenuItem);
        shutLastMenuItem.setEnabled(false);
        shutLastMenuItem.setToolTipText("Shut the last output file used.");
        shutLastMenuItem.addActionListener(e -> {
            if (!outputFileList.isEmpty()) { // not strictly necessary
                int last = outputFileList.size() - 1;
                RunREDUCE.reducePanel.sendStringToREDUCE("shut \"" + outputFileList.remove(last).toString() + "\"$\n");
            }
            if (outputFileList.isEmpty()) {
                shutLastMenuItem.setEnabled(false);
                shutFileMenuItem.setEnabled(false);
                outputHereMenuItem.setEnabled(false);
            }
        });

        fileMenu.addSeparator();

        // Load packages.
        fileMenu.add(loadPackagesMenuItem);
        loadPackagesMenuItem.setToolTipText("Select and load one or more packages.");
        loadPackagesMenuItem.addActionListener(e -> {
            if (loadPackagesDialog == null) loadPackagesDialog = new LoadPackagesDialog(frame);
            if (packageList == null) packageList = new REDUCEPackageList();
            if (packageList.isEmpty()) {
                // Allow the user to correct the packages directory and try again:
                packageList = null;
                return;
            }
            // Select packages to load:
            List<String> selectedPackages = loadPackagesDialog.showDialog(packageList);
            if (!selectedPackages.isEmpty()) {
                StringBuilder text = new StringBuilder("load_package ");
                text.append(selectedPackages.get(0));
                for (int i = 1; i < selectedPackages.size(); i++) {
                    text.append(", ");
                    text.append(selectedPackages.get(i));
                }
                text.append(";\n");
                RunREDUCE.reducePanel.sendStringToREDUCE(text.toString());
            }
        });

        // Save the display log to file.
        JMenuItem saveLogMenuItem = new JMenuItem("Save Session Log...");
        fileMenu.add(saveLogMenuItem);
        saveLogMenuItem.setToolTipText("Save the full session log to the selected text file.");
        saveLogMenuItem.addActionListener(e -> {
            fileChooser.setDialogTitle("Save Session Log...");
            fileChooser.resetChoosableFileFilters();
            fileChooser.setFileFilter(outputFileFilter);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setAccessory(appendCheckBox);
            fileChooser.setApproveButtonText("Save");
            fileChooser.setApproveButtonToolTipText("Save to selected file");
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (Writer out = new BufferedWriter
                        (new FileWriter(file, appendCheckBox.isSelected()))) {
                    RunREDUCE.reducePanel.outputTextPane.write(out);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        // Exit:
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);
        exitMenuItem.setToolTipText("Terminate REDUCE and exit this GUI.");
        exitMenuItem.addActionListener(e -> System.exit(0));


        /* *************** *
         * The REDUCE menu *
         * *************** */
        JMenu reduceMenu = new JMenu("REDUCE");
        this.add(reduceMenu);

        // Create a menu to run the selected version of REDUCE:
        // Allow space in the title string for the submenu indicator.
        reduceMenu.add(runREDUCESubmenu);
        runREDUCESubmenu.setToolTipText("Select a version of REDUCE and run it.");
        runREDUCESubmenuBuild();

        // Create a menu to select the version of REDUCE to auto-run:
        // Allow space in the title string for the submenu indicator.
        reduceMenu.add(autoRunREDUCESubmenu);
        autoRunREDUCESubmenu.setToolTipText(
                "Select a version of REDUCE to auto-run and run it now if REDUCE is not already running.");
        autoRunREDUCESubmenuBuild();

        reduceMenu.add(stopREDUCEMenuItem);
        stopREDUCEMenuItem.setToolTipText("Terminate REDUCE but *not* this GUI.");
        stopREDUCEMenuItem.addActionListener(e -> {
            RunREDUCE.reducePanel.sendStringToREDUCE("bye;\n");
            outputFileList.clear();
            // Reset enabled state of menu items etc.:
            whenREDUCERunning(false);
        });

        JMenuItem clearDisplayMenuItem = new JMenuItem("Clear I/O Display");
        reduceMenu.add(clearDisplayMenuItem);
        clearDisplayMenuItem.setToolTipText("Clear the REDUCE Input/Output Display.");
        clearDisplayMenuItem.addActionListener(e -> {
            StyledDocument styledDoc = RunREDUCE.reducePanel.outputTextPane.getStyledDocument();
            try {
                styledDoc.remove(0, styledDoc.getLength());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });

        reduceMenu.addSeparator();

        // Configure REDUCE.
        JMenuItem configureREDUCEMenuItem = new JMenuItem("Configure REDUCE...");
        reduceMenu.add(configureREDUCEMenuItem);
        configureREDUCEMenuItem.setToolTipText("Configure REDUCE directories and commands.");
        configureREDUCEMenuItem.addActionListener(e -> showREDUCEConfigDialog());


        /* ************* *
         * The View menu *
         * ************* */
        JMenu viewMenu = new JMenu("View");
        this.add(viewMenu);

        // Create a Font Size... item in the View menu that pops up a dialogue:
        JMenuItem fontSizeMenuItem = new JMenuItem("Font Size...");
        viewMenu.add(fontSizeMenuItem);
        fontSizeMenuItem.setToolTipText("Change the font size used for REDUCE input and output.");
        fontSizeMenuItem.addActionListener(e -> {
            if (fontSizeDialog == null) fontSizeDialog = new FontSizeDialog(frame);
            fontSizeDialog.showDialog();
        });

        JCheckBoxMenuItem boldPromptsCheckBox = new JCheckBoxMenuItem("Bold Prompts");
        viewMenu.add(boldPromptsCheckBox);
        boldPromptsCheckBox.setToolTipText("Make input prompts bold (independently of I/O colouring).");
        boldPromptsCheckBox.setState(RRPreferences.boldPromptsState);
        applyBoldPromptsState();
        boldPromptsCheckBox.addItemListener(e -> {
            RRPreferences.boldPromptsState = boldPromptsCheckBox.isSelected();
            RRPreferences.save(RRPreferences.BOLDPROMPTS);
            applyBoldPromptsState();
        });

        JMenu colouredIOSubMenu = new JMenu("I/O Colouring");
        viewMenu.add(colouredIOSubMenu);
        colouredIOSubMenu.setToolTipText("Select a text colouring style for the I/O Display, or none.");
        ButtonGroup colouredIOButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem noColouredIORadioButton = new JRadioButtonMenuItem("None");
        colouredIOSubMenu.add(noColouredIORadioButton);
        colouredIOButtonGroup.add(noColouredIORadioButton);
        noColouredIORadioButton.setToolTipText("No text colouring.");
        noColouredIORadioButton.setSelected(RRPreferences.colouredIOIntent == RRPreferences.ColouredIO.NONE);
        noColouredIORadioButton.addActionListener(e ->
                RRPreferences.save(RRPreferences.COLOUREDIO, RRPreferences.ColouredIO.NONE));
        JRadioButtonMenuItem modeColouredIORadioButton = new JRadioButtonMenuItem("Modal");
        colouredIOSubMenu.add(modeColouredIORadioButton);
        colouredIOButtonGroup.add(modeColouredIORadioButton);
        modeColouredIORadioButton.setToolTipText(
                "Colour prompts, input and output to indicate algebraic or symbolic mode.");
        modeColouredIORadioButton.setSelected(RRPreferences.colouredIOIntent == RRPreferences.ColouredIO.MODAL);
        modeColouredIORadioButton.addActionListener(e ->
                RRPreferences.save(RRPreferences.COLOUREDIO, RRPreferences.ColouredIO.MODAL));
        JRadioButtonMenuItem redfrontColouredIORadioButton = new JRadioButtonMenuItem("Redfront");
        colouredIOSubMenu.add(redfrontColouredIORadioButton);
        colouredIOButtonGroup.add(redfrontColouredIORadioButton);
        redfrontColouredIORadioButton.setToolTipText("Full redfront emulation loading the redfront package.");
        redfrontColouredIORadioButton.setSelected(RRPreferences.colouredIOIntent == RRPreferences.ColouredIO.REDFRONT);
        redfrontColouredIORadioButton.addActionListener(e ->
                RRPreferences.save(RRPreferences.COLOUREDIO, RRPreferences.ColouredIO.REDFRONT));


        /* ************* *
         * The Help menu *
         * ************* */
        JMenu helpMenu = new JMenu("Help");
        this.add(helpMenu);

        String[][] manuals = {
                {"REDUCE Manual (HTML)", "lib/csl/reduce.doc/manual.html", "manual.html"},
                {"REDUCE Manual (PDF)", "lib/csl/reduce.doc/manual.pdf", "manual.pdf.gz"},
                {"Inside Reduce (PDF)", "doc/insidereduce.pdf", "insidereduce.pdf.gz"},
                {"REDUCE Symbolic Mode Primer (PDF)", "doc/primer.pdf", "primer.pdf.gz"},
                {"Standard Lisp Report (PDF)", "doc/sl.pdf", "sl.pdf.gz"}
        };
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            for (String[] manual : manuals) {
                JMenuItem menuItem = new JMenuItem(manual[0]);
                helpMenu.add(menuItem);
                menuItem.setToolTipText("Open this manual in the default application.");
                menuItem.addActionListener(e ->
                {
                    try {
                        Desktop.getDesktop().open(RRPreferences.windowsOS ?
                                // ToDo Make the directory used below configurable?
                                new File(RunREDUCE.reduceConfiguration.packagesRootDir, manual[1]) :
                                new File("/usr/share/doc/reduce", manual[2]));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }

        helpMenu.addSeparator();

        // Create an About Run-REDUCE item in the Help menu that pops up a dialogue:
        JMenuItem aboutMenuItem = new JMenuItem("About Run-REDUCE");
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.setToolTipText("Information about this application.");
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog
                (frame,
                        new String[]{"Run CLI REDUCE in a Java Swing GUI.",
                                "Prototype version 0.8",
                                "\u00A9 Francis Wright, April 2020"},
                        "About Run-REDUCE",
                        JOptionPane.PLAIN_MESSAGE));

        // Initialise enabled state of menu items etc.:
        whenREDUCERunning(false);
    }

    static void runREDUCESubmenuBuild() {
        runREDUCESubmenu.removeAll();
        for (REDUCECommand cmd : RunREDUCE.reduceConfiguration.reduceCommandList) {
            JMenuItem item = new JMenuItem(cmd.version);
            runREDUCESubmenu.add(item);
            item.addActionListener(e -> {
                // Run REDUCE.  (A direct call hangs the GUI!)
                SwingUtilities.invokeLater(cmd::run);
            });
        }
    }

    static void autoRunREDUCESubmenuBuild() {
        autoRunREDUCESubmenu.removeAll();
        ButtonGroup autoRunButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem noAutoRunRadioButton = new JRadioButtonMenuItem(RRPreferences.NONE);
        autoRunREDUCESubmenu.add(noAutoRunRadioButton);
        autoRunButtonGroup.add(noAutoRunRadioButton);
        if (RRPreferences.autoRunVersion.equals(RRPreferences.NONE)) noAutoRunRadioButton.setSelected(true);
        noAutoRunRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                RRPreferences.save(RRPreferences.AUTORUNVERSION, RRPreferences.NONE);
        });
        for (REDUCECommand cmd : RunREDUCE.reduceConfiguration.reduceCommandList) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(cmd.version);
            if (RRPreferences.autoRunVersion.equals(cmd.version)) item.setSelected(true);
            autoRunREDUCESubmenu.add(item);
            autoRunButtonGroup.add(item);
            item.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String version = ((JRadioButtonMenuItem) e.getItem()).getText();
                    RRPreferences.save(RRPreferences.AUTORUNVERSION, version);
                    if (!runningREDUCE) {
                        for (REDUCECommand cmd1 : RunREDUCE.reduceConfiguration.reduceCommandList) {
                            if (version.equals(cmd1.version)) SwingUtilities.invokeLater(cmd1::run);
                            break;
                        }
                    }
                }
            });
        }
    }

    static void showREDUCEConfigDialog() {
        if (reduceConfigDialog == null) reduceConfigDialog = new REDUCEConfigDialog(frame);
        reduceConfigDialog.showDialog();
    }

    static void applyBoldPromptsState() {
        StyleConstants.setBold(REDUCEOutputThread.promptAttributeSet, RRPreferences.boldPromptsState);
        StyleConstants.setBold(REDUCEOutputThread.algebraicPromptAttributeSet, RRPreferences.boldPromptsState);
        StyleConstants.setBold(REDUCEOutputThread.symbolicPromptAttributeSet, RRPreferences.boldPromptsState);
    }

    /**
     * Enable or disable menu items, buttons, etc., depending on whether REDUCE is running.
     */
    static void whenREDUCERunning(boolean running) {
        runningREDUCE = running;
        // Items to enable when REDUCE is running:
        inputFileMenuItem.setEnabled(running);
        outputFileMenuItem.setEnabled(running);
        loadPackagesMenuItem.setEnabled(running);
        stopREDUCEMenuItem.setEnabled(running);
        if (RunREDUCE.reducePanel != null)
            RunREDUCE.reducePanel.sendAction.setEnabled(running);

        // Items to disable when REDUCE is running:
        runREDUCESubmenu.setEnabled(!running);

        // Items to disable always:
        outputHereMenuItem.setEnabled(false);
        shutFileMenuItem.setEnabled(false);
        shutLastMenuItem.setEnabled(false);
    }
}

package fjwright.runreduce;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.StyleConstants;
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
class RunREDUCEMenubar extends JMenuBar {
    private static Frame frame = null;
    static final JMenuItem closeFileMenuItem = new JMenuItem("Shut Output Files...");
    static final JMenuItem closeLastMenuItem = new JMenuItem("Shut Last Output File");
    static final JMenuItem loadPackagesMenuItem = new JMenuItem("Load Packages...");
    static final JMenu runREDUCESubmenu = new JMenu("Run REDUCE...  ");
    static final JMenu autoRunREDUCESubmenu = new JMenu("Auto-run REDUCE...  ");

    static final JFileChooser fileChooser = new JFileChooser();
    static final FileNameExtensionFilter inputFileFilter =
            new FileNameExtensionFilter("REDUCE Input Files (*.red, *.tst, *.txt)", "red", "tst", "txt");
    static final FileNameExtensionFilter outputFileFilter =
            new FileNameExtensionFilter("REDUCE Output Files (*.rlg, *.txt)", "rlg", "txt");
    static final JCheckBox echoButton = new JCheckBox("Echo");
    static final JCheckBox appendButton = new JCheckBox("Append");
    static ShutOutputFilesDialog shutOutputFilesDialog;
    static final List<File> outputFileList = new ArrayList<>();
    static LoadPackagesDialog loadPackagesDialog;
    static List<String> packageList;
    static FontSizeDialog fontSizeDialog;
    static REDUCEConfigDialog reduceConfigDialog;

    RunREDUCEMenubar(JFrame frame) {
        this.frame = frame;
        frame.setJMenuBar(this);
        // menuBar.setOpaque(true);

        /* ************* *
         * The File menu *
         * ************* */
        JMenu fileMenu = new JMenu("File");
        this.add(fileMenu);

        // Input from one or more files with echo control.
        JMenuItem inputFileMenuItem = new JMenuItem("Input from Files...");
        fileMenu.add(inputFileMenuItem);
        inputFileMenuItem.setToolTipText
                ("Select and input from one or more REDUCE source files.");
        inputFileMenuItem.addActionListener(e -> {
            fileChooser.setDialogTitle("Input from Files...");
            fileChooser.resetChoosableFileFilters();
            fileChooser.setFileFilter(inputFileFilter);
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setAccessory(echoButton);
            fileChooser.setApproveButtonText("Input");
            fileChooser.setApproveButtonToolTipText("Input from selected files");
            echoButton.setSelected(true);
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                String text = "in \"" + files[0].toString();
                for (int i = 1; i < files.length; i++)
                    text += "\", \"" + files[i].toString();
                RunREDUCE.sendStringToREDUCE(text + (echoButton.isSelected() ? "\";" : "\"$"));
            }
        });

        // Output to a file.
        JMenuItem outputFileMenuItem = new JMenuItem("Output to File...");
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
                RunREDUCE.sendStringToREDUCE
                        ("out \"" + file.toString() + "\"$");
                outputFileList.add(file);
                closeLastMenuItem.setEnabled(true);
                closeFileMenuItem.setEnabled(true);
            }
        });

        // Output to this GUI.
        JMenuItem outputHereMenuItem = new JMenuItem("Output Here");
        fileMenu.add(outputHereMenuItem);
        outputHereMenuItem.setToolTipText
                ("Switch output back to this GUI.");
        outputHereMenuItem.addActionListener(e -> RunREDUCE.sendStringToREDUCE("out t$"));

        // Shut one or more output files.
        // JMenuItem closeFileMenuItem = new JMenuItem("Shut Output Files...");
        fileMenu.add(closeFileMenuItem);
        closeFileMenuItem.setEnabled(false);
        closeFileMenuItem.setToolTipText
                ("Select and shut one or more output files.");
        closeFileMenuItem.addActionListener(e -> {
            if (shutOutputFilesDialog == null)
                shutOutputFilesDialog = new ShutOutputFilesDialog(frame);
            if (!outputFileList.isEmpty()) { // not strictly necessary
                // Select output files to shut:
                int[] fileIndices = shutOutputFilesDialog.showDialog(outputFileList);
                int length = fileIndices.length;
                if (length != 0) {
                    // Process backwards to avoid remove() changing subsequent indices:
                    String text = outputFileList.remove(fileIndices[--length]).toString() + "\"$";
                    for (int i = --length; i >= 0; i--)
                        text = outputFileList.remove(fileIndices[i]).toString() + "\", \"" + text;
                    RunREDUCE.sendStringToREDUCE("shut \"" + text);
                }
            }
            if (outputFileList.isEmpty()) {
                closeLastMenuItem.setEnabled(false);
                closeFileMenuItem.setEnabled(false);
            }
        });

        // Shut the last output file used.
        // JMenuItem closeLastMenuItem = new JMenuItem("Shut Last Output File");
        fileMenu.add(closeLastMenuItem);
        closeLastMenuItem.setEnabled(false);
        closeLastMenuItem.setToolTipText
                ("Shut the last output file used.");
        closeLastMenuItem.addActionListener(e -> {
            if (!outputFileList.isEmpty()) { // not strictly necessary
                int last = outputFileList.size() - 1;
                RunREDUCE.sendStringToREDUCE("shut \"" + outputFileList.remove(last).toString() + "\"$");
            }
            if (outputFileList.isEmpty()) {
                closeLastMenuItem.setEnabled(false);
                closeFileMenuItem.setEnabled(false);
            }
        });

        fileMenu.addSeparator();

        // Load packages.
        // JMenuItem loadPackagesMenuItem = new JMenuItem("Load Packages...");
        fileMenu.add(loadPackagesMenuItem);
        loadPackagesMenuItem.setToolTipText
                ("Select and load one or more packages.");
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
                String text = "load_package " + selectedPackages.get(0);
                for (int i = 1; i < selectedPackages.size(); i++)
                    text += ", " + selectedPackages.get(i);
                RunREDUCE.sendStringToREDUCE(text + ";");
            }
        });

        // Save the display log to file.
        JMenuItem saveLogMenuItem = new JMenuItem("Save Session Log...");
        fileMenu.add(saveLogMenuItem);
        saveLogMenuItem.setToolTipText
                ("Save the full session log to the selected text file.");
        saveLogMenuItem.addActionListener(e -> {
            fileChooser.setDialogTitle("Save Session Log...");
            fileChooser.resetChoosableFileFilters();
            fileChooser.setFileFilter(outputFileFilter);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setAccessory(appendButton);
            fileChooser.setApproveButtonText("Save");
            fileChooser.setApproveButtonToolTipText("Save to selected file");
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (Writer out = new BufferedWriter
                        (new FileWriter(file, appendButton.isSelected()))) {
                    RunREDUCE.outputTextPane.write(out);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        // Quit:
        JMenuItem quitMenuItem = new JMenuItem("Quit");
        fileMenu.add(quitMenuItem);
        quitMenuItem.setToolTipText("Quit REDUCE and this GUI.");
        quitMenuItem.addActionListener(e -> System.exit(0));


        /* *************** *
         * The REDUCE menu *
         * *************** */
        JMenu reduceMenu = new JMenu("REDUCE");
        this.add(reduceMenu);

        // Create a menu to run the selected version of REDUCE:
        // Allow space in the title string for the submenu indicator.
        // JMenu runREDUCESubmenu = new JMenu("Run REDUCE...  ");
        reduceMenu.add(runREDUCESubmenu);
        runREDUCESubmenu.setEnabled(!RunREDUCEPrefs.autoRunState);
        runREDUCESubmenuBuild();

        JCheckBoxMenuItem autoRun = new JCheckBoxMenuItem("Auto-run REDUCE?");
        reduceMenu.add(autoRun);
        autoRun.setState(RunREDUCEPrefs.autoRunState);
        autoRun.addItemListener(e -> {
            RunREDUCEPrefs.autoRunState = autoRun.isSelected();
            RunREDUCEPrefs.save(RunREDUCEPrefs.AUTORUN);
            runREDUCESubmenu.setEnabled(!RunREDUCEPrefs.autoRunState);
        });

        // Create a menu to select the version of REDUCE to auto-run:
        // Allow space in the title string for the submenu indicator.
        // JMenu autoRunREDUCESubmenu = new JMenu("Auto-run REDUCE...  ");
        reduceMenu.add(autoRunREDUCESubmenu);
        autoRunREDUCESubmenuBuild();

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

        JCheckBoxMenuItem colouredIO = new JCheckBoxMenuItem("Coloured I/O?");
        viewMenu.add(colouredIO);
        colouredIO.setToolTipText("Use redfront-style red input and blue output?");
        setFontColour();
        colouredIO.setState(RunREDUCEPrefs.colouredIOState);
        colouredIO.addItemListener(e -> {
            RunREDUCEPrefs.colouredIOState = colouredIO.isSelected();
            RunREDUCEPrefs.save(RunREDUCEPrefs.COLOUREDIO);
            setFontColour();
        });


        /* ************* *
         * The Help menu *
         * ************* */
        JMenu helpMenu = new JMenu("Help");
        this.add(helpMenu);

        // Create an About Run-REDUCE item in the Help menu that pops up a dialogue:
        JMenuItem aboutMenuItem = new JMenuItem("About Run-REDUCE");
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.setToolTipText("Information about this app.");
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog
                (frame,
                        new String[]{"Run CLI REDUCE in a Java Swing GUI.",
                                "Prototype version 0.4",
                                "Francis Wright, March 2020"},
                        "About Run-REDUCE",
                        JOptionPane.PLAIN_MESSAGE));
    }

    static void setFontColour() {
        StyleConstants.setForeground(RunREDUCE.inputSimpleAttributeSet,
                RunREDUCEPrefs.colouredIOState ? Color.red : Color.black);
        StyleConstants.setForeground(ReduceOutputThread.outputSimpleAttributeSet,
                RunREDUCEPrefs.colouredIOState ? Color.blue : Color.black);
    }

    static void runREDUCESubmenuBuild() {
        runREDUCESubmenu.removeAll();
        for (RunREDUCECommand cmd : RunREDUCE.reduceConfiguration.runREDUCECommandList) {
            JMenuItem item = new JMenuItem(cmd.version);
            runREDUCESubmenu.add(item);
            item.setToolTipText("Select a version of REDUCE and run it.");
            item.addActionListener(e -> {
                // Run REDUCE.  (A direct call hangs the GUI!)
                SwingUtilities.invokeLater(cmd::run);
                RunREDUCEMenubar.runREDUCESubmenu.setEnabled(false);
            });
        }
    }

    static void autoRunREDUCESubmenuBuild() {
        autoRunREDUCESubmenu.removeAll();
        ButtonGroup autoRunButtonGroup = new ButtonGroup();
        for (RunREDUCECommand cmd : RunREDUCE.reduceConfiguration.runREDUCECommandList) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(cmd.version);
            if (RunREDUCEPrefs.autoRunVersion.equals(cmd.version)) item.setSelected(true);
            autoRunREDUCESubmenu.add(item);
            autoRunButtonGroup.add(item);
            item.setToolTipText("Select a version of REDUCE to auto-run.");
            item.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    RunREDUCEPrefs.save(RunREDUCEPrefs.AUTORUNVERSION,
                            ((JRadioButtonMenuItem) e.getItem()).getText());
            });
        }
    }

    static void showREDUCEConfigDialog() {
        if (reduceConfigDialog == null) reduceConfigDialog = new REDUCEConfigDialog(frame);
        reduceConfigDialog.showDialog();
    }
}

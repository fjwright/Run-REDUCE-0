package fjwright.runreduce;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;


/**
 * This class provides the RunREDUCE menu bar.
 *
 * Note that the current directory for REDUCE is the directory from
 * which this GUI was run, so filenames must be relative to that
 * directory or absolute; I currently use the latter.
 */
class RunREDUCEMenubar extends JMenuBar {

    static final JMenuItem closeFileMenuItem = new JMenuItem("Shut Output Files...");
    static final JMenuItem closeLastMenuItem = new JMenuItem("Shut Last Output File");

    static final JFileChooser fileChooser = new JFileChooser();
    static final FileNameExtensionFilter inputFileFilter =
        new FileNameExtensionFilter("REDUCE Input Files (*.red, *.txt)", "red", "txt");
    static final FileNameExtensionFilter outputFileFilter =
        new FileNameExtensionFilter("REDUCE Output Files (*.rlg, *.txt)", "rlg", "txt");
    static final JCheckBox echoButton = new JCheckBox("Echo");
    static final JCheckBox appendButton = new JCheckBox("Append");
    static final List<File> outputFileList = new ArrayList<>();
    static ShutOutputFilesDialog dialog;

    RunREDUCEMenubar(JFrame frame) {
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
        inputFileMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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
                        File files[] = fileChooser.getSelectedFiles();
                        String text = "in \"" + files[0].toString();
                        for (int i = 1; i < files.length; i++) {
                            text += "\", \"" + files[i].toString();
                        }
                        RunREDUCE.sendStringToREDUCE
                            (text + (echoButton.isSelected() ? "\";" : "\"$"));
                    } 
                }
            });

        // Output to a file.
        JMenuItem outputFileMenuItem = new JMenuItem("Output to File...");
        fileMenu.add(outputFileMenuItem);
        outputFileMenuItem.setToolTipText
            ("Select and output to a text file. Append if the file is already open.");
        outputFileMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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
                }
            });

        // Output to this GUI.
        JMenuItem outputHereMenuItem = new JMenuItem("Output Here");
        fileMenu.add(outputHereMenuItem);
        outputHereMenuItem.setToolTipText
            ("Switch output back to this GUI.");
        outputHereMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    RunREDUCE.sendStringToREDUCE("out t$");
                }
            });

        // Shut one or more output files.
        dialog = new ShutOutputFilesDialog(frame);
        // JMenuItem closeFileMenuItem = new JMenuItem("Shut Output Files...");
        fileMenu.add(closeFileMenuItem);
        closeFileMenuItem.setEnabled(false);
        closeFileMenuItem.setToolTipText
            ("Select and shut one or more output files.");
        closeFileMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!outputFileList.isEmpty()) { // not strictly necessary
                        // Select output files to shut:
                        int[] fileIndices = dialog.showDialog(outputFileList);
                        int length = fileIndices.length;
                        if (length != 0) {
                            // Process backwards to avoid remove changing subsequent indices:
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
                }
            });

        // Shut the last output file used.
        // JMenuItem closeLastMenuItem = new JMenuItem("Shut Last Output File");
        fileMenu.add(closeLastMenuItem);
        closeLastMenuItem.setEnabled(false);
        closeLastMenuItem.setToolTipText
            ("Shut the last output file used.");
        closeLastMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!outputFileList.isEmpty()) { // not strictly necessary
                        int last = outputFileList.size() - 1;
                        RunREDUCE.sendStringToREDUCE
                            ("shut \"" + outputFileList.remove(last).toString() + "\"$");
                    }
                    if (outputFileList.isEmpty()) {
                        closeLastMenuItem.setEnabled(false);
                        closeFileMenuItem.setEnabled(false);
                    }
                }
            });

        fileMenu.addSeparator();

        // Save the display log to file.
        JMenuItem saveLogMenuItem = new JMenuItem("Save Session Log...");
        fileMenu.add(saveLogMenuItem);
        saveLogMenuItem.setToolTipText
            ("Save the full session log to the selected text file.");
        saveLogMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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
                            RunREDUCE.outputTextArea.write(out);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            });

        // Quit:
        JMenuItem quitMenuItem = new JMenuItem("Quit");
        fileMenu.add(quitMenuItem);
        quitMenuItem.setToolTipText("Quit REDUCE and this GUI.");
        quitMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

        /* ************* *
         * The Help menu *
         * ************* */
        JMenu helpMenu = new JMenu("Help");
        this.add(helpMenu);

        // Create an About RunREDUCE item in the Help menu that pops up a dialogue:
        JMenuItem aboutMenuItem = new JMenuItem("About RunREDUCE");
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.setToolTipText("Information about this app.");
        aboutMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog
                        (frame,
                         // "Prototype version 0.1\nFrancis Wright, February 2020",
                         new String [] {"Run CLI REDUCE in a Java Swing GUI.",
                                        "Prototype version 0.1",
                                        "Francis Wright, February 2020"},
                         "About RunREDUCE",
                         JOptionPane.PLAIN_MESSAGE);
                }
            });
    }
}

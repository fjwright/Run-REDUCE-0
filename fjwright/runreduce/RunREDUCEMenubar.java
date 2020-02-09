package fjwright.runreduce;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.nio.file.*;

/**
 * This class provides the RunREDUCE menu bar.
 *
 * Note that the current directory for REDUCE is the directory from
 * which this GUI was run, so filenames must be resolved to be
 * relative to that directory or absolute; I currently use the latter.
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
    static File lastOutputFile = null;


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
                    int returnVal = fileChooser.showOpenDialog(frame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        lastOutputFile = fileChooser.getSelectedFile();
                        RunREDUCE.sendStringToREDUCE
                            ("out \"" + lastOutputFile.toString() + "\"$");
                        closeLastMenuItem.setEnabled(true);
                        closeFileMenuItem.setEnabled(true);
                    } 
                }
            });

        // Shut one or more output files.
        // JMenuItem closeFileMenuItem = new JMenuItem("Shut Output Files...");
        fileMenu.add(closeFileMenuItem);
        closeFileMenuItem.setEnabled(false);
        closeFileMenuItem.setToolTipText
            ("Select and close one or more output files.");
        closeFileMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
            });

        fileMenu.addSeparator();

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

        // Shut the last output file used.
        // JMenuItem closeLastMenuItem = new JMenuItem("Shut Last Output File");
        fileMenu.add(closeLastMenuItem);
        closeLastMenuItem.setEnabled(false);
        closeLastMenuItem.setToolTipText
            ("Shut the last output file used.");
        closeLastMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (lastOutputFile != null) // not really necessary!
                        RunREDUCE.sendStringToREDUCE
                            ("shut \"" + lastOutputFile.toString() + "\"$");
                }
            });

        fileMenu.addSeparator();

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
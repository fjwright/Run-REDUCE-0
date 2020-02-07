package fjwright.runreduce;

// import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// import javax.swing.text.*;
// import javax.swing.border.*;


/**
 * This class provides the RunREDUCE menu bar.
 */
class RunREDUCEMenubar extends JMenuBar {

    RunREDUCEMenubar(JFrame frame) {
        frame.setJMenuBar(this);
        // menuBar.setOpaque(true);

        /* ************* *
         * The File menu *
         * ************* */
        JMenu fileMenu = new JMenu("File");
        this.add(fileMenu);

        // Input from one or more files -- should allow multiple files.
        // Also need to provide echo control.
        JMenuItem inputFileMenuItem = new JMenuItem("Input Files...");
        fileMenu.add(inputFileMenuItem);
        inputFileMenuItem.setToolTipText
            ("Select and input from one or more REDUCE source files.");
        inputFileMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
            });

        // Output to a file.
        JMenuItem outputFileMenuItem = new JMenuItem("Output File...");
        fileMenu.add(outputFileMenuItem);
        outputFileMenuItem.setToolTipText
            ("Select and output to a text file. Append if the file is already open.");
        outputFileMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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

        // Close one or more output files.
        JMenuItem closeFileMenuItem = new JMenuItem("Close Files...");
        fileMenu.add(closeFileMenuItem);
        closeFileMenuItem.setToolTipText
            ("Select and close one or more output files.");
        closeFileMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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

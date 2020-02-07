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

        // Create a Quit item in the File menu:
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

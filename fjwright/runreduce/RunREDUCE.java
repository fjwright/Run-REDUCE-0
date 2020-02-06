/*
 * Prototype Java GUI to run REDUCE using Swing.
 * Based on https://docs.oracle.com/javase/tutorial/uiswing/index.html.
 * This file is ../fjwright/runreduce/RunREDUCE.java
 * Compile and run it from the PARENT directory of fjwright:
 * javac fjwright/runreduce/RunREDUCE.java
 * java fjwright.runreduce.RunREDUCE
 * (The above works in a Windows cmd shell.)
 */ 

package fjwright.runreduce;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.io.*;

/* RunREDUCE.java requires no other files. */

public class RunREDUCE extends JPanel implements ActionListener {
    private static JTextArea inputTextArea;
    static JTextArea outputTextArea;
    private final static String newline = "\n";

    public RunREDUCE() {
        super(new BorderLayout()); // JPanel defaults to FlowLayout!

        // Create the non-editable vertically-scrollable output text area:
        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        outputScrollPane.setPreferredSize(new Dimension(640, 480));
        outputScrollPane.setMinimumSize(new Dimension(120, 120));

        // Create the editable vertically-scrollable input text area:
        inputTextArea = new JTextArea();
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane.setPreferredSize(new Dimension(640, 120));
        inputScrollPane.setMinimumSize(new Dimension(120, 30));

        // Create titled borders for the scrollpanes:
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(border, "Input/Output Display");
        titledBorder.setTitlePosition(TitledBorder.ABOVE_TOP);
        outputScrollPane.setBorder(titledBorder);
        titledBorder = BorderFactory.createTitledBorder(border, "Input editor");
        titledBorder.setTitlePosition(TitledBorder.ABOVE_TOP);
        inputScrollPane.setBorder(titledBorder);

        // Create a split pane to contain the output and input scrollpanes:
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              outputScrollPane, inputScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.8);
        add(splitPane, BorderLayout.CENTER);

        // Create a button to input the text entered above:
        JButton inputButton = new JButton("Input");
        add(inputButton, BorderLayout.SOUTH);
        inputButton.setActionCommand("input");
        inputButton.addActionListener(this);
        inputButton.setToolTipText("Send the input above to REDUCE. It is terminated with a newline if necessary.");
    }

    public void actionPerformed(ActionEvent e) {
        if ("input".equals(e.getActionCommand())) {
            // System.out.println("Input button clicked!");
            String text = inputTextArea.getText();
            if (!text.endsWith(newline)) text += newline;
            inputTextArea.setText(null);
            outputTextArea.append(text);
            // Make sure the new text is visible, even if there was a
            // selection in the text area:
            outputTextArea.setCaretPosition
                (outputTextArea.getDocument().getLength());
            // Return the focus to the input text area:
            inputTextArea.requestFocusInWindow(); 

            // Send the input to the REDUCE input pipe:
            RunREDUCEProcess.reduceInputPrintWriter.print(text);
            RunREDUCEProcess.reduceInputPrintWriter.flush(); // Necessary?
        }
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window:
        JFrame frame = new JFrame("RunREDUCE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add content to the window:
        RunREDUCE runREDUCE = new RunREDUCE();
        frame.add(runREDUCE);

        // Create a menu bar:
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        // menuBar.setOpaque(true);

        // Create a Help menu:
        JMenu menu = new JMenu("Help");
        menuBar.add(menu);

        // Create an About menuItem that pops up a dialogue:
        JMenuItem menuItem = new JMenuItem("About");
        menuItem.addActionListener(new ActionListener() {
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
        menu.add(menuItem);

        // Display the window:
        frame.pack();
        // Give the input text area the initial focus:
        inputTextArea.requestFocusInWindow(); 
        frame.setVisible(true);
    }

    public static void main(String... args) {
        // Schedule jobs for the event-dispatching thread.
        // Create and show this application's GUI:
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI();
                }
            });
        // Run REDUCE.  (A direct call hangs the GUI!)
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    RunREDUCEProcess.reduce();
                }
            });
    }
}

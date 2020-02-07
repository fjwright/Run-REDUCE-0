/*
 * Prototype Java Swing GUI to run CLI REDUCE.
 * Based on https://docs.oracle.com/javase/tutorial/uiswing/index.html.
 * This file is ../fjwright/runreduce/RunREDUCE.java
 * It requires also ../fjwright/runreduce/RunREDUCEProcess.java
 * Compile and run the app from the PARENT directory of fjwright:
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

/**
 * This is the main class that runs the whole app.  It also provide
 * the pane that displays REDUCE input and output.
 */
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
            RunREDUCEProcess.reduceInputPrintWriter.flush();
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

        // Create the menu bar and add it to the frame:
        new RunREDUCEMenubar(frame);

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

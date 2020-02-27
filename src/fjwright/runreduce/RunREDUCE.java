/*
 * Prototype Java Swing GUI to run CLI REDUCE.
 * Based on https://docs.oracle.com/javase/tutorial/uiswing/index.html.
 * This file is ../fjwright/runreduce/RunREDUCE.java
 * It requires also ../fjwright/runreduce/RunREDUCE*.java
 * Compile and run the app from the PARENT directory of fjwright:
 * javac fjwright/runreduce/RunREDUCE.java
 * java fjwright.runreduce.RunREDUCE
 * (The above works in a Microsoft Windows cmd shell.)
 */

package fjwright.runreduce;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the main class that runs the whole app.  It also provides the pane that displays REDUCE input and output.
 */
public class RunREDUCE extends JPanel implements ActionListener {
    private static JTextArea inputTextArea;
    static JTextArea outputTextArea;
    private final static String NEWLINE = "\n";
    private final static List<String> inputList = new ArrayList<>();
    private static int inputListIndex = 0;
    private static int maxInputListIndex = 0;

    public RunREDUCE() {
        super(new BorderLayout()); // JPanel defaults to FlowLayout!

        // Create the non-editable vertically-scrollable output text area:
        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        JPanel outputPane = new JPanel(new BorderLayout(0, 3));
        outputPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel outputLabel = new JLabel("Input/Output Display");
        outputPane.add(outputLabel, BorderLayout.PAGE_START);
        outputPane.add(outputScrollPane);

        // Create the editable vertically-scrollable input text area:
        inputTextArea = new JTextArea();
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        JPanel inputPane = new JPanel(new BorderLayout(0, 3));
        inputPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel inputLabel = new JLabel("Input editor");
        inputPane.add(inputLabel, BorderLayout.PAGE_START);
        inputPane.add(inputScrollPane);

        // Create a split pane to contain the output and input scrollpanes:
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputPane, inputPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.8);

        add(splitPane, BorderLayout.CENTER);

        // Buttons to control the input:
        JButton previousButton = new JButton("\u25b2 Previous Input");
        previousButton.setActionCommand("Previous");
        previousButton.addActionListener(this);
        previousButton.setToolTipText("Select the previous input.");
        JButton sendButton = new JButton("Send Input");
        sendButton.setActionCommand("Send");
        sendButton.addActionListener(this);
        sendButton.setToolTipText("Send the input above to REDUCE. It is terminated with a newline if necessary.");
        JButton nextButton = new JButton("\u25bc Next Input");
        nextButton.setActionCommand("Next");
        nextButton.addActionListener(this);
        nextButton.setToolTipText("Select the next input.");

        // Set buttons to all have the same size as the widest:
        Dimension buttonDimension = previousButton.getPreferredSize();
        sendButton.setPreferredSize(buttonDimension);
        nextButton.setPreferredSize(buttonDimension);

        // Lay out the buttons horizontally and uniformly spaced:
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(previousButton);
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(sendButton);
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(nextButton);
        buttonPane.add(Box.createHorizontalGlue());

        add(buttonPane, BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent e) {
        if ("Send".equals(e.getActionCommand())) {
            String text = inputTextArea.getText();
            if (text.length() > 0) {
                inputList.add(text);
                sendStringToREDUCE(text);
                inputTextArea.setText(null);
                inputListIndex = inputList.size();
                maxInputListIndex = inputListIndex - 1;
            }
        } else if ("Previous".equals(e.getActionCommand())) {
            if (inputListIndex > 0)
                inputTextArea.setText(inputList.get(--inputListIndex));
        } else if ("Next".equals(e.getActionCommand())) {
            if (inputListIndex < maxInputListIndex)
                inputTextArea.setText(inputList.get(++inputListIndex));
        }
        // Return the focus to the input text area:
        inputTextArea.requestFocusInWindow();
    }

    static void sendStringToREDUCE(String text) {
        if (!text.endsWith(NEWLINE)) text += NEWLINE;
        outputTextArea.append(text);
        // Make sure the new input text is visible, even if there was
        // a selection in the output text area:
        outputTextArea.setCaretPosition
                (outputTextArea.getDocument().getLength());
        // Send the input to the REDUCE input pipe:
        RunREDUCEProcess.reduceInputPrintWriter.print(text);
        RunREDUCEProcess.reduceInputPrintWriter.flush();
    }

    /**
     * Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window:
        JFrame frame = new JFrame("RunREDUCE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(640, 480)); // (960, 720)???

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
        SwingUtilities.invokeLater(RunREDUCE::createAndShowGUI);
        // Run REDUCE.  (A direct call hangs the GUI!)
        SwingUtilities.invokeLater(RunREDUCEProcess::reduce);
    }
}

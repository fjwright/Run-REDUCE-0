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
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This is the main class that runs the whole application.
 * It also provides the pane that displays REDUCE input and output.
 */
public class RunREDUCE extends JPanel {
    private static JFrame frame;
    static JTextArea inputTextArea;
    static JTextPane outputTextPane;
    static Action sendAction = new SendAction();
    private static Action earlierAction = new EarlierAction();
    private static Action laterAction = new LaterAction();

    // Use the logical Monospaced font for REDUCE I/O:
    static Font reduceFont = new Font(Font.MONOSPACED, Font.PLAIN, RunREDUCEPrefs.fontSize);
    private final static List<String> inputList = new ArrayList<>();
    private static int inputListIndex = 0;
    private static int maxInputListIndex = 0;
    static REDUCEConfigurationDefault reduceConfigurationDefault;
    static REDUCEConfiguration reduceConfiguration;
    private static final Pattern pattern =
            Pattern.compile(".*\\b(?:bye|quit)\\s*[;$]?.*", Pattern.CASE_INSENSITIVE);

    public RunREDUCE() {
        super(new BorderLayout()); // JPanel defaults to FlowLayout!

        // Create the non-editable vertically-scrollable output text area:
        outputTextPane = new JTextPane();
        outputTextPane.setFont(reduceFont);
        outputTextPane.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextPane);
        JPanel outputPane = new JPanel(new BorderLayout(0, 3));
        outputPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel outputLabel = new JLabel("Input/Output Display");
        outputPane.add(outputLabel, BorderLayout.PAGE_START);
        outputPane.add(outputScrollPane);

        // Create the editable vertically-scrollable input text area:
        inputTextArea = new JTextArea();
        inputTextArea.setFont(reduceFont);
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        JPanel inputPane = new JPanel(new BorderLayout(0, 3));
        inputPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel inputLabel = new JLabel("Input Editor");
        inputPane.add(inputLabel, BorderLayout.PAGE_START);
        inputPane.add(inputScrollPane);

        // Create a split pane to contain the output and input scrollpanes:
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputPane, inputPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.8);

        add(splitPane, BorderLayout.CENTER);

        // Buttons to control the input:
        JButton sendButton = new JButton(sendAction);
        JButton earlierButton = new JButton(earlierAction);
        earlierAction.setEnabled(false);
        JButton laterButton = new JButton(laterAction);
        laterAction.setEnabled(false);

        // Set buttons to all have the same size as the widest:
        Dimension buttonDimension = earlierButton.getPreferredSize();
        sendButton.setPreferredSize(buttonDimension);
        laterButton.setPreferredSize(buttonDimension);

        // Lay out the buttons horizontally and uniformly spaced:
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(earlierButton);
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(sendButton);
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(laterButton);
        buttonPane.add(Box.createHorizontalGlue());

        add(buttonPane, BorderLayout.PAGE_END);

        // Add keyboard shortcuts to the input text area:
        InputMap inputMap = inputTextArea.getInputMap(); // WHEN_FOCUSED map
        ActionMap actionMap = inputTextArea.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "Send");
        actionMap.put("Send", sendAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK), "Earlier");
        actionMap.put("Earlier", earlierAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK), "Later");
        actionMap.put("Later", laterAction);
    }

    private static class SendAction extends AbstractAction {
        public SendAction() {
            super("Send Input");
            putValue(SHORT_DESCRIPTION,
                    "Send the input above to REDUCE, adding a semicolon and/or newline if necessary." +
                            " Keyboard Shortcut: Control+Enter");
        }

        public void actionPerformed(ActionEvent e) {
            String text = inputTextArea.getText();
            if (text.length() > 0) {
                inputList.add(text);
                sendStringToREDUCE(text);
                inputTextArea.setText(null);
                inputListIndex = inputList.size();
                maxInputListIndex = inputListIndex - 1;
                earlierAction.setEnabled(true);
                laterAction.setEnabled(false);
                if (pattern.matcher(text).matches())
                    // Reset enabled state of menu items etc.:
                    RunREDUCEMenubar.whenREDUCERunning(false);
                // Return the focus to the input text area:
                inputTextArea.requestFocusInWindow();
            }
        }
    }

    private static class EarlierAction extends AbstractAction {
        public EarlierAction() {
            super("\u25b2 Earlier Input");
            putValue(SHORT_DESCRIPTION, "Select earlier input via this editor." +
                    " Keyboard Shortcut: Control+UpArrow");
        }

        public void actionPerformed(ActionEvent e) {
            if (inputListIndex > 0) {
                inputTextArea.setText(inputList.get(--inputListIndex));
                if (inputListIndex <= maxInputListIndex)
                    laterAction.setEnabled(true);
            }
            if (inputListIndex == 0)
                earlierAction.setEnabled(false);
            // Return the focus to the input text area:
            inputTextArea.requestFocusInWindow();
        }
    }

    private static class LaterAction extends AbstractAction {
        public LaterAction() {
            super("\u25bc Later Input");
            putValue(SHORT_DESCRIPTION, "Select later input via this editor." +
                    " Keyboard Shortcut: Control+DownArrow");
        }

        public void actionPerformed(ActionEvent e) {
            if (inputListIndex < maxInputListIndex) {
                inputTextArea.setText(inputList.get(++inputListIndex));
            } else {
                inputTextArea.setText(null);
                inputListIndex = maxInputListIndex + 1;
            }
            if (inputListIndex > 0) {
                earlierAction.setEnabled(true);
            }
            if (inputListIndex > maxInputListIndex) {
                laterAction.setEnabled(false);
            }
            // Return the focus to the input text area:
            inputTextArea.requestFocusInWindow();
        }
    }

    static void sendStringToREDUCE(String text) {
        // Strip trailing white space and ensure the input end with a terminator:
        int i;
        char c = 0;
//        for (i = text.length() - 1; i > 0 && Character.isWhitespace(c = text.charAt(i)); i--) ;
        i = text.length() - 1;
        while (i > 0 && Character.isWhitespace(c = text.charAt(i)))
            i--;
        text = text.substring(0, i + 1);
        if (c == ';' || c == '$') text += "\n";
        else text += ";\n";

        StyledDocument styledDoc = outputTextPane.getStyledDocument();
        SimpleAttributeSet inputAttributeSet;
        switch (RunREDUCEPrefs.colouredIOState) {
            case RunREDUCEPrefs.MODAL: // mode coloured IO display processing
                inputAttributeSet = ReduceOutputThread.inputAttributeSet;
                break;
            case RunREDUCEPrefs.REDFRONT: // redfront coloured IO display processing
                inputAttributeSet = ReduceOutputThread.algebraicInputAttributeSet;
                break;
            default: // no IO display processing
                inputAttributeSet = null;
        }

        try {
            styledDoc.insertString(styledDoc.getLength(), text, inputAttributeSet);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
        // Make sure the new input text is visible, even if there was
        // a selection in the output text area:
        outputTextPane.setCaretPosition(styledDoc.getLength());

        // Send the input to the REDUCE input pipe:
        if (RunREDUCECommand.reduceInputPrintWriter != null) {
            RunREDUCECommand.reduceInputPrintWriter.print(text);
            RunREDUCECommand.reduceInputPrintWriter.flush();
        }

    }

    /**
     * Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window:
        frame = new JFrame("RunREDUCE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set the main window to 2/3 the linear dimension of the screen:
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setPreferredSize(new Dimension((screenSize.width * 2) / 3, (screenSize.height * 2) / 3));

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

        // Auto-run REDUCE if appropriate:
        if (RunREDUCEPrefs.autoRunState)
            for (RunREDUCECommand cmd : reduceConfiguration.runREDUCECommandList)
                if (RunREDUCEPrefs.autoRunVersion.equals(cmd.version)) {
                    // Run REDUCE.  (A direct call hangs the GUI!)
                    SwingUtilities.invokeLater(cmd::run);
                    break;
                }
    }

    static void errorMessageDialog(Object message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String... args) {
        reduceConfigurationDefault = new REDUCEConfigurationDefault();
        reduceConfiguration = new REDUCEConfiguration();
        // Schedule jobs for the event-dispatching thread.
        // Create and show this application's GUI:
        SwingUtilities.invokeLater(RunREDUCE::createAndShowGUI);
    }
}

package fjwright.runreduce;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class provides the panel that displays REDUCE input and output.
 */
public class REDUCEPanel extends JPanel {
    JTextArea inputTextArea;
    JTextPane outputTextPane;
    final Action sendAction = new SendAction();
    private final Action earlierAction = new EarlierAction();
    private final Action laterAction = new LaterAction();
    private final List<String> inputList = new ArrayList<>();
    private int inputListIndex = 0;
    private int maxInputListIndex = 0;
    private static final Pattern pattern =
            Pattern.compile(".*\\b(?:bye|quit)\\s*[;$]?.*", Pattern.CASE_INSENSITIVE);

    public REDUCEPanel() {
        super(new BorderLayout()); // JPanel defaults to FlowLayout!

        // Create the non-editable vertically-scrollable output text area:
        outputTextPane = new JTextPane();
        outputTextPane.setFont(RunREDUCE.reduceFont);
        outputTextPane.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextPane);
        JPanel outputPane = new JPanel(new BorderLayout(0, 3));
        outputPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel outputLabel = new JLabel("Input/Output Display");
        outputPane.add(outputLabel, BorderLayout.PAGE_START);
        outputPane.add(outputScrollPane);

        // Create the editable vertically-scrollable input text area:
        inputTextArea = new JTextArea();
        inputTextArea.setFont(RunREDUCE.reduceFont);
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
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "Send");
        actionMap.put("Send", sendAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK), "Earlier");
        actionMap.put("Earlier", earlierAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK), "Later");
        actionMap.put("Later", laterAction);

        // Give the input text area the initial focus:
        inputTextArea.requestFocusInWindow();

        // Disable all actions initially:
        sendAction.setEnabled(false);
        earlierAction.setEnabled(false);
        laterAction.setEnabled(false);

        // Auto-run REDUCE if appropriate:
        if (!RRPreferences.autoRunVersion.equals(RRPreferences.NONE))
            for (REDUCECommand cmd : RunREDUCE.reduceConfiguration.reduceCommandList)
                if (RRPreferences.autoRunVersion.equals(cmd.version)) {
                    // Run REDUCE.  (A direct call hangs the GUI!)
                    SwingUtilities.invokeLater(cmd::run);
                    break;
                }

    }

    private class SendAction extends AbstractAction {
        public SendAction() {
            super("Send Input");
            putValue(SHORT_DESCRIPTION,
                    "Send the input above to REDUCE, adding a semicolon and/or newline if necessary." +
                            " Keyboard Shortcut: Control+Enter." +
                            " (Also hold Shift to prevent auto-termination.)");
        }

        public void actionPerformed(ActionEvent e) {
            String text = inputTextArea.getText();
            if (text.length() > 0) {
                inputList.add(text);
                boolean questionPrompt = false;
                try {
                    StyledDocument styledDoc = outputTextPane.getStyledDocument();
                    // The last paragraph element should be the prompt line.
                    int start = styledDoc.getParagraphElement(styledDoc.getLength()).getStartOffset();
                    for (int i = styledDoc.getLength() - 1; i >= start; i--)
                        if (styledDoc.getText(i, 1).equals("?")) {
                            questionPrompt = true;
                            break;
                        }
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                boolean unshifted = (e.getModifiers() & ActionEvent.SHIFT_MASK) == 0;
                // if shifted then do not auto terminate, hence if unshifted then auto terminate:
                sendInteractiveInputToREDUCE(text, !questionPrompt && unshifted);
                inputTextArea.setText(null);
                inputListIndex = inputList.size();
                maxInputListIndex = inputListIndex - 1;
                earlierAction.setEnabled(true);
                laterAction.setEnabled(false);
                if (pattern.matcher(text).matches())
                    // Reset enabled state of menu items etc.:
                    RRMenuBar.whenREDUCERunning(false);
                // Return the focus to the input text area:
                inputTextArea.requestFocusInWindow();
            }
        }
    }

    private class EarlierAction extends AbstractAction {
        public EarlierAction() {
            super("\u25b2 Earlier Input");
            putValue(SHORT_DESCRIPTION, "Select earlier input via this editor." +
                    " Keyboard Shortcut: Control+UpArrow.");
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

    private class LaterAction extends AbstractAction {
        public LaterAction() {
            super("\u25bc Later Input");
            putValue(SHORT_DESCRIPTION, "Select later input via this editor." +
                    " Keyboard Shortcut: Control+DownArrow.");
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

    void sendInteractiveInputToREDUCE(String text, boolean autoTerminate) {
        // Strip trailing white space and if autoTerminate then ensure the input ends with a terminator:
        int i = text.length() - 1;
        char c = 0;
        while (i >= 0 && Character.isWhitespace(c = text.charAt(i))) i--;
        text = text.substring(0, i + 1);
        if (c == ';' || c == '$' || !autoTerminate) text += "\n";
        else text += ";\n";
        sendStringToREDUCE(text);
    }

    void sendStringToREDUCE(String text) {
        StyledDocument styledDoc = outputTextPane.getStyledDocument();
        try {
            styledDoc.insertString(styledDoc.getLength(), text, REDUCEOutputThread.inputAttributeSet);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
        // Make sure the new input text is visible, even if there was
        // a selection in the output text area:
        outputTextPane.setCaretPosition(styledDoc.getLength());

        // Send the input to the REDUCE input pipe:
        if (REDUCECommand.reduceInputPrintWriter != null) {
            REDUCECommand.reduceInputPrintWriter.print(text);
            REDUCECommand.reduceInputPrintWriter.flush();
        }
    }
}

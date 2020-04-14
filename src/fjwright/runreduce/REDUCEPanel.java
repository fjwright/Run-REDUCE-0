package fjwright.runreduce;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This class provides the panel that displays REDUCE input and output.
 * The run method runs REDUCE as a sub-process.
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
    private PrintWriter reduceInputPrintWriter;
    MenuItemStatus menuItemStatus = new MenuItemStatus();
    boolean runningREDUCE;

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
                    SwingUtilities.invokeLater(() -> run(cmd));
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
                if (pattern.matcher(text).matches()) {
                    sendAction.setEnabled(runningREDUCE = false);
                    // Reset enabled status of menu items:
                    RunREDUCE.reducePanel.menuItemStatus.reduceStopped();
                }
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
        sendStringToREDUCEAndEcho(text);
    }

    void sendStringToREDUCEAndEcho(String text) {
        StyledDocument styledDoc = outputTextPane.getStyledDocument();
        try {
            styledDoc.insertString(styledDoc.getLength(), text, REDUCEOutputThread.inputAttributeSet);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
        // Make sure the new input text is visible, even if there was
        // a selection in the output text area:
        outputTextPane.setCaretPosition(styledDoc.getLength());
        sendStringToREDUCENoEcho(text);
    }

    void sendStringToREDUCENoEcho(String text) {
        // Send the input to the REDUCE input pipe:
        if (reduceInputPrintWriter != null) {
            reduceInputPrintWriter.print(text);
            reduceInputPrintWriter.flush();
        }
    }

    /*
     * Run the specified REDUCE command in this REDUCE panel.
     */
    void run(REDUCECommand reduceCommand) {
        String[] command = reduceCommand.buildCommand();
        if (command == null) return;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            // pb.redirectInput(ProcessBuilder.Redirect.INHERIT); // Works!
            Process p = pb.start();

            // Assign the REDUCE input stream to an instance field:
            OutputStreamWriter osr = new OutputStreamWriter(p.getOutputStream());
            reduceInputPrintWriter = new PrintWriter(osr);

            // Start a thread to handle the REDUCE output stream
            // (assigned to a global variable):
            REDUCEOutputThread outputGobbler = new
                    REDUCEOutputThread(p.getInputStream(), outputTextPane);
            outputGobbler.start();

            // Initialise enabled state of menu items etc.:
            menuItemStatus.reduceStarted();
        } catch (Exception exc) {
            RunREDUCE.errorMessageDialog(
                    "Error running REDUCE -- " + exc,
                    "REDUCE Process Error");
        }

        if (RRPreferences.tabbedPaneState) {
            int tabIndex = RunREDUCE.tabbedPane.indexOfComponent(this);
            RunREDUCE.tabbedPane.setTitleAt(tabIndex, reduceCommand.version);
        }

        sendAction.setEnabled(runningREDUCE = true);

        // Special support for Redfront I/O colouring:
        RRPreferences.colouredIOState = RRPreferences.colouredIOIntent;
        if (RRPreferences.colouredIOState == RRPreferences.ColouredIO.REDFRONT) {
            sendStringToREDUCENoEcho("load_package redfront;\n");
            // Tidy up the initial prompt.
            StyledDocument styledDoc = outputTextPane.getStyledDocument();
            try {
                styledDoc.remove(styledDoc.getLength() - 8, 4);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
}

/*
 * Enabled status of menu items depending on whether REDUCE is running in this panel.
 */
class MenuItemStatus {
    boolean inputFileMenuItem;
    boolean outputFileMenuItem;
    boolean loadPackagesMenuItem;
    boolean stopREDUCEMenuItem;
    boolean runREDUCESubmenu;
    boolean outputHereMenuItem;
    boolean shutFileMenuItem;
    boolean shutLastMenuItem;

    /*
     * Initialise MenuItemStatus as appropriate when REDUCE is not running.
     */
    MenuItemStatus() {
        startingOrStoppingREDUCE(false);
    }

    /*
     * Reset MenuItemStatus as appropriate when REDUCE is not running.
     */
    void reduceStopped() {
        startingOrStoppingREDUCE(false);
    }

    /*
     * Reset MenuItemStatus as appropriate when REDUCE has just started.
     */
    void reduceStarted() {
        startingOrStoppingREDUCE(true);
    }

    private void startingOrStoppingREDUCE(boolean starting) {
        // Items to enable/disable when REDUCE starts/stops running:
        RRMenuBar.inputFileMenuItem.setEnabled(inputFileMenuItem = starting);
        RRMenuBar.outputFileMenuItem.setEnabled(outputFileMenuItem = starting);
        RRMenuBar.loadPackagesMenuItem.setEnabled(loadPackagesMenuItem = starting);
        RRMenuBar.stopREDUCEMenuItem.setEnabled(stopREDUCEMenuItem = starting);

        // Items to disable/enable when REDUCE starts/stops running:
        RRMenuBar.runREDUCESubmenu.setEnabled(runREDUCESubmenu = !starting);

        // Items to disable when REDUCE starts/stops running:
        RRMenuBar.outputHereMenuItem.setEnabled(outputHereMenuItem = false);
        RRMenuBar.shutFileMenuItem.setEnabled(shutFileMenuItem = false);
        RRMenuBar.shutLastMenuItem.setEnabled(shutLastMenuItem = false);
    }

    /*
     * Update the enabled status of the menu for this REDUCE panel.
     */
    void updateMenus() {
        RRMenuBar.inputFileMenuItem.setEnabled(inputFileMenuItem);
        RRMenuBar.outputFileMenuItem.setEnabled(outputFileMenuItem);
        RRMenuBar.loadPackagesMenuItem.setEnabled(loadPackagesMenuItem);
        RRMenuBar.stopREDUCEMenuItem.setEnabled(stopREDUCEMenuItem);
        RRMenuBar.runREDUCESubmenu.setEnabled(runREDUCESubmenu);
        RRMenuBar.outputHereMenuItem.setEnabled(outputHereMenuItem);
        RRMenuBar.shutFileMenuItem.setEnabled(shutFileMenuItem);
        RRMenuBar.shutLastMenuItem.setEnabled(shutLastMenuItem);
    }
}

/**
 * This thread reads from the REDUCE output pipe and appends it to the GUI output pane.
 */
class REDUCEOutputThread extends Thread {
    InputStream input;        // REDUCE pipe output (buffered)
    JTextPane outputTextPane; // GUI output pane
    private final StyledDocument styledDoc;
    static final SimpleAttributeSet algebraicPromptAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet symbolicPromptAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet algebraicOutputAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet symbolicOutputAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet algebraicInputAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet symbolicInputAttributeSet = new SimpleAttributeSet();
    static SimpleAttributeSet promptAttributeSet = new SimpleAttributeSet();
    static SimpleAttributeSet inputAttributeSet;
    static SimpleAttributeSet outputAttributeSet;
    private static final Pattern promptPattern = Pattern.compile("\\d+([:*]) ");
    private final StringBuilder text = new StringBuilder(); // Must not be static!

    private static final Color ALGEBRAICOUTPUTCOLOR = Color.blue;
    private static final Color SYMBOLICOUTPUTCOLOR = new Color(0x80_00_80);
    private static final Color ALGEBRAICINPUTCOLOR = Color.red;
    private static final Color SYMBOLICINPUTCOLOR = new Color(0x80_00_00);

    REDUCEOutputThread(InputStream input, JTextPane outputTextPane) {
        this.input = input;
        this.outputTextPane = outputTextPane;
        styledDoc = outputTextPane.getStyledDocument();
        StyleConstants.setForeground(algebraicOutputAttributeSet, ALGEBRAICOUTPUTCOLOR);
        StyleConstants.setForeground(symbolicOutputAttributeSet, SYMBOLICOUTPUTCOLOR);
        StyleConstants.setForeground(algebraicInputAttributeSet, ALGEBRAICINPUTCOLOR);
        StyleConstants.setForeground(symbolicInputAttributeSet, SYMBOLICINPUTCOLOR);
        StyleConstants.setForeground(algebraicPromptAttributeSet, ALGEBRAICINPUTCOLOR);
        StyleConstants.setForeground(symbolicPromptAttributeSet, SYMBOLICINPUTCOLOR);
    }

    public void run() {
        outputAttributeSet = null; // for initial header
        switch (RRPreferences.colouredIOState) {
            case NONE:
                inputAttributeSet = null;
                break;
            case REDFRONT:
                inputAttributeSet = algebraicInputAttributeSet;
                break;
        }
        // Must output characters rather than lines so that prompt appears!
        try (InputStreamReader isr = new InputStreamReader(input);
             BufferedReader br = new BufferedReader(isr)) {
            int c, promptIndex;
            String promptString;
            for (; ; ) {
                if (!br.ready()) {
                    int textLength = text.length();
                    if (textLength > 0) {

                        switch (RRPreferences.colouredIOState) {
                            case NONE:
                            default: // no IO display colouring, but maybe prompt processing
                                if ((RRPreferences.boldPromptsState) &&
                                        (promptIndex = text.lastIndexOf("\n") + 1) < textLength &&
                                        promptPattern.matcher(promptString = text.substring(promptIndex)).matches()) {
                                    styledDoc.insertString(styledDoc.getLength(), text.substring(0, promptIndex), null);
                                    styledDoc.insertString(styledDoc.getLength(), promptString, promptAttributeSet);
                                } else
                                    styledDoc.insertString(styledDoc.getLength(), text.toString(), null);
                                break;

                            case MODAL: // mode coloured IO display processing
                                // Split off the final line, which should consist of the next input prompt:
                                promptIndex = text.lastIndexOf("\n") + 1;
                                Matcher promptMatcher;
                                if (promptIndex < textLength &&
                                        (promptMatcher = promptPattern.matcher(promptString = text.substring(promptIndex))).matches()) {
                                    styledDoc.insertString(styledDoc.getLength(), text.substring(0, promptIndex), outputAttributeSet);
                                    // Only colour output *after* initial REDUCE header.
                                    switch (promptMatcher.group(1)) {
                                        case "*":
                                            promptAttributeSet = symbolicPromptAttributeSet;
                                            inputAttributeSet = symbolicInputAttributeSet;
                                            outputAttributeSet = symbolicOutputAttributeSet;
                                            break;
                                        case ":":
                                        default:
                                            promptAttributeSet = algebraicPromptAttributeSet;
                                            inputAttributeSet = algebraicInputAttributeSet;
                                            outputAttributeSet = algebraicOutputAttributeSet;
                                            break;
                                    }
                                    styledDoc.insertString(styledDoc.getLength(), promptString, promptAttributeSet);
                                } else
                                    styledDoc.insertString(styledDoc.getLength(), text.toString(), outputAttributeSet);
                                break; // end of case RunREDUCEPrefs.MODE

                            case REDFRONT: // redfront coloured IO display processing
                                /*
                                 * The markup output by the redfront package uses ASCII control characters:
                                 * ^A prompt ^B input
                                 * ^C algebraic-mode output ^D
                                 * where ^A = \u0001, etc. ^A/^B and ^C/^D should always be paired.
                                 * Prompts and input are always red, algebraic-mode output is blue,
                                 * but any other output (echoed input or symbolic-mode output) is not coloured.
                                 */
                                // Must process arbitrary chunks of output, which may not contain matched pairs of start and end markers:
                                for (; ; ) {
                                    int algOutputStartMarker = text.indexOf("\u0003");
                                    int algOutputEndMarker = text.indexOf("\u0004");
                                    if (algOutputStartMarker >= 0 && algOutputEndMarker >= 0) {
                                        if (algOutputStartMarker < algOutputEndMarker) {
                                            // TEXT < algOutputStartMarker < TEXT < algOutputEndMarker
                                            styledDoc.insertString(styledDoc.getLength(), text.substring(0, algOutputStartMarker), null);
                                            styledDoc.insertString(styledDoc.getLength(), text.substring(algOutputStartMarker + 1, algOutputEndMarker), algebraicOutputAttributeSet);
                                            outputAttributeSet = null;
                                            text.delete(0, algOutputEndMarker + 1);
                                        } else {
                                            // TEXT < algOutputEndMarker < TEXT < algOutputStartMarker
                                            styledDoc.insertString(styledDoc.getLength(), text.substring(0, algOutputEndMarker), algebraicOutputAttributeSet);
                                            styledDoc.insertString(styledDoc.getLength(), text.substring(algOutputEndMarker + 1, algOutputStartMarker), null);
                                            outputAttributeSet = algebraicOutputAttributeSet;
                                            text.delete(0, algOutputStartMarker + 1);
                                        }
                                    } else if (algOutputStartMarker >= 0) {
                                        // TEXT < algOutputStartMarker < TEXT
                                        styledDoc.insertString(styledDoc.getLength(), text.substring(0, algOutputStartMarker), null);
                                        styledDoc.insertString(styledDoc.getLength(), text.substring(algOutputStartMarker + 1), algebraicOutputAttributeSet);
                                        outputAttributeSet = algebraicOutputAttributeSet;
                                        break;
                                    } else if (algOutputEndMarker >= 0) {
                                        // TEXT < algOutputEndMarker < TEXT
                                        styledDoc.insertString(styledDoc.getLength(), text.substring(0, algOutputEndMarker), algebraicOutputAttributeSet);
                                        outputAttributeSet = null;
                                        processPromptMarkers(algOutputEndMarker + 1);
                                        break;
                                    } else {
                                        // No algebraic output markers.
                                        processPromptMarkers(0);
                                        break;
                                    }
                                }
                                break; // end of case RunREDUCEPrefs.REDFRONT
                        } // end of switch (RunREDUCEPrefs.colouredIOState)

                        text.setLength(0); // delete any remaining text
                        outputTextPane.setCaretPosition(styledDoc.getLength());
                    }
                    Thread.sleep(10);
                } else if ((c = br.read()) != -1) {
                    if (RunREDUCE.debugOutput) {
                        if (Character.isISOControl((char) c)) {
                            if ((char) c != '\r') {
                                if ((char) c == '\n')
                                    text.append((char) c);
                                else
                                    text.append('|').append((char) c).append('^').append((char) (c + 64)).append('|');
                            }
                        } else
                            text.append((char) c);
                    } else {
                        if ((char) c != '\r') // ignore CRs
                            text.append((char) c);
                    }
                } else break;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    void processPromptMarkers(int start) throws BadLocationException {
        // Look for prompt markers:
        int promptStartMarker = text.indexOf("\u0001", start);
        int promptEndMarker = text.indexOf("\u0002", start);
        if (promptStartMarker >= 0 && promptEndMarker >= 0) {
            styledDoc.insertString(styledDoc.getLength(), text.substring(start, promptStartMarker), outputAttributeSet);
            styledDoc.insertString(styledDoc.getLength(), text.substring(promptStartMarker + 1, promptEndMarker), algebraicPromptAttributeSet);
            styledDoc.insertString(styledDoc.getLength(), text.substring(promptEndMarker + 1), null);
        } else {
            styledDoc.insertString(styledDoc.getLength(), text.substring(start), outputAttributeSet);
        }
    }
}

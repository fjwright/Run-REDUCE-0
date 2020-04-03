package fjwright.runreduce;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines a command to run REDUCE and checks that it is executable.
 * The run method runs REDUCE as a sub-process.
 */
class RunREDUCECommand {
    String version = ""; // e.g. "CSL REDUCE" or "PSL REDUCE"
    String versionRootDir = ""; // version-specific reduceRootDir.
    String[] command = {"", "", "", "", "", ""}; // executable pathname followed by arguments

    RunREDUCECommand() {
    }

    RunREDUCECommand(String version, String versionRootDir, String... command) {
        this.version = version;
        this.versionRootDir = versionRootDir;
        this.command = command;
    }

    // Merge this method into the constructor?
    String[] buildCommand() {
        // Replace $REDUCE by versionRootDir if non-null else by reduceRootDir.
        Path reduceRootPath = Paths.get(
                !versionRootDir.equals("") ? versionRootDir : RunREDUCE.reduceConfiguration.reduceRootDir);
        String[] command = new String[this.command.length];
        for (int i = 0; i < this.command.length; i++) {
            String element = this.command[i];
            if (element.startsWith("$REDUCE/"))
                element = reduceRootPath.resolve(element.substring(8)).toString();
            command[i] = element;
        }
        if (!Files.isExecutable(Paths.get(command[0]))) {
            RunREDUCE.errorMessageDialog(
                    command[0] + " is not executable!",
                    "REDUCE Configuration Error");
            return null;
        }
        return command;
    }

    static PrintWriter reduceInputPrintWriter;

    void run() {
        String[] command = buildCommand();
        if (command == null) return;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            // pb.redirectInput(ProcessBuilder.Redirect.INHERIT); // Works!
            Process p = pb.start();

            // Assign the REDUCE input stream to a global variable:
            OutputStreamWriter osr = new OutputStreamWriter(p.getOutputStream());
            reduceInputPrintWriter = new PrintWriter(osr);

            // Start a thread to handle the REDUCE output stream
            // (assigned to a global variable):
            ReduceOutputThread outputGobbler = new
                    ReduceOutputThread(p.getInputStream(), RunREDUCE.outputTextPane);
            outputGobbler.start();

            // Initialise enabled state of menu items etc.:
            RunREDUCEMenubar.whenREDUCERunning(true);
        } catch (Exception exc) {
            RunREDUCE.errorMessageDialog(
                    "Error running REDUCE -- " + exc,
                    "REDUCE Process Error");
        }

        RunREDUCEPrefs.colouredIOState = RunREDUCEPrefs.colouredIOIntent;
        if (RunREDUCEPrefs.colouredIOState == RunREDUCEPrefs.ColouredIO.REDFRONT &&
                RunREDUCECommand.reduceInputPrintWriter != null) {
            RunREDUCECommand.reduceInputPrintWriter.print("load_package redfront;");
            RunREDUCECommand.reduceInputPrintWriter.flush();
        }
    }
}

/**
 * This thread reads from the REDUCE output pipe and appends it to the GUI output pane.
 */
class ReduceOutputThread extends Thread {
    InputStream input;        // REDUCE pipe output (buffered)
    JTextPane outputTextPane; // GUI output pane
    private static StyledDocument styledDoc;
    static final SimpleAttributeSet algebraicPromptAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet symbolicPromptAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet algebraicOutputAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet symbolicOutputAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet algebraicInputAttributeSet = new SimpleAttributeSet();
    static final SimpleAttributeSet symbolicInputAttributeSet = new SimpleAttributeSet();
    static SimpleAttributeSet promptAttributeSet = new SimpleAttributeSet();
    static SimpleAttributeSet inputAttributeSet;
    static SimpleAttributeSet outputAttributeSet; // for initial header
    private static final Pattern promptPattern = Pattern.compile("\\d+([:*]) ");
    private static final StringBuilder text = new StringBuilder();

    ReduceOutputThread(InputStream input, JTextPane outputTextPane) {
        this.input = input;
        this.outputTextPane = outputTextPane;
        styledDoc = outputTextPane.getStyledDocument();
        StyleConstants.setForeground(algebraicOutputAttributeSet, Color.blue);
        StyleConstants.setForeground(symbolicOutputAttributeSet, Color.cyan);
        StyleConstants.setForeground(algebraicInputAttributeSet, Color.red);
        StyleConstants.setForeground(symbolicInputAttributeSet, Color.green);
        StyleConstants.setForeground(algebraicPromptAttributeSet, Color.red);
        StyleConstants.setForeground(symbolicPromptAttributeSet, Color.green);
    }

    public void run() {
        // Must output characters rather than lines so that prompt appears!
        try (InputStreamReader isr = new InputStreamReader(input);
             BufferedReader br = new BufferedReader(isr)) {
            int c;
            for (; ; ) {
                if (!br.ready()) {
                    int textLength = text.length();
                    if (textLength > 0) {

                        switch (RunREDUCEPrefs.colouredIOState) {
                            case MODAL: // mode coloured IO display processing
                                // Split off the final line, which should consist of the next input prompt:
                                int promptIndex = text.lastIndexOf("\n") + 1;
                                String promptString;
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
                                // FixMe Errors using PSL and inputting alg.tst!
                                outputAttributeSet = null;
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

                            case NONE:
                            default: // no IO display colouring, but maybe prompt processing
                                if ((RunREDUCEPrefs.boldPromptsState) &&
                                        (promptIndex = text.lastIndexOf("\n") + 1) < textLength &&
                                        promptPattern.matcher(promptString = text.substring(promptIndex)).matches()) {
                                    styledDoc.insertString(styledDoc.getLength(), text.substring(0, promptIndex), null);
                                    styledDoc.insertString(styledDoc.getLength(), promptString, promptAttributeSet);
                                } else
                                    styledDoc.insertString(styledDoc.getLength(), text.toString(), null);
                        } // end of switch (RunREDUCEPrefs.colouredIOState)

                        text.setLength(0); // delete any remaining text
                        outputTextPane.setCaretPosition(styledDoc.getLength());
                    }
                    Thread.sleep(10);
                } else if ((c = br.read()) != -1) {
                    if ((char) c != '\r') // ignore CRs
                        text.append((char) c);
//                    if (Character.isISOControl((char) c)) {
//                        if ((char) c != '\r') {
//                            if ((char) c == '\n')
//                                text.append((char) c);
//                            else
//                                text.append('|').append(Character.getName(c)).append(' ').append(c).append('|');
//                        }
//                    } else
//                        text.append((char) c);
                } else break;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    static void processPromptMarkers(int start) throws BadLocationException {
        // Look for prompt markers:
        int promptStartMarker = text.indexOf("\u0001");
        int promptEndMarker = text.indexOf("\u0002");
        if (promptStartMarker >= 0 && promptEndMarker >= 0) {
            styledDoc.insertString(styledDoc.getLength(), text.substring(start, promptStartMarker), outputAttributeSet);
            styledDoc.insertString(styledDoc.getLength(), text.substring(promptStartMarker + 1, promptEndMarker), algebraicPromptAttributeSet);
            styledDoc.insertString(styledDoc.getLength(), text.substring(promptEndMarker + 1), null);
        } else {
            styledDoc.insertString(styledDoc.getLength(), text.substring(start), outputAttributeSet);
        }
    }
}

/**
 * This class provides a list of all REDUCE packages by parsing the package.map file.
 * The list excludes preloaded packages, and it is sorted alphabetically.
 */
class REDUCEPackageList extends ArrayList<String> {

    REDUCEPackageList() {
        Path packagesRootPath = Paths.get(RunREDUCE.reduceConfiguration.packagesRootDir);
        Path packageMapFile = packagesRootPath.resolve("packages/package.map");
        if (!Files.isReadable(packageMapFile)) {
            RunREDUCE.errorMessageDialog(
                    new String[]{"The REDUCE package map file is not available!",
                            "Please correct 'Packages Root Dir' in the 'Configure REDUCE...' dialogue,",
                            "which will open automatically when you close this dialogue."},
                    "REDUCE Package Error");
            RunREDUCEMenubar.showREDUCEConfigDialog();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(packageMapFile)) {
            String line;
            Pattern pattern = Pattern.compile("\\s*\\((\\w+)");
            // The preloaded packages are these (using non-capturing groups):
            Pattern exclude = Pattern.compile("(?:alg)|(?:arith)|(?:mathpr)|(?:poly)|(?:rlisp)");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.lookingAt()) {
                    String pkg = matcher.group(1);
                    if (!exclude.matcher(pkg).matches()) this.add(pkg);
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        Collections.sort(this);

        // For testing only:
//        for (String s : this) {
//            System.out.print(s);
//            System.out.print(" ");
//        }
    }
}

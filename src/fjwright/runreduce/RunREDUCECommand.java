package fjwright.runreduce;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.io.*;
import java.nio.file.*;
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
    }
}

/**
 * This thread reads from the REDUCE output pipe and appends it to the GUI output pane.
 */
class ReduceOutputThread extends Thread {
    InputStream input;        // REDUCE pipe output (buffered)
    JTextPane outputTextPane; // GUI output pane
    static SimpleAttributeSet outputSimpleAttributeSet = new SimpleAttributeSet();
    static SimpleAttributeSet promptSimpleAttributeSet = new SimpleAttributeSet();

    ReduceOutputThread(InputStream input, JTextPane outputTextPane) {
        this.input = input;
        this.outputTextPane = outputTextPane;
        StyleConstants.setBold(promptSimpleAttributeSet,true);
    }

    public void run() {
        StyledDocument styledDoc = outputTextPane.getStyledDocument();
        StringBuilder text = new StringBuilder();
        SimpleAttributeSet outputSimpleAttributeSet = null;
        // Must output characters rather than lines so that prompt appears!
        try (InputStreamReader isr = new InputStreamReader(input);
             BufferedReader br = new BufferedReader(isr)) {
            int c;
            for (; ; ) {
                if (!br.ready()) {
                    if (text.length() > 0) {
                        // Split off the final line consisting of the next input prompt:
                        int promptIndex = text.lastIndexOf("\n") + 1;
                        styledDoc.insertString(styledDoc.getLength(), text.substring(0,promptIndex), outputSimpleAttributeSet);
                        styledDoc.insertString(styledDoc.getLength(), text.substring(promptIndex), promptSimpleAttributeSet);
                        text.setLength(0);
                        outputTextPane.setCaretPosition(styledDoc.getLength());
                        // Only colour output *after* initial header info:
                        outputSimpleAttributeSet = ReduceOutputThread.outputSimpleAttributeSet;
                    }
                    Thread.sleep(10);
                } else if ((c = br.read()) != -1) {
                    if ((char) c != '\r') // ignore CRs
                        text.append((char) c);
                } else break;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
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

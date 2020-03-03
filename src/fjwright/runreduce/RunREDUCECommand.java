package fjwright.runreduce;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines a command to run REDUCE and checks that it is executable.
 * The run method runs REDUCE as a sub-process.
 */
class RunREDUCECommand {
    String version; // e.g. "CSL" or "PSL"
    String[] command; // absolute executable pathname followed by arguments

    RunREDUCECommand(String version, String... command) {
        this.version = version;
        this.command = command;
    }

    // Merge this method into the constructor?
    String[] buildCommand() {
        if (FindREDUCE.reduceRootPath == null) FindREDUCE.findREDUCERootDir();
        String[] command = new String[this.command.length];
        // Replace a leading occurrence of $REDUCE/ in any element by reduceRootPath:
        for (int i = 0; i < this.command.length; i++) {
            String element = this.command[i];
            if (element.startsWith("$REDUCE/"))
                element = FindREDUCE.reduceRootPath.resolve(element.substring(8)).toString();
            command[i] = element;
        }
        if (!Files.isExecutable(Paths.get(command[0]))) {
            System.err.println("Fatal error: " + command[0] + " is not executable!");
            System.exit(1);
        }
        return command;
    }

    static PrintWriter reduceInputPrintWriter;

    void run() {
        try {
            ProcessBuilder pb = new ProcessBuilder(buildCommand());
            pb.redirectErrorStream(true);
            // pb.redirectInput(ProcessBuilder.Redirect.INHERIT); // Works!
            Process p = pb.start();

            // Assign the REDUCE input stream to a global variable:
            OutputStreamWriter osr = new OutputStreamWriter(p.getOutputStream());
            reduceInputPrintWriter = new PrintWriter(osr);

            // Start a thread to handle the REDUCE output stream
            // (assigned to a global variable):
            ReduceOutputThread outputGobbler = new
                    ReduceOutputThread(p.getInputStream(), RunREDUCE.outputTextArea);
            outputGobbler.start();

        } catch (Exception exc) {
            System.err.println("Fatal error running REDUCE -- " + exc);
            System.exit(1);
        }
    }
}

/*
 * This class defines a list of commands to run different versions of REDUCE.
 */
class RunREDUCECommands extends ArrayList<RunREDUCECommand> {
    RunREDUCECommands() {
        // $REDUCE will be replaced by the root of the REDUCE installation
        // before attempting to run REDUCE.
        add(new RunREDUCECommand("CSL REDUCE",
                "$REDUCE/lib/csl/reduce.exe",
                "--nogui"));
        add(new RunREDUCECommand("PSL REDUCE",
                "$REDUCE/lib/psl/psl/bpsl.exe",
                "-td", "1000", "-f",
                "$REDUCE/lib/psl/red/reduce.img"));
    }
}

/**
 * This thread reads from the REDUCE output pipe and appends it to the GUI output pane.
 */
class ReduceOutputThread extends Thread {
    InputStream input;        // REDUCE pipe output (buffered)
    JTextArea outputTextArea; // GUI output pane

    ReduceOutputThread(InputStream input, JTextArea outputTextArea) {
        this.input = input;
        this.outputTextArea = outputTextArea;
    }

    public void run() {
        // Must output characters rather than lines so that prompt appears!
        try (InputStreamReader isr = new InputStreamReader(input);
             BufferedReader br = new BufferedReader(isr)) {
            int c;
            for (; ; ) {
                if (!br.ready()) {
                    outputTextArea.setCaretPosition
                            (outputTextArea.getDocument().getLength());
                    Thread.sleep(10);
                } else if ((c = br.read()) != -1) {
                    if ((char) c != '\r') // ignore CRs
                        outputTextArea.append(String.valueOf((char) c));
                } else break;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}

/**
 * This class attempts to locate the REDUCE installation directory.
 */
class FindREDUCE {
    static String prefsNodeName = "/fjwright/runreduce";  // cf. package name
    // On Windows, the preferences for this app are stored in the registry under the key
    // Computer\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\fjwright\runreduce
    static Preferences prefs = Preferences.userRoot().node(prefsNodeName);
    // Preference keys for this package
    private static final String REDUCE_ROOT_DIR = "REDUCE_root_dir";
    static Path reduceRootPath = null;

    static void findREDUCERootDir() {
        String reduce = prefs.get(REDUCE_ROOT_DIR, System.getenv("REDUCE"));
        if (reduce != null) {
            try {
                reduceRootPath = Path.of(reduce);
            } catch (InvalidPathException exc) {
                System.err.println("Fatal error processing environment variable $REDUCE = " + reduce);
                System.err.println(exc);
                System.exit(1);
            }
            if (Files.exists(reduceRootPath)) return;
        }

        if (!"Windows 10".equals(System.getProperty("os.name"))) {
            System.err.println("Fatal error: Only Windows 10 currently supported!");
            System.exit(1);
        }
        boolean reduceRootPathFound = false;
        Path targetPath = Path.of("Program Files", "Reduce");
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            reduceRootPath = root.resolve(targetPath);
            if (Files.exists(reduceRootPath)) {
                reduceRootPathFound = true;
                break;
            }
        }
        if (!reduceRootPathFound) {
            System.err.println("Fatal error: REDUCE installation directory not found!");
            System.exit(1);
        }
    }
}

/**
 * This class provides a list of all REDUCE packages by finding the
 * REDUCE packages directory in a standard installation and parsing
 * the package.map file.
 * The list excludes preloaded packages, and it is sorted alphabetically.
 */
class REDUCEPackageList extends ArrayList<String> {

    REDUCEPackageList() {
        if (FindREDUCE.reduceRootPath == null) FindREDUCE.findREDUCERootDir();
        Path packageMapFile = FindREDUCE.reduceRootPath.resolve("packages/package.map");
        if (!Files.isReadable(packageMapFile)) {
            System.err.println("REDUCE package map file is not available!");
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

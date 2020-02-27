package fjwright.runreduce;

import javax.swing.JTextArea;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This class runs the REDUCE sub-process.
 */
class RunREDUCEProcess {
    static PrintWriter reduceInputPrintWriter;

    public static void reduce() {
        if (FindREDUCE.reduceRootPath == null) FindREDUCE.findREDUCERootDir();
        Path CSLRootPath = FindREDUCE.reduceRootPath.resolve("lib").resolve("csl");
        String[] CSLProcessBuilderArgs =
                {CSLRootPath.resolve("reduce.exe").toString(), "--nogui"};

        Path PSLRootPath = FindREDUCE.reduceRootPath.resolve("lib").resolve("psl");
        String[] PSLProcessBuilderArgs =
                {PSLRootPath.resolve("psl").resolve("bpsl.exe").toString(),
                        "-td", "1000", "-f",
                        PSLRootPath.resolve("red").resolve("reduce.img").toString()};

        try {
            ProcessBuilder pb = new ProcessBuilder(CSLProcessBuilderArgs);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * This class attempts to locate the REDUCE installation directory.
 */
class FindREDUCE {
    static Path reduceRootPath = null;

    static void findREDUCERootDir() {
        if (!"Windows 10".equals(System.getProperty("os.name"))) {
            System.err.println("Only Windows 10 currently supported!");
            System.exit(1);
        }
        boolean reduceRootPathFound = false;
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            reduceRootPath = root.resolve("Program Files/Reduce");
            if (Files.exists(reduceRootPath)) {
                reduceRootPathFound = true;
                break;
            }
        }
        if (!reduceRootPathFound) {
            System.err.println("REDUCE installation directory not found!");
            System.exit(1);
        }
    }
}

/**
 * This class provides a list of all REDUCE packages by finding the
 * REDUCE packages directory in a standard installation and parsing
 * the packages.map file.
 */
class REDUCEPackageList extends ArrayList<String> {

    REDUCEPackageList() {
        if (FindREDUCE.reduceRootPath == null) FindREDUCE.findREDUCERootDir();
        Path packageMapFile = FindREDUCE.reduceRootPath.resolve("packages/package.map");
        if (!Files.isReadable(packageMapFile)) {
            System.err.println("REDUCE packages map file is not readable!");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(packageMapFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 2 && line.substring(0, 2).equals(" (")) {
                    this.add(line.substring(2, line.indexOf(' ', 3)));
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        // For testing only:
//        for (int i = 0; i < this.size(); i++) {
//            System.out.print(this.get(i));
//            System.out.print(" ");
//        }
    }
}

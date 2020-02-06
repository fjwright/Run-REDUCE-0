package fjwright.runreduce;

import javax.swing.JTextArea;
import java.io.*;


/**
 * This class runs the REDUCE sub-process.
 */
class RunREDUCEProcess {
    static PrintWriter reduceInputPrintWriter;

    public static void reduce() {
        try {
            ProcessBuilder pb =
                new ProcessBuilder("D:\\Program Files\\Reduce\\lib\\psl\\psl\\bpsl.exe",
                                   "-td", "1000", "-f",
                                   "D:\\Program Files\\Reduce\\lib\\psl\\red\\reduce.img");
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
 * This thread reads from the REDUCE output pipe and appends it to the
 * GUI output pane.
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
            for (;;) {
                if (!br.ready())
                    Thread.sleep(10);
                else if ((c = br.read()) != -1) {
                    outputTextArea.append(String.valueOf((char) c));
                    // outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
                }
                else break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

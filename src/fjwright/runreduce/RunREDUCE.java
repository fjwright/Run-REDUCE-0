/*
 * Prototype Java Swing GUI to run CLI REDUCE.
 * Based on https://docs.oracle.com/javase/tutorial/uiswing/index.html.
 * This file is ../fjwright/runreduce/RunREDUCE.java
 * It requires also ../fjwright/runreduce/*.java
 * Compile and run the app from the PARENT directory of fjwright:
 * javac fjwright/runreduce/RunREDUCE.java
 * java fjwright.runreduce.RunREDUCE
 * (The above works in a Microsoft Windows cmd shell.)
 */

package fjwright.runreduce;

import javax.swing.*;
import java.awt.*;

/**
 * This is the main class that sets up and runs the application.
 **/
public class RunREDUCE {
    static JFrame frame;
    static Font reduceFont;
    static REDUCEConfigurationDefault reduceConfigurationDefault;
    static REDUCEConfiguration reduceConfiguration;
    static REDUCEPanel reducePanel;

    /**
     * Create the GUI and show it.
     * For thread safety, this method should be invoked from the event-dispatching thread.
     **/
    private static void createAndShowGUI() {
        // Create and set up the window:
        frame = new JFrame("RunREDUCE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set the main window to 2/3 the linear dimension of the screen:
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setPreferredSize(new Dimension((screenSize.width * 2) / 3, (screenSize.height * 2) / 3));

        // Create the menu bar and add it to the frame:
        new RRMenuBar(frame);

        // REDUCE I/O requires a monospaced font:
//    static Font reduceFont = new Font(Font.MONOSPACED, Font.PLAIN, RunREDUCEPrefs.fontSize);
        reduceFont = new Font("DejaVu Sans Mono", Font.PLAIN, RRPreferences.fontSize);
        if (debugPlatform) System.err.println("I/O display font: " + reduceFont.getName());

        // Add content to the window:
        reducePanel = new REDUCEPanel();
        frame.add(reducePanel);

        // Display the window:
        frame.pack();
        frame.setVisible(true);
    }

    static void errorMessageDialog(Object message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Run-time argument processing:
    static boolean debugPlatform, debugOutput;
    private static final String debugPlatformOption = "-debugPlatform";
    private static final String debugOutputOption = "-debugOutput";

    public static void main(String... args) {
        for (String arg : args) {
            switch (arg) {
                case debugPlatformOption:
                    debugPlatform = true;
                    break;
                case debugOutputOption:
                    debugOutput = true;
                    break;
                default:
                    System.err.format("Unrecognised argument: %s. Allowed options are: %s and %s.",
                            arg, debugPlatformOption, debugOutputOption);
            }
        }
        reduceConfigurationDefault = new REDUCEConfigurationDefault();
        reduceConfiguration = new REDUCEConfiguration();
        // Schedule jobs for the event-dispatching thread.
        // Create and show this application's GUI:
        SwingUtilities.invokeLater(RunREDUCE::createAndShowGUI);
    }
}

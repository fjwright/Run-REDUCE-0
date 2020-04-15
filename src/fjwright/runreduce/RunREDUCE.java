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
    static JTabbedPane tabbedPane;
    static int tabLabelNumber = 1;
    static REDUCEPanel reducePanel;
    static boolean enableTabbedPaneChangeListener = true;

    static REDUCEConfigurationDefault reduceConfigurationDefault;
    static REDUCEConfiguration reduceConfiguration;

    /**
     * Create the GUI and show it.
     * For thread safety, this method should be invoked from the event-dispatching thread.
     **/
    private static void createAndShowGUI() {
        // Create and set up the window:
        frame = new JFrame("Run-REDUCE");
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

        reducePanel = new REDUCEPanel();
        if (RRPreferences.tabbedDisplayState)
            useTabbedPane(true);
        else
            frame.add(reducePanel);

        // Display the window:
        frame.pack();
        frame.setVisible(true);
    }

    static void useTabbedPane(boolean enable) {
        enableTabbedPaneChangeListener = false;
        if (enable) {
            tabbedPane = new JTabbedPane();
            frame.remove(reducePanel);
            frame.add(tabbedPane);
            tabbedPane.addChangeListener(e -> {
                if (enableTabbedPaneChangeListener) {
                    int addTabIndex = tabbedPane.getTabCount() - 1;
                    if (tabbedPane.getSelectedIndex() == addTabIndex) addTab();
                    else if ((reducePanel = (REDUCEPanel) tabbedPane.getSelectedComponent()) != null) {
                        reducePanel.menuItemStatus.updateMenus();
                        reducePanel.inputTextArea.requestFocusInWindow();
                    }
                }
            });
            tabLabelNumber = 1;
            tabbedPane.addTab(reducePanel.title != null ? reducePanel.title : "Tab 1", reducePanel);
            tabbedPane.setTabComponentAt(0, new ButtonTabComponent(tabbedPane));
            tabbedPane.addTab("+", null, null, "Add a new REDUCE tab.");
        } else {
            if (tabbedPane != null) {
                frame.remove(tabbedPane);
                tabbedPane = null; // release resources
            }
            // Retain the reducePanel from the selected tab if possible:
            if (reducePanel == null) reducePanel = new REDUCEPanel();
            frame.add(reducePanel);
            frame.pack();
        }
        enableTabbedPaneChangeListener = true;
    }

    static void addTab() {
        enableTabbedPaneChangeListener = false;
        if (!RRPreferences.tabbedDisplayState) { // enable tabbed pane
            RRMenuBar.tabbedDisplayCheckBox.setState(RRPreferences.tabbedDisplayState = true);
            RRPreferences.save(RRPreferences.TABBEDDISPLAY);
            useTabbedPane(true);
        }
        int lastTabIndex = tabbedPane.getTabCount() - 1;
        tabbedPane.insertTab("Tab " + (++tabLabelNumber), null, reducePanel = new REDUCEPanel(), null, lastTabIndex);
        tabbedPane.setTabComponentAt(lastTabIndex, new ButtonTabComponent(tabbedPane));
        tabbedPane.setSelectedIndex(lastTabIndex);
        RRMenuBar.removeTabMenuItem.setEnabled(true);
        enableTabbedPaneChangeListener = true;
    }

    static void removeTab() {
        // ToDo Does this leave zombie REDUCE processes?
        enableTabbedPaneChangeListener = false;
        if (tabbedPane.getTabCount() > 2) {
            int selectedIndex = tabbedPane.getSelectedIndex();
            // Remove both tab and content:
            tabbedPane.remove(selectedIndex);
            if (selectedIndex == tabbedPane.getTabCount() - 1)
                tabbedPane.setSelectedIndex(selectedIndex - 1);
        } else { // disable tabbed pane
            useTabbedPane(false);
            RRMenuBar.tabbedDisplayCheckBox.setState(RRPreferences.tabbedDisplayState = false);
            RRPreferences.save(RRPreferences.TABBEDDISPLAY);
            RRMenuBar.removeTabMenuItem.setEnabled(false);
        }
        enableTabbedPaneChangeListener = true;
    }

    static void errorMessageDialog(Object message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Run-time argument processing:
    static boolean debugPlatform, debugOutput;
    private static final String debugPlatformArg = "-debugPlatform";
    private static final String debugOutputArg = "-debugOutput";
    private static final String lfNativeArg = "-lfNative";
    private static final String lfMotifArg = "-lfMotif";

    public static void main(String... args) {
        String lookAndFeel = null;
        for (String arg : args) {
            switch (arg) {
                case debugPlatformArg:
                    debugPlatform = true;
                    break;
                case debugOutputArg:
                    debugOutput = true;
                    break;
                case lfNativeArg:
                    lookAndFeel = UIManager.getSystemLookAndFeelClassName();
                    break;
                case lfMotifArg:
                    lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
                    break;
                default:
                    System.err.format("Unrecognised argument: %s.\nAllowed arguments are: %s, %s, %s and %s.",
                            arg, debugPlatformArg, debugOutputArg, lfNativeArg, lfMotifArg);
            }
        }

        if (lookAndFeel == null) {
            switch (RRPreferences.lookAndFeelState) {
                case JAVA:
                default:
                    // lookAndFeel = null
                    break;
                case NATIVE:
                    lookAndFeel = UIManager.getSystemLookAndFeelClassName();
                    break;
                case MOTIF:
                    lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            }
        }

        if (lookAndFeel != null) try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println(e);
        }

        reduceConfigurationDefault = new REDUCEConfigurationDefault();
        reduceConfiguration = new REDUCEConfiguration();
        // Schedule jobs for the event-dispatching thread.
        // Create and show this application's GUI:
        SwingUtilities.invokeLater(RunREDUCE::createAndShowGUI);
    }
}

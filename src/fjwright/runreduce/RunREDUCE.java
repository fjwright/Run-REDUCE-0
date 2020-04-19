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
import java.awt.event.*;

/**
 * This is the main class that sets up and runs the application.
 **/
public class RunREDUCE {
    static JFrame frame;
    static Font reduceFont;
    static JSplitPane splitPane;
    static JTabbedPane tabbedPane;
    static int tabLabelNumber = 1;
    static REDUCEPanel reducePanel;
    static boolean enableTabbedPaneChangeListener = true;
    static final REDUCEPanelMouseListener REDUCE_PANEL_MOUSE_LISTENER = new REDUCEPanelMouseListener();

    static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    // Set the main window to 2/3 the linear dimension of the screen initially:
    static Dimension initialFrameSize = // reset to null once used!
            new Dimension((SCREEN_SIZE.width * 2) / 3, (SCREEN_SIZE.height * 2) / 3);

    static REDUCEConfigurationDefault reduceConfigurationDefault;
    static REDUCEConfiguration reduceConfiguration;

    private static final Action nextPanel = new NextPanel();
    private static final Action nextTab = new NextTab();

    /**
     * Create the GUI and show it.
     * For thread safety, this method should be invoked from the event-dispatching thread.
     **/
    private static void createAndShowGUI() {
        // Create and set up the window:
        frame = new JFrame("Run-REDUCE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the menu bar and add it to the frame:
        new RRMenuBar(frame);

        // REDUCE I/O requires a monospaced font:
//    static Font reduceFont = new Font(Font.MONOSPACED, Font.PLAIN, RunREDUCEPrefs.fontSize);
        reduceFont = new Font("DejaVu Sans Mono", Font.PLAIN, RRPreferences.fontSize);
        if (debugPlatform) System.err.println("I/O display font: " + reduceFont.getName());

        reducePanel = new REDUCEPanel();
        switch (RRPreferences.displayPane) {
            case SINGLE:
                frame.add(reducePanel);
                frame.setPreferredSize(initialFrameSize);
                frame.pack();
                break;
            case SPLIT:
                useSplitPane(true);
                break;
            case TABBED:
                useTabbedPane(true);
        }
        initialFrameSize = null;

        // Display the window:
        frame.setVisible(true);
    }

    static void useSplitPane(boolean enable) {
        if (enable) {
            REDUCEPanel reducePanel2 = new REDUCEPanel();
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, reducePanel, reducePanel2);
            splitPane.setResizeWeight(0.5);
            splitPane.setOneTouchExpandable(true);
            frame.add(splitPane);
            reducePanel.addMouseListener(REDUCE_PANEL_MOUSE_LISTENER);
            reducePanel2.addMouseListener(REDUCE_PANEL_MOUSE_LISTENER);
            reducePanel2.setSelected(false);

            splitPane.setFocusable(true); // necessary despite using the WHEN_FOCUSED input map!
            InputMap inputMap = splitPane.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap actionMap = splitPane.getActionMap();
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.CTRL_DOWN_MASK), "nextPanel");
            actionMap.put("nextPanel", nextPanel);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "nextPanel");
            actionMap.put("nextPanel", nextPanel);
        } else { // Revert to single pane.
            splitPane.getLeftComponent().removeMouseListener(REDUCE_PANEL_MOUSE_LISTENER);
            splitPane.getRightComponent().removeMouseListener(REDUCE_PANEL_MOUSE_LISTENER);
            // Retain the reducePanel from the selected tab if possible:
            frame.remove(splitPane);
            splitPane = null; // release resources
            frame.add(reducePanel);
        }
        frame.setPreferredSize(initialFrameSize != null ? initialFrameSize : frame.getSize());
        frame.pack();
        if (enable) splitPane.setDividerLocation(0.5); // must be after pack()!
    }

    private static class REDUCEPanelMouseListener extends MouseAdapter {
        /**
         * Invoked when the mouse button has been clicked (pressed
         * and released) on a component.
         *
         * @param e the event to be processed
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            reducePanel.setSelected(false);
            Component c = e.getComponent();
            while (!(c instanceof REDUCEPanel)) {
                c = c.getParent();
            }
            reducePanel = (REDUCEPanel) c;
            reducePanel.menuItemStatus.updateMenus();
            reducePanel.inputTextArea.requestFocusInWindow();
            reducePanel.setSelected(true);
        }
    }

    private static class NextPanel extends AbstractAction {
        /**
         * Invoked when an action occurs.
         *
         * @param e the event to be processed
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            reducePanel.setSelected(false);
            REDUCEPanel reducePanel2 = (REDUCEPanel) splitPane.getLeftComponent();
            if (reducePanel == reducePanel2) {
                reducePanel = (REDUCEPanel) splitPane.getRightComponent();
            } else {
                reducePanel = reducePanel2;
            }
            reducePanel.menuItemStatus.updateMenus();
            reducePanel.inputTextArea.requestFocusInWindow();
            reducePanel.setSelected(true);
        }
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

            tabbedPane.setFocusable(true);
            InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            ActionMap actionMap = frame.getRootPane().getActionMap();
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.CTRL_DOWN_MASK), "nextTab");
            actionMap.put("nextTab", nextTab);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "nextTab");
            actionMap.put("nextTab", nextTab);
        } else { // Revert to single pane.
            // Retain the reducePanel from the selected tab:
            reducePanel = (REDUCEPanel) tabbedPane.getSelectedComponent();
            frame.remove(tabbedPane);
            tabbedPane = null; // release resources
            frame.add(reducePanel);
            reducePanel.menuItemStatus.updateMenus();
            reducePanel.inputTextArea.requestFocusInWindow();
        }
        frame.setPreferredSize(initialFrameSize != null ? initialFrameSize : frame.getSize());
        frame.pack();
        enableTabbedPaneChangeListener = true;
    }

    private static class NextTab extends AbstractAction {
        /**
         * Invoked when an action occurs.
         *
         * @param e the event to be processed
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            System.err.println("NextTab action called.");
            enableTabbedPaneChangeListener = false; // ???
            int nextTabIndex = tabbedPane.getSelectedIndex() + 1;
            if (nextTabIndex == tabbedPane.getTabCount() - 1) nextTabIndex = 0;
            tabbedPane.setSelectedIndex(nextTabIndex);
            reducePanel = (REDUCEPanel) tabbedPane.getSelectedComponent();
            reducePanel.menuItemStatus.updateMenus();
            reducePanel.inputTextArea.requestFocusInWindow();
            enableTabbedPaneChangeListener = true; // ???
        }
    }

    static void addTab() {
        enableTabbedPaneChangeListener = false;
        if (RRPreferences.displayPane != RRPreferences.DisplayPane.TABBED) { // enable tabbed pane
            RRMenuBar.tabbedPaneRadioButton.setSelected(true);
            RRPreferences.save(RRPreferences.DISPLAYPANE, RRPreferences.DisplayPane.TABBED);
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
        enableTabbedPaneChangeListener = false;
        if (tabbedPane.getTabCount() > 2) {
            int selectedIndex = tabbedPane.getSelectedIndex();
            // Remove both tab and content:
            tabbedPane.remove(selectedIndex);
            if (selectedIndex == tabbedPane.getTabCount() - 1)
                tabbedPane.setSelectedIndex(selectedIndex - 1);
        } else { // disable tabbed pane
            useTabbedPane(false);
            RRMenuBar.singlePaneRadioButton.setSelected(true);
            RRPreferences.save(RRPreferences.DISPLAYPANE, RRPreferences.DisplayPane.SINGLE);
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

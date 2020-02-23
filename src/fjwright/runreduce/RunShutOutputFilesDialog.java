package fjwright.runreduce;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.List;          // Also in java.awt!
import java.util.ArrayList;
import java.lang.reflect.*;

public class RunShutOutputFilesDialog {
    // Temporary test data:
    static final List<File> outputFileList =
        new ArrayList<>(java.util.Arrays.asList
                        (new File[] {new File("file1"), new File("file2"), new File("file3"), new File("file4"), new File("file5")}));

    static ShutOutputFilesDialog dialog = new ShutOutputFilesDialog((Frame) null);

    public static void main(String... args) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    // dialog = new ShutOutputFilesDialog((Frame) null);
                    int[] fileIndices = dialog.showDialog(outputFileList);
                    int length = fileIndices.length;
                    if (length != 0) {
                        // Process backwards to avoid remove changing subsequent indices:
                        String text = outputFileList.remove(fileIndices[--length]).toString() + "\"$";
                        for (int i = --length; i >= 0; i--)
                            text = outputFileList.remove(fileIndices[i]).toString() + "\", \"" + text;
                        System.out.println("shut \"" + text);
                        System.out.println(outputFileList);
                    }
                }
            });
    }
}

class ShutOutputFilesDialog extends JDialog implements ActionListener {
    private static JList<File> list = new JList<File>();
    private static int[] fileIndices;

    int[] showDialog(List<File> outputFileList) {
        list.setListData(outputFileList.toArray(new File[0]));
        setVisible(true);
        return fileIndices;
    }

    ShutOutputFilesDialog(Frame frame) {
        // Create a modal dialog:
        super(frame, "Shut Output Files...", true);

        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(5);

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel
            ("Select one or more open output files to shut...");
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Create and initialize the control buttons:
        JButton shutButton = new JButton("Shut");
        shutButton.setActionCommand("Shut");
        shutButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        // Lay out the buttons horizontally and right-justified:
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(shutButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);

        // Lay out the dialog contents:
        add(listPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
        pack();
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        if ("Shut".equals(e.getActionCommand())) {
            fileIndices = list.getSelectedIndices();
        } else
            fileIndices = new int[0];
        setVisible(false);
    }
}

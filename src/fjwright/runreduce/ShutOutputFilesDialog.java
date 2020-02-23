package fjwright.runreduce;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.List;          // Also in java.awt!
import java.util.ArrayList;


/**
 * This class provides a modal dialog to select open output files to
 * shut.
 */
class ShutOutputFilesDialog extends JDialog implements ActionListener {
    private static JList<File> list = new JList<File>();
    private Frame frame;
    private static int[] fileIndices;

    int[] showDialog(List<File> outputFileList) {
        list.setListData(outputFileList.toArray(new File[0]));
        pack();                       // must be done dynamically
        setLocationRelativeTo(frame); // ditto
        setVisible(true);
        return fileIndices;
    }

    ShutOutputFilesDialog(Frame frame) {
        // Create a modal dialog:
        super(frame, "Shut Output Files...", true);
        this.frame = frame;

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
    }

    public void actionPerformed(ActionEvent e) {
        if ("Shut".equals(e.getActionCommand())) {
            fileIndices = list.getSelectedIndices();
        } else
            fileIndices = new int[0];
        setVisible(false);
    }
}

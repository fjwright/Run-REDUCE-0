package fjwright.runreduce;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a modal dialog to select open output files to
 * shut or packages to load.
 */
abstract class RunREDUCEListDialog<E> extends JDialog implements ActionListener {
    protected Frame frame;
    protected JList<E> list = new JList<>();

    RunREDUCEListDialog(Frame frame, String dialogTitle, String listLabel, String buttonText) {
        // Create a modal dialog:
        super(frame, dialogTitle, true);
        this.frame = frame;

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(listLabel);
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0, 5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create and initialize the control buttons:
        JButton shutButton = new JButton(buttonText);
        shutButton.setActionCommand(buttonText);
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
}

/**
 * This class provides a modal dialog to select open output files to
 * shut.
 */
class ShutOutputFilesDialog<File> extends RunREDUCEListDialog<File> {
    private static int[] fileIndices;

    ShutOutputFilesDialog(Frame frame) {
        super(frame,
                "Shut Output Files...",
                "Select one or more open output files to shut...",
                "Shut");
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(5);
    }

    int[] showDialog(List<File> outputFileList) {
        list.setListData((File[]) outputFileList.toArray());
        pack();                       // must be done dynamically
        setLocationRelativeTo(frame); // ditto
        setVisible(true);
        return fileIndices;
    }

    public void actionPerformed(ActionEvent e) {
        if ("Shut".equals(e.getActionCommand())) {
            fileIndices = list.getSelectedIndices();
        } else
            fileIndices = new int[0];
        setVisible(false);
    }
}

/**
 * This class provides a modal dialog to select packages to load.
 */
class LoadPackagesDialog<String> extends RunREDUCEListDialog<String> {
    private List<String> selectedPackages;

    LoadPackagesDialog(Frame frame) {
        // Create a modal dialog:
        super(frame,
                "Load Packages...",
                "Select one or more packages to load...",
                "Load");
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(20);
    }

    List<String> showDialog(List<String> packageList) {
        list.setListData((String[]) packageList.toArray());
        pack();                       // must be done dynamically
        setLocationRelativeTo(frame); // ditto
        setVisible(true);
        return selectedPackages;
    }

    public void actionPerformed(ActionEvent e) {
        if ("Load".equals(e.getActionCommand())) {
            selectedPackages = list.getSelectedValuesList();
        } else
            selectedPackages = new ArrayList<>();
        setVisible(false);
    }
}

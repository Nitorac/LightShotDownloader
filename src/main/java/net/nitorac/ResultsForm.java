package net.nitorac;

import javax.swing.*;

public class ResultsForm {
    public JPanel rootPanel;
    public JList imgList;
    public JPanel viewPanel;
    public JSplitPane splitPane;
    public JPanel logPanel;
    public JScrollPane scrollPane;
    public JPanel imgPanel;
    public JLabel imgLbl;

    private void createUIComponents() {
        imgList = new JList<>(new DefaultListModel<String>());
    }
}

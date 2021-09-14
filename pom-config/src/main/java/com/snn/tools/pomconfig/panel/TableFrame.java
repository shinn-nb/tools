package com.snn.tools.pomconfig.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * @Author: shinn
 * @Date: 2021/9/13 下午6:48 （日期和时间）
 */
public class TableFrame {
    private TableFrame() {}

    public static void showTable(DefaultTableModel tableModel) {
        JFrame frame = new JFrame("明细");
        frame.setBounds(500, 10, 750, 900);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTable jTable = new JTable(tableModel);
        jTable.setEnabled(true);
        JScrollPane scrollPane = new JScrollPane(jTable);
        frame.add(scrollPane);
        frame.setVisible(true);
    }
}

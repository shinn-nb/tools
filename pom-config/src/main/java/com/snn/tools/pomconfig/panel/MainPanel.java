package com.snn.tools.pomconfig.panel;

import com.snn.tools.pomconfig.exception.MavenException;
import com.snn.tools.pomconfig.utils.PomDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.invoker.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * @Author: shinn
 * @Date: 2021/9/13 下午4:38 （日期和时间）
 */
public class MainPanel {
    private final JFrame frame;

    public MainPanel() {
        frame = new JFrame("版本比较");
        frame.setSize(450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);
        JLabel fileLabel1 = new JLabel("版本来源pom路径：");
        JLabel fileLabel2 = new JLabel("版本比较pom路径：");
        JTextField fileText1 = new JTextField(20);
        fileText1.setText("/Users/anan/Documents/workspace/idaas-core/pom.xml");
        JTextField fileText2 = new JTextField(20);
        fileText2.setText("/Users/anan/Documents/workspace/idaas-lcm/pom.xml");

        fileLabel1.setBounds(10, 20, 150, 35);
        panel.add(fileLabel1);
        fileText1.setBounds(165, 20, 250, 35);
        panel.add(fileText1);

        fileLabel2.setBounds(10, 60, 150, 35);
        panel.add(fileLabel2);
        fileText2.setBounds(165, 60, 250, 35);
        panel.add(fileText2);
        Font f = new Font("宋体", Font.PLAIN, 16);

        JLabel text = new JLabel("maven路径:");
        panel.add(text);
        text.setBounds(10, 110, 150, 35);
        JTextField mvn = new JTextField(20);
        panel.add(mvn);
        mvn.setBounds(165, 110, 250, 35);

        fileLabel1.setFont(f);
        fileLabel1.setForeground(Color.red);
        fileText1.setFont(f);
        fileText1.setForeground(Color.red);

        fileLabel2.setFont(f);
        fileLabel2.setForeground(Color.red);
        fileText2.setFont(f);
        fileText2.setForeground(Color.red);

        JCheckBox jCheckBox = new JCheckBox("是否含第三方引用");
        jCheckBox.setBounds(10, 190, 160, 35);

        JButton loginButton = new JButton("比较");
        loginButton.setBounds(10, 230, 80, 35);
        panel.add(loginButton);
        panel.add(jCheckBox);
        loginButton.addActionListener(e -> {
            if (!loginButton.isEnabled()) {
                return;
            }
            try {
                loginButton.setText("正在比较...");
                loginButton.setEnabled(false);
                String file1 = fileText1.getText();
                String file2 = fileText2.getText();
                if (StringUtils.isAnyBlank(file1, file2)) {
                    JOptionPane.showMessageDialog(frame, "存在路径未填写",
                        "警告", JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                if (!StringUtils.endsWith(file1, "pom.xml")) {
                    file1 = file1 + "/pom.xml";
                }
                if (!StringUtils.endsWith(file2, "pom.xml")) {
                    file2 = file2 + "/pom.xml";
                }
                if (!new File(file1).exists() || !new File(file2).exists()) {
                    JOptionPane.showMessageDialog(frame, "请检查路径文件是否存在",

                        "警告", JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                List<Object[]> data = PomDataUtil.compareData(file1, file2, mvn.getText(), jCheckBox.isSelected());
                Object[][] tableData = new Object[data.size()][];
                for (int i = 0, len = data.size(); i < len; i++) {
                    tableData[i] = data.get(i);
                }
                DefaultTableModel tableModel2 = new DefaultTableModel(tableData, COLUMN_NAMES);
                TableFrame.showTable(tableModel2);
            } catch (MavenException | MavenInvocationException e2) {
                e2.printStackTrace();
                JOptionPane.showMessageDialog(frame, "生成失败,请查看输出的报错信息",
                    "错误", JOptionPane.ERROR_MESSAGE
                );
            } finally {
                loginButton.setText("比较");
                loginButton.setEnabled(true);
            }

        });
    }

    private static final Object[]
        COLUMN_NAMES =
        new Object[]{"序号",
            "groupId",
            "artifactId",
            "版本来源/module",
            "版本来源/version",
            "版本比较/module",
            "版本比较/version"};
    //设置列名

    public void showModel() {

        frame.setVisible(true);
    }

}

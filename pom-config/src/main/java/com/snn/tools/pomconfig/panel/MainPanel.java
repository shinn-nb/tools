package com.snn.tools.pomconfig.panel;

import com.snn.tools.pomconfig.entity.JarConfig;
import com.snn.tools.pomconfig.utils.TxtUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: shinn
 * @Date: 2021/9/13 下午4:38 （日期和时间）
 */
public class MainPanel {
    private MainPanel() {}

    private static final Object[]
        COLUMN_NAMES =
        new Object[]{"序号",
            "groupId",
            "artifactId",
            "idaas-core/module",
            "idaas-core/version",
            "idaas-lcm/module",
            "idaas-lcm/version"};
    //设置列名

    public static void initModel() {
        JFrame frame = new JFrame("版本比较");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);
        JLabel fileLabel1 = new JLabel("版本来源路径：");
        JLabel fileLabel2 = new JLabel("版本比较路径：");
        JTextField fileText1 = new JTextField(20);
        fileText1.setText("/Users/anan/Documents/workspace/idaas-core/tree.txt");
        JTextField fileText2 = new JTextField(20);
        fileText2.setText("/Users/anan/Documents/workspace/idaas-lcm/tree.txt");

        fileLabel1.setBounds(10, 20, 100, 35);
        panel.add(fileLabel1);
        fileText1.setBounds(120, 20, 250, 35);
        panel.add(fileText1);

        fileLabel2.setBounds(10, 60, 100, 35);
        panel.add(fileLabel2);
        fileText2.setBounds(120, 60, 250, 35);
        panel.add(fileText2);
        Font f = new Font("宋体", Font.PLAIN, 16);

        JLabel text = new JLabel("路径是workSpace通过 maven 命令生成的txt的地址");
        panel.add(text);
        text.setBounds(10, 110, 400, 35);
        JLabel mvn = new JLabel("命令：mvn dependency:tree --> tree.txt ");
        panel.add(mvn);
        mvn.setBounds(10, 150, 400, 35);

        text.setFont(f);
        text.setForeground(Color.red);
        mvn.setFont(f);
        mvn.setForeground(Color.red);

        JCheckBox jCheckBox = new JCheckBox("是否含第三方引用");
        jCheckBox.setBounds(10, 190, 160, 35);

        JButton loginButton = new JButton("比较");
        loginButton.setBounds(10, 230, 80, 35);
        panel.add(loginButton);
        panel.add(jCheckBox);
        loginButton.addActionListener(e -> {
            String file1 = fileText1.getText();
            String file2 = fileText2.getText();
            if (StringUtils.isAnyBlank(file1, file2)) {
                JOptionPane.showMessageDialog(frame, "存在路径未填写",

                    "警告", JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            if(!new File(file1).exists()||!new File(file2).exists()){
                JOptionPane.showMessageDialog(frame, "请检查路径文件是否存在",

                    "警告", JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            List<Object[]> data = compareData(file1, file2, jCheckBox.isSelected());
            Object[][] tableData = new Object[data.size()][];
            for (int i = 0, len = data.size(); i < len; i++) {
                tableData[i] = data.get(i);
            }
            DefaultTableModel tableModel2 = new DefaultTableModel(tableData, COLUMN_NAMES);
            TableFrame.showTable(tableModel2);
        });
        frame.setVisible(true);
    }

    private static List<Object[]> compareData(String file1, String file2, boolean hasThird) {
        JarConfig jarConfig = TxtUtil.readTxt(file1);
        JarConfig jarConfig11 = TxtUtil.readTxt(file2);

        Map<String, JarConfig> map1 = new HashMap<>();
        Map<String, JarConfig> map2 = new HashMap<>();
        dealMap(jarConfig, map1);
        dealMap(jarConfig11, map2);

        Set<String> allKeys = new HashSet<>(map1.keySet());
        allKeys.addAll(map2.keySet());

        allKeys = hasThird ? allKeys.stream().sorted().collect(Collectors.toCollection(
            LinkedHashSet::new)) : allKeys.stream().filter(key -> StringUtils.compare(key, "2") <= 0).sorted().collect(
            Collectors.toCollection(LinkedHashSet::new));
        List<Object[]> retList = new ArrayList<>();
        int s = 0;
        for (String key : allKeys) {
            JarConfig jar1 = map1.getOrDefault(key, JarConfig.builder().build());
            JarConfig jar2 = map2.getOrDefault(key, JarConfig.builder().build());
            if (!StringUtils.equals(jar1.getVersion(), jar2.getVersion()) && !StringUtils.isAnyBlank(
                jar1.getVersion(),
                jar2.getVersion()
            )) {
                s++;
                retList.add(new Object[]{s,
                    jar1.getGroupId(),
                    jar1.getArtifactId(),
                    jar1.getModule(),
                    jar1.getVersion(),
                    jar2.getModule(),
                    jar2.getVersion()});
            }
        }
        return retList;
    }

    private static void dealMap(JarConfig jarConfig, Map<String, JarConfig> map) {
        map.put(jarConfig.getId(), jarConfig);
        jarConfig.getNext().forEach(item -> dealMap(item, map));
    }
}

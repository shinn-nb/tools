package com.snn.tools.pomconfig.panel;

import com.snn.tools.pomconfig.entity.JarConfig;
import com.snn.tools.pomconfig.utils.TxtUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.invoker.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
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

    private static final String BUILD_TREE = "dependency:tree";
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
                List<Object[]> data = compareData(file1, file2, mvn.getText(), jCheckBox.isSelected());
                Object[][] tableData = new Object[data.size()][];
                for (int i = 0, len = data.size(); i < len; i++) {
                    tableData[i] = data.get(i);
                }
                DefaultTableModel tableModel2 = new DefaultTableModel(tableData, COLUMN_NAMES);
                TableFrame.showTable(tableModel2);
            } catch (Exception e2) {
                JOptionPane.showMessageDialog(frame, "生成失败,请查看输出的报错信息",
                    "错误", JOptionPane.ERROR_MESSAGE
                );
            } finally {
                loginButton.setText("比较");
                loginButton.setEnabled(true);
            }

        });
        frame.setVisible(true);
    }

    /**
     * @param mavenPath  maven系统路径 如D:\apache-maven-3.5.4
     * @param pomPath    要操控的pom文件的系统路径 如：D:\coding\**\pom.xml
     * @param mavenOrder maven命令如：clean
     */
    public static JarConfig operationMavenOrder(String mavenPath, String pomPath, String mavenOrder) {

        InvocationRequest request = new DefaultInvocationRequest();
        //想要操控的pom文件的位置
        request.setPomFile(new File(pomPath));
        //操控的maven命令
        request.setGoals(Collections.singletonList(mavenOrder));

        Invoker invoker = new DefaultInvoker();
        //maven的位置
        if (!StringUtils.isBlank(mavenPath)) {
            invoker.setMavenHome(new File(mavenPath));
        }

        TxtUtil util = new TxtUtil();
        invoker.setOutputHandler(line -> util.readTxt(line));
        try {
            int a = invoker.execute(request).getExitCode();
            if (a != 0) {
                throw new RuntimeException("maven命令执行失败");
            }
            return util.getJarConfig();
        } catch (
            MavenInvocationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static List<Object[]> compareData(String pomFile1, String pomFile2, String mavenPath, boolean hasThird) {

        JarConfig jarConfig = operationMavenOrder(
            mavenPath,
            pomFile1,
            BUILD_TREE
        );
        JarConfig jarConfig11 = operationMavenOrder(
            mavenPath,
            pomFile2,
            BUILD_TREE
        );

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

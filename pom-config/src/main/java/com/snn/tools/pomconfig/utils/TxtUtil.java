package com.snn.tools.pomconfig.utils;

import com.snn.tools.pomconfig.entity.JarConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author: shinn
 * @Date: 2021/9/13 上午11:52 （日期和时间）
 */
public class TxtUtil {

    private final String MODULE_SPLIT = "@";
    private final String JAR_START = "+-";
    private final String JAR_END = "\\-";
    private final String JAR_CONFIG_SPLIT = ":";
    private JarConfig jarFirst;
    private String module = null;

    JarConfig jarConfig = null;
    boolean nextModule = false;
    int lastReadNum = -1;

    public TxtUtil() {
        jarFirst = JarConfig.builder().number(-2).next(new ArrayList<>()).build();
    }

    public JarConfig readTxt(String tempStr) {
        if (StringUtils.contains(tempStr, MODULE_SPLIT) && StringUtils.contains(
            tempStr,
            "maven-dependency-plugin:"
        )) {
            module = StringUtils.remove(StringUtils.split(tempStr, MODULE_SPLIT)[1], " ---");
            jarConfig = JarConfig.builder().module(module).next(new ArrayList<>()).build();
            nextModule = true;
        }
        if (nextModule && StringUtils.containsAny(tempStr, ":jar:", ":pom:")) {
            String[] modules = StringUtils.split(tempStr, JAR_CONFIG_SPLIT);
            jarConfig.setPre(jarFirst);
            jarConfig.setGroupId(modules[0]);
            jarConfig.setModule(module);
            jarConfig.setArtifactId(modules[1]);
            jarConfig.setVersion(modules[3]);
            jarFirst.getNext().add(jarConfig);
            jarConfig.setNumber(-1);
            lastReadNum = -1;
            nextModule = false;
        }
        if (StringUtils.containsAny(tempStr, JAR_START, JAR_END)) {
            assert jarConfig != null;
            int num = getNum(tempStr);
            if (num == lastReadNum) {
                jarConfig = jarConfig.getPre();
            } else if (num < lastReadNum) {
                jarConfig = getPreJarByNum(jarConfig, num - 1);
            }
            jarConfig = dealNext(tempStr, jarConfig);
            lastReadNum = jarConfig.getNumber();
        }
        return jarFirst;
    }

    public JarConfig getJarConfig() {
        return jarFirst;
    }

    private JarConfig getPreJarByNum(JarConfig jarConfig, int num) {
        jarConfig = jarConfig.getPre();
        while (jarConfig.getNumber() != num && jarConfig.getPre() != null) {
            jarConfig = jarConfig.getPre();
        }
        return jarConfig;
    }

    private void dealJarDetail(String tempStr, JarConfig jarConfig) {
        String[] modules = StringUtils.split(tempStr, JAR_CONFIG_SPLIT);
        jarConfig.setGroupId(modules[0]);
        jarConfig.setArtifactId(modules[1]);
        jarConfig.setVersion(modules[3]);
    }

    private int getNum(String tempStr) {
        return tempStr.length() - StringUtils.replace(tempStr, "|", "").length();
    }

    private JarConfig dealNext(String tempStr, JarConfig preJarConfig) {
        String repStr = StringUtils.replace(tempStr, "|", "");
        int num = tempStr.length() - repStr.length();
        repStr = StringUtils.remove(repStr, " ");
        repStr = StringUtils.remove(repStr, JAR_START);
        repStr = StringUtils.remove(repStr, JAR_END);
        repStr = StringUtils.remove(repStr, "[INFO]");
        repStr = StringUtils.remove(repStr, "---");

        JarConfig jarConfig = JarConfig.builder()
            .module(preJarConfig.getModule())
            .next(new ArrayList<>())
            .number(num)
            .build();

        jarConfig.setPre(preJarConfig);
        dealJarDetail(repStr, jarConfig);
        preJarConfig.getNext().add(jarConfig);
        return jarConfig;
    }

}

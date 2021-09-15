package com.snn.tools.pomconfig.utils;

import com.snn.tools.pomconfig.entity.JarConfig;
import com.snn.tools.pomconfig.exception.MavenException;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: shinn
 * @Date: 2021/9/15 上午9:24 （日期和时间）
 */
public class PomDataUtil {
    private PomDataUtil() {}

    private static final String BUILD_TREE = "dependency:tree";

    public static List<Object[]> compareData(
        String pomFile1,
        String pomFile2,
        String mavenPath,
        boolean hasThird
    ) throws
        MavenInvocationException,
        MavenException {

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

    /**
     * @param mavenPath  maven系统路径 如D:\apache-maven-3.5.4
     * @param pomPath    要操控的pom文件的系统路径 如：D:\coding\**\pom.xml
     * @param mavenOrder maven命令如：clean
     */
    public static JarConfig operationMavenOrder(String mavenPath, String pomPath, String mavenOrder) throws
        MavenException, MavenInvocationException {

        mavenPath = StringUtils.trim(mavenPath);
        pomPath = StringUtils.trim(pomPath);
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
        invoker.setOutputHandler(util::readTxt);

        int a = invoker.execute(request).getExitCode();
        if (a != 0) {
            throw new MavenException("maven命令执行失败");
        }
        return util.getJarConfig();

    }

    private static void dealMap(JarConfig jarConfig, Map<String, JarConfig> map) {
        map.put(jarConfig.getId(), jarConfig);
        jarConfig.getNext().forEach(item -> dealMap(item, map));
    }
}

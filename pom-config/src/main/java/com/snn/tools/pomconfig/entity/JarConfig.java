package com.snn.tools.pomconfig.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author: shinn
 * @Date: 2021/9/13 上午11:59 （日期和时间）
 */
@Data
@Builder
public class JarConfig {
    private String module;
    private String version;
    private String groupId;
    private String artifactId;
    private JarConfig pre;
    private List<JarConfig> next;
    private String id;
    private int number;
    public String getId() {
        return number+";"+groupId + ";" + artifactId+"；";
    }
}

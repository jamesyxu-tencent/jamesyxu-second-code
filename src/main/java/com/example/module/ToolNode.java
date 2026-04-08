package com.example.module;

import lombok.Data;
import lombok.ToString;

import java.util.*;

/**
 * 工具调用节点
 */
@Data
@ToString
public class ToolNode {

    private String toolName;

    private Map<String, Object> parameters;

    private List<ToolNode> dependencies = new ArrayList<>();

    private String resultKey;  // 结果存储的key

    public ToolNode(String toolName, Map<String, Object> parameters) {
        this.toolName = toolName;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.resultKey = toolName;
    }

    public ToolNode(String toolName, Map<String, Object> parameters, String resultKey) {
        this.toolName = toolName;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.resultKey = resultKey;
    }

    public void addDependency(ToolNode dependency) {
        this.dependencies.add(dependency);
    }

    public void addDependencies(ToolNode... dependencies) {
        this.dependencies.addAll(Arrays.asList(dependencies));
    }

}

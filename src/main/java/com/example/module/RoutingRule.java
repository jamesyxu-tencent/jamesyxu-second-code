package com.example.module;

import lombok.Data;

import java.util.regex.Pattern;

/**
 * 路由规则类
 */
@Data
public class RoutingRule {
    private String name;           // 规则名称
    private Pattern pattern;        // 匹配模式（正则）
    private String model;           // 目标模型
    private int priority;           // 优先级（数字越小优先级越高）
    private String description;     // 规则描述

    public RoutingRule(String name, String pattern, String model, int priority, String description) {
        this.name = name;
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        this.model = model;
        this.priority = priority;
        this.description = description;
    }

    public boolean matches(String question) {
        return pattern.matcher(question).find();
    }

}

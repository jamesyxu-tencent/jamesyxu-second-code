package com.example.service.impl;

import com.example.module.RoutingRule;
import com.example.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ModelRouterService {

    @Autowired
    private IChatService chatService;

    // 路由规则配置
    private final List<RoutingRule> routingRules = new ArrayList<>();

    public ModelRouterService() {
        // 初始化路由规则
        initRoutingRules();
    }

    /**
     * 初始化路由规则
     */
    private void initRoutingRules() {
        // 高质量任务 -> 使用 qwen-plus
        routingRules.add(new RoutingRule(
                "复杂推理",
                "为什么|如何实现|原理|区别|对比|优缺点|设计模式|架构",
                "qwen-plus",
                1,
                "复杂推理问题使用qwen-plus"
        ));

        routingRules.add(new RoutingRule(
                "代码生成",
                "写一段代码|实现一个|编程|开发|debug|调试|算法",
                "qwen-plus",
                1,
                "代码相关使用qwen-plus"
        ));

        routingRules.add(new RoutingRule(
                "长文本处理",
                "总结|摘要|概括|分析文章|长文档",
                "qwen-plus",
                1,
                "长文本处理使用qwen-plus"
        ));

        // 简单任务 -> 使用 qwen-turbo（快速）
        routingRules.add(new RoutingRule(
                "简单问答",
                "什么是|介绍一下|你好|帮助|谢谢|定义",
                "qwen-turbo",
                2,
                "简单问答使用qwen-turbo"
        ));

        routingRules.add(new RoutingRule(
                "日常对话",
                "今天天气|吃饭|你好吗|名字|功能",
                "qwen-turbo",
                2,
                "日常对话使用qwen-turbo"
        ));

        // 按长度规则
        routingRules.add(new RoutingRule(
                "短问题",
                "^.{0,20}$",  // 20字以内的问题
                "qwen-turbo",
                3,
                "短问题使用qwen-turbo"
        ));

        routingRules.add(new RoutingRule(
                "长问题",
                "^.{100,}$",  // 100字以上的问题
                "qwen-plus",
                3,
                "长问题使用qwen-plus"
        ));
    }

    /**
     * 根据问题路由到合适的模型
     */
    public String routeModel(String question) {
        // 按优先级排序的规则
        List<RoutingRule> sortedRules = new ArrayList<>(routingRules);
        sortedRules.sort(Comparator.comparingInt(RoutingRule::getPriority));

        // 查找匹配的规则
        for (RoutingRule rule : sortedRules) {
            if (rule.matches(question)) {
                System.out.println("路由规则匹配: " + rule.getName() + " -> " + rule.getModel());
                return rule.getModel();
            }
        }

        // 默认使用配置的默认模型
        return "qwen-turbo";
    }

    /**
     * 带路由的问答
     */
    public String askWithRouting(String question) throws Exception {
        String model = routeModel(question);
        return chatService.chat(question, model);
    }

    /**
     * 获取路由决策信息
     */
    public Map<String, Object> getRoutingDecision(String question) {
        Map<String, Object> decision = new HashMap<>();
        decision.put("question", question);
        decision.put("question_length", question.length());

        String selectedModel = routeModel(question);
        decision.put("selected_model", selectedModel);

        // 记录所有匹配的规则
        List<Map<String, Object>> matchedRules = new ArrayList<>();
        for (RoutingRule rule : routingRules) {
            if (rule.matches(question)) {
                Map<String, Object> ruleInfo = new HashMap<>();
                ruleInfo.put("name", rule.getName());
                ruleInfo.put("model", rule.getModel());
                ruleInfo.put("priority", rule.getPriority());
                ruleInfo.put("description", rule.getDescription());
                matchedRules.add(ruleInfo);
            }
        }

        // 按优先级排序
        matchedRules.sort((a, b) ->
                Integer.compare((int)a.get("priority"), (int)b.get("priority")));

        decision.put("matched_rules", matchedRules);
        decision.put("matched_count", matchedRules.size());

        return decision;
    }

    /**
     * 动态添加规则
     */
    public void addRule(String name, String pattern, String model, int priority, String description) {
        routingRules.add(new RoutingRule(name, pattern, model, priority, description));
    }

    /**
     * 获取所有规则
     */
    public List<Map<String, Object>> getAllRules() {
        List<Map<String, Object>> rules = new ArrayList<>();
        for (RoutingRule rule : routingRules) {
            Map<String, Object> ruleInfo = new HashMap<>();
            ruleInfo.put("name", rule.getName());
            ruleInfo.put("pattern", rule.getPattern().toString());
            ruleInfo.put("model", rule.getModel());
            ruleInfo.put("priority", rule.getPriority());
            ruleInfo.put("description", rule.getDescription());
            rules.add(ruleInfo);
        }
        return rules;
    }

}
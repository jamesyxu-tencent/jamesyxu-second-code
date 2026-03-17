package com.example.controller;

import com.example.service.IChatService;
import com.example.service.impl.ModelRouterService;
import com.example.vo.base.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/router")
public class ModelRouterController {

    @Autowired
    private ModelRouterService routerService;

    @Autowired
    private IChatService chatService;

    /**
     * 智能路由问答
     */
    @GetMapping("/ask")
    public ApiResult<Map<String, Object>> askWithRouting(@RequestParam String question) {
        Map<String, Object> result = new HashMap<>();

        try {
            long startTime = System.currentTimeMillis();

            // 获取路由决策
            Map<String, Object> decision = routerService.getRoutingDecision(question);
            String selectedModel = (String) decision.get("selected_model");

            // 调用对应模型
            // 注意：这里需要注入DashScopeService，为简化示例，先注释
             String answer = chatService.chat(question, selectedModel);
//            String answer = "[模拟] 使用 " + selectedModel + " 回答问题：" + question;

            long endTime = System.currentTimeMillis();

            result.put("success", true);
            result.put("question", question);
            result.put("answer", answer);
            result.put("selected_model", selectedModel);
            result.put("routing_decision", decision);
            result.put("duration_ms", endTime - startTime);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return ApiResult.success(result);
    }

    /**
     * 查看路由决策（不实际调用API）
     */
    @GetMapping("/decision")
    public ApiResult<Map<String, Object>> getRoutingDecision(@RequestParam String question) {
        return ApiResult.success(routerService.getRoutingDecision(question));
    }

    /**
     * 获取所有路由规则
     */
    @GetMapping("/rules")
    public ApiResult<List<Map<String, Object>>> getAllRules() {
        return ApiResult.success(routerService.getAllRules());
    }

    /**
     * 动态添加路由规则
     */
    @PostMapping("/rules")
    public ApiResult<Map<String, Object>> addRule(
            @RequestParam String name,
            @RequestParam String pattern,
            @RequestParam String model,
            @RequestParam int priority,
            @RequestParam String description) {

        Map<String, Object> result = new HashMap<>();
        try {
            routerService.addRule(name, pattern, model, priority, description);
            result.put("success", true);
            result.put("message", "规则添加成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ApiResult.success(result);
    }
}
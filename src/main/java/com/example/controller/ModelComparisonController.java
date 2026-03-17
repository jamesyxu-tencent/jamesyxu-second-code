package com.example.controller;

import com.example.module.ComparisonResult;
import com.example.service.IChatService;
import com.example.service.impl.ModelComparisonService;
import com.example.vo.base.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/compare")
public class ModelComparisonController {

    @Autowired
    private ModelComparisonService comparisonService;

    @Autowired
    private IChatService chatService;

    /**
     * 并发对比多个模型
     */
    @GetMapping("/models")
    public ApiResult<Map<String, Object>> compareModels(
            @RequestParam String question,
            @RequestParam(defaultValue = "qwen-turbo,qwen-plus") String models) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<String> modelList = Arrays.asList(models.split(","));

            ComparisonResult comparisonResult =
                    comparisonService.compareModels(question, modelList);

            Map<String, Object> report = comparisonService.generateReport(comparisonResult);

            result.put("success", true);
            result.put("comparison", report);

            // 包含详细回答（可选，避免响应过大）
            boolean includeAnswers = false; // 可以改为从请求参数获取
            if (includeAnswers) {
                result.put("detailed_responses", comparisonResult.getResponses());
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return ApiResult.success(result);
    }

    /**
     * 简单速度测试
     */
    @GetMapping("/speed-test")
    public ApiResult<Map<String, Object>> speedTest(@RequestParam(defaultValue = "你好，请简单介绍一下自己") String question) {
        Map<String, Object> result = new HashMap<>();

        List<String> models = Arrays.asList("qwen-turbo", "qwen-plus");
        Map<String, Long> speeds = new HashMap<>();

        for (String model : models) {
            try {
                long start = System.currentTimeMillis();
                chatService.chat(question, model);
                long duration = System.currentTimeMillis() - start;
                speeds.put(model, duration);
            } catch (Exception e) {
                speeds.put(model, -1L);
            }
        }

        result.put("question", question);
        result.put("speeds_ms", speeds);

        // 计算比例
        if (speeds.get("qwen-turbo") > 0 && speeds.get("qwen-plus") > 0) {
            double ratio = (double) speeds.get("qwen-plus") / speeds.get("qwen-turbo");
            result.put("plus_vs_turbo_ratio", String.format("%.2f", ratio));
            if (ratio > 1.5) {
                result.put("analysis", "qwen-plus 明显慢于 qwen-turbo");
            } else if (ratio < 0.8) {
                result.put("analysis", "qwen-plus 比预期快");
            } else {
                result.put("analysis", "两者速度相近");
            }
        }

        return ApiResult.success(result);
    }
}
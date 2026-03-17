package com.example.service.impl;

import com.example.module.ComparisonResult;
import com.example.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class ModelComparisonService {

    @Autowired
    private IChatService chatService;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * 并发对比多个模型
     */
    public ComparisonResult compareModels(String question, List<String> models)
            throws InterruptedException, ExecutionException {

        long startTime = System.currentTimeMillis();
        ComparisonResult result = new ComparisonResult();
        result.setQuestion(question);

        List<Callable<ComparisonResult.ModelResponse>> tasks = new ArrayList<>();

        for (String model : models) {
            tasks.add(() -> {
                ComparisonResult.ModelResponse response = new ComparisonResult.ModelResponse();
                response.setModel(model);

                long modelStartTime = System.currentTimeMillis();
                try {
                    String answer = chatService.chat(question, model);
                    response.setAnswer(answer);
                    response.setSuccess(true);
                } catch (Exception e) {
                    response.setSuccess(false);
                    response.setError(e.getMessage());
                }

                response.setDurationMs(System.currentTimeMillis() - modelStartTime);
                return response;
            });
        }

        // 并发执行
        List<Future<ComparisonResult.ModelResponse>> futures = executor.invokeAll(tasks);

        Map<String, ComparisonResult.ModelResponse> responses = new HashMap<>();
        for (Future<ComparisonResult.ModelResponse> future : futures) {
            ComparisonResult.ModelResponse response = future.get();
            responses.put(response.getModel(), response);
        }

        result.setResponses(responses);
        result.setTotalTimeMs(System.currentTimeMillis() - startTime);

        // 推荐模型（可以根据策略选择）
        result.setRecommendedModel(result.getFastestModel()); // 简单策略：推荐最快的

        return result;
    }

    /**
     * 生成对比报告
     */
    public Map<String, Object> generateReport(ComparisonResult result) {
        Map<String, Object> report = new HashMap<>();
        report.put("question", result.getQuestion());
        report.put("total_time_ms", result.getTotalTimeMs());
        report.put("recommended_model", result.getRecommendedModel());
        report.put("fastest_model", result.getFastestModel());
        report.put("most_detailed_model", result.getMostDetailedModel());

        List<Map<String, Object>> modelDetails = new ArrayList<>();
        for (Map.Entry<String, ComparisonResult.ModelResponse> entry : result.getResponses().entrySet()) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("model", entry.getKey());
            detail.put("success", entry.getValue().isSuccess());
            detail.put("duration_ms", entry.getValue().getDurationMs());
            detail.put("answer_length", entry.getValue().getAnswerLength());

            if (!entry.getValue().isSuccess()) {
                detail.put("error", entry.getValue().getError());
            }

            // 速度评级
            if (entry.getValue().getDurationMs() < 1000) {
                detail.put("speed_rating", "⚡ 极快");
            } else if (entry.getValue().getDurationMs() < 3000) {
                detail.put("speed_rating", "🚀 快");
            } else if (entry.getValue().getDurationMs() < 5000) {
                detail.put("speed_rating", "⏱️ 中等");
            } else {
                detail.put("speed_rating", "🐢 慢");
            }

            modelDetails.add(detail);
        }

        // 按速度排序
        modelDetails.sort(Comparator.comparingLong(m -> (Long) m.get("duration_ms")));

        report.put("model_details", modelDetails);

        return report;
    }
}
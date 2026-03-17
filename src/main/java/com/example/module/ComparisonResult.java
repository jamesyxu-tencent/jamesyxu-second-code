package com.example.module;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 模型对比结果
 */
public class ComparisonResult {
    private String question;
    private Map<String, ModelResponse> responses = new HashMap<>();
    private String recommendedModel;
    private long totalTimeMs;

    public static class ModelResponse {
        private String model;
        private String answer;
        private long durationMs;
        private boolean success;
        private String error;
        private int answerLength;

        // getters and setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) {
            this.answer = answer;
            this.answerLength = answer != null ? answer.length() : 0;
        }

        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public int getAnswerLength() { return answerLength; }
    }

    // getters and setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public Map<String, ModelResponse> getResponses() { return responses; }
    public void setResponses(Map<String, ModelResponse> responses) { this.responses = responses; }

    public String getRecommendedModel() { return recommendedModel; }
    public void setRecommendedModel(String recommendedModel) { this.recommendedModel = recommendedModel; }

    public long getTotalTimeMs() { return totalTimeMs; }
    public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }

    /**
     * 获取最快的模型
     */
    public String getFastestModel() {
        return responses.entrySet().stream()
                .filter(e -> e.getValue().isSuccess())
                .min(Comparator.comparingLong(e -> e.getValue().getDurationMs()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 获取回答最长的模型（粗略代表详细程度）
     */
    public String getMostDetailedModel() {
        return responses.entrySet().stream()
                .filter(e -> e.getValue().isSuccess())
                .max(Comparator.comparingInt(e -> e.getValue().getAnswerLength()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}

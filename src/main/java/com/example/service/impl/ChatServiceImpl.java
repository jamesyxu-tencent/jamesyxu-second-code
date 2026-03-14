package com.example.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import com.example.config.ChatAiConfig;
import com.example.dto.CrispeRequestDTO;
import com.example.service.IChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatServiceImpl implements IChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Map<String, List<Map<String, String>>> conversationHistoryMap = new ConcurrentHashMap<>();

    @Resource
    private ChatAiConfig chatAiConfig;

    @Override
    public String chat(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-turbo");
            requestBody.put("stream", false);
            requestBody.put("messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
            });

            HttpRequest request = getHttpRequest(requestBody);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode jsonNode = OBJECT_MAPPER.readTree(response.body());

            if (jsonNode.has("choices") && jsonNode.get("choices").isArray() &&
                    jsonNode.get("choices").size() > 0) {
                return jsonNode.get("choices").get(0).get("message").get("content").asText();
            }
        } catch (Exception e) {
            return "Hello ! 欢迎进入 AI 开发世界！(AI 服务暂时不可用))";
        }
        return "Hello ! 欢迎进入 AI 开发世界！(AI 服务暂时不可用))";
    }

    @Override
    public SseEmitter chatStream(String message, String sessionId) {

        log.info("用户提问：{}，sessionId：{}", message, sessionId);

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        String finalSessionId = sessionId;
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            try {
                List<Map<String, String>> conversationHistory = conversationHistoryMap.computeIfAbsent(finalSessionId, k -> new ArrayList<>());

                conversationHistory.add(Map.of("role", "user", "content", message));

                HttpClient client = HttpClient.newHttpClient();

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "qwen-turbo");
                requestBody.put("stream", true);
                requestBody.put("messages", conversationHistory.toArray(new Object[0]));

                HttpRequest request = getHttpRequest(requestBody);

                StringBuilder fullResponse = new StringBuilder();

                client.send(request, HttpResponse.BodyHandlers.ofLines()).body().forEach(line -> {
                    try {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data.trim())) {
                                emitter.send("[DONE]");
                                emitter.complete();
                            } else {
                                JsonNode jsonNode = OBJECT_MAPPER.readTree(data);
                                if (jsonNode.has("choices") && jsonNode.get("choices").isArray() &&
                                        jsonNode.get("choices").size() > 0) {
                                    JsonNode delta = jsonNode.get("choices").get(0).get("delta");
                                    if (delta != null && delta.has("content")) {
                                        String content = delta.get("content").asText();
                                        if (content != null && !content.isEmpty()) {
                                            emitter.send(content);
                                            fullResponse.append(content);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    } catch (Exception e) {
                        try {
                            emitter.send("处理响应出错：" + e.getMessage());
                        } catch (IOException ex) {
                            emitter.completeWithError(ex);
                        }
                    }
                });

                if (fullResponse.length() > 0) {
                    conversationHistory.add(Map.of("role", "assistant", "content", fullResponse.toString()));
                }

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Override
    public Map<String, String> clearConversation(String sessionId) {
        Map<String, String> result = new HashMap<>();
        if (sessionId != null && !sessionId.isEmpty()) {
            conversationHistoryMap.remove(sessionId);
            result.put("status", "success");
            result.put("message", "已清空会话历史");
        } else {
            result.put("status", "error");
            result.put("message", "请提供 sessionId");
        }
        return result;
    }

    @Override
    public String chatWithRole(String question, String role) {
        // 构建系统提示词模板
        String systemPrompt = String.format("""
                你是一位%s。请以这个身份回答此问题：%s
                你的回答应该：
                1. 符合该角色的专业背景
                2. 使用该角色的语气和表达方式
                3. 如果问题超出专业范围，礼貌说明并建议咨询专业人士
                """, role, question);
        return this.chat(systemPrompt);
    }

    @Override
    public String chatWithCrispe(CrispeRequestDTO dto) {
        // 构建系统提示词模板
        String systemPrompt = askWithCRISPE(dto);
        return this.chat(systemPrompt);
    }

    /**
     * HttpRequest
     *
     * @param requestBody
     * @return
     */
    private HttpRequest getHttpRequest(Map<String, Object> requestBody) {
        try {
            return HttpRequest.newBuilder()
                    .uri(URI.create(chatAiConfig.getChatUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + chatAiConfig.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(OBJECT_MAPPER.writeValueAsBytes(requestBody)))
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Request Error");
        }
        return null;
    }

    /**
     * 使用CRISPE框架的完整提示词
     */
    public String askWithCRISPE(CrispeRequestDTO dto) {
        return String.format("""
                【角色】%s
                【背景】%s
                【指令】请回答以下问题：%s
                【风格】%s
                【实验】%s
                """, dto.getRole(), dto.getBackground(), dto.getQuestion(), dto.getStyle(), dto.getExperiment());
    }

}

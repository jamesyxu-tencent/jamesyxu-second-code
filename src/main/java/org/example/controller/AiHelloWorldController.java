package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class AiHelloWorldController {

    private static final String DASHSCOPE_API_KEY = "my-app-keys";
    private static final String DASHSCOPE_CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GetMapping("/api/hello")
    public String showHelloPage() {
        return "欢迎访问 AI 应用！请访问 /index.html 或使用 /api/sayHello 接口";
    }

    @GetMapping("/api/sayHello")
    public String sayHello(@RequestParam(value = "name", defaultValue = "World") String name) {
        if (DASHSCOPE_API_KEY == null || DASHSCOPE_API_KEY.isEmpty() || "sk-your-dashscope-api-key".equals(DASHSCOPE_API_KEY)) {
            return "Hello " + name + "! 欢迎进入 AI 开发世界！(请先配置通义千问 API Key)";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-turbo");
            requestBody.put("stream", false);
            requestBody.put("messages", new Object[] {
                    Map.of("role", "user", "content", "用一句话欢迎用户：" + name)
            });

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DASHSCOPE_CHAT_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + DASHSCOPE_API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(OBJECT_MAPPER.writeValueAsBytes(requestBody)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode jsonNode = OBJECT_MAPPER.readTree(response.body());

            if (jsonNode.has("choices") && jsonNode.get("choices").isArray() &&
                    jsonNode.get("choices").size() > 0) {
                return jsonNode.get("choices").get(0).get("message").get("content").asText();
            }

            return "Hello " + name + "! 欢迎进入 AI 开发世界！";
        } catch (Exception e) {
            return "Hello " + name + "! 欢迎进入 AI 开发世界！(AI 服务暂时不可用)";
        }
    }

    @GetMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam(value = "message", defaultValue = "你好") String message) {
        SseEmitter emitter = new SseEmitter(0L);

        if (DASHSCOPE_API_KEY == null || DASHSCOPE_API_KEY.isEmpty() || "sk-your-dashscope-api-key".equals(DASHSCOPE_API_KEY)) {
            try {
                emitter.send("请先配置通义千问 API Key");
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "qwen-turbo");
                requestBody.put("stream", true);
                requestBody.put("messages", new Object[] {
                        Map.of("role", "user", "content", message)
                });

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(DASHSCOPE_CHAT_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + DASHSCOPE_API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(OBJECT_MAPPER.writeValueAsBytes(requestBody)))
                        .build();

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

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

}

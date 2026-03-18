package com.example.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ollama")
public class OllamaController {

    @Autowired
    private ChatClient chatClient;

    /**
     * 基础问答
     * 测试：GET /ollama/chat?msg=你好
     */
    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam("msg") String message) {
        Map<String, Object> result = new HashMap<>();

        try {
            long startTime = System.currentTimeMillis();

            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("message", message);
            result.put("response", response);
            result.put("duration_ms", duration);
            result.put("model", "qwen2.5:7b-instruct (本地)");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 带系统提示词的问答
     * POST /ollama/system
     * Body: 用户问题
     */
    @PostMapping("/system")
    public Map<String, Object> chatWithSystem(
            @RequestParam(required = false, defaultValue = "你是一个专业的AI助手，请用中文回答") String systemPrompt,
            @RequestBody String userMessage) {

        Map<String, Object> result = new HashMap<>();

        try {
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();

            result.put("success", true);
            result.put("system_prompt", systemPrompt);
            result.put("user_message", userMessage);
            result.put("response", response);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
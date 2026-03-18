package com.example.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ollama/memory")
public class OllamaMemoryController {

    @Autowired
    private ChatClient chatClient;

    // 内存存储（生产环境可用Redis）
    private final ChatMemory chatMemory = new InMemoryChatMemory();

    /**
     * 带记忆的对话（需要sessionId标识用户）
     */
    @GetMapping("/chat")
    public Map<String, Object> chatWithMemory(
            @RequestParam String sessionId,
            @RequestParam String message) {

        Map<String, Object> result = new HashMap<>();

        try {
            String response = chatClient.prompt()
                    .system("你是一个友好的AI助手，记住对话历史，保持上下文连贯")
                    .user(message)
                    .advisors(new MessageChatMemoryAdvisor(chatMemory, sessionId, 10)) // 保留最近10条
                    .call()
                    .content();

            result.put("success", true);
            result.put("session_id", sessionId);
            result.put("message", message);
            result.put("response", response);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 清除记忆
     */
    @DeleteMapping("/clear")
    public Map<String, Object> clearMemory(@RequestParam String sessionId) {
        chatMemory.clear(sessionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "记忆已清除");
        return result;
    }
}
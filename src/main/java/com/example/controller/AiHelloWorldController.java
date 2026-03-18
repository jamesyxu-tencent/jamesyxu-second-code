package com.example.controller;

import jakarta.annotation.Resource;
import com.example.service.IChatService;
import com.example.vo.base.ApiResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
public class AiHelloWorldController {

    @Resource
    private IChatService chatService;

    @Autowired
    private ChatClient chatClient;

    @GetMapping("api/free-test")
    public ApiResult<String> test() {
        return ApiResult.success(chatClient.prompt()
                .user("你好，Spring AI 要钱吗？")
                .call()
                .content());
    }

    @GetMapping("/api/hello")
    public ApiResult<String> showHelloPage() {
        return ApiResult.success("欢迎访问 AI 应用！请访问 /index.html 或使用 /api/sayHello 接口");
    }

    @GetMapping("/api/sayHello")
    public ApiResult<String> sayHello(@RequestParam(value = "question", defaultValue = "今天天气怎么样？") String question,
                                      @RequestParam(value = "model", defaultValue = "qwen-turbo") String model) {
        String result = chatService.chat(question, model);
        return ApiResult.success(result);
    }

    @GetMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam(value = "message", defaultValue = "你好") String message,
                                 @RequestParam(value = "sessionId", required = false) String sessionId) {
        return chatService.chatStream(message, sessionId);
    }

    @GetMapping("/api/chat/clear")
    public ApiResult<Map<String, String>> clearConversation(@RequestParam(value = "sessionId", required = false) String sessionId) {
        return ApiResult.success(chatService.clearConversation(sessionId));
    }

}
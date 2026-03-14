package org.example.controller;

import jakarta.annotation.Resource;
import org.example.service.IChatService;
import org.example.vo.base.ApiResult;
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

    @GetMapping("/api/hello")
    public ApiResult<String> showHelloPage() {
        return ApiResult.success("欢迎访问 AI 应用！请访问 /index.html 或使用 /api/sayHello 接口");
    }

    @GetMapping("/api/sayHello")
    public ApiResult<String> sayHello(@RequestParam(value = "question", defaultValue = "今天天气怎么样？") String question) {
        String result = chatService.chat(question);
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
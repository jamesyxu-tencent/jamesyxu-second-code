package com.example.controller.ollama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ollama/stream")
public class OllamaStreamController {

    @Autowired
    @Qualifier("ollamaChatClient")
    private ChatClient ollamaChatClient;  // 本地模型

    /**
     * 流式输出
     * 访问：GET /ollama/stream/chat?msg=讲一个程序员笑话
     * 返回格式：text/event-stream (SSE)
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam("msg") String message) {
        return ollamaChatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    /**
     * 带系统提示词的流式输出
     */
    @GetMapping(value = "/system", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamWithSystem(
            @RequestParam String msg,
            @RequestParam(required = false, defaultValue = "你是一个幽默的程序员") String system) {
        return ollamaChatClient.prompt()
                .system(system)
                .user(msg)
                .stream()
                .content();
    }
}
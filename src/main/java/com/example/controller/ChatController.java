//package org.example.controller;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class ChatController {
//
//    private final ChatClient chatClient;
//
//    // 构造器注入ChatClient
//    public ChatController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }
//
//    @GetMapping("/ai/chat")
//    public String chat(@RequestParam(value = "message", defaultValue = "你好") String message) {
//        return chatClient.prompt()
//                .user(message)           // 传入用户消息
//                .call()                   // 调用模型
//                .content();                // 获取返回内容
//    }
//
////    @GetMapping("/ai/chat")
////    public String chat(@RequestParam String message) {
////        return chatClient.prompt()
////                .system("你是一个专业的Java技术顾问，请用简洁专业的语言回答问题。")
////                .user(message)
////                .call()
////                .content();
////    }
//}
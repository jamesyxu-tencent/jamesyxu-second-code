package com.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

//    @Bean
//    public ChatClient chatClient(ChatClient.Builder builder) {
//        return builder.build();  // 使用默认配置
//    }

    @Bean
    @ConditionalOnProperty(name = "app.model", havingValue = "ollama")
    public ChatClient ollamaChatClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(OllamaOptions.builder()
                        .model("llama3.2:3b")
                        .temperature(0.8)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.model", havingValue = "qwen")
    public ChatClient qwenChatClient(ChatClient.Builder builder) {
        // ✅ 正确方式：使用builder()创建
        OllamaOptions options = OllamaOptions.builder()
                .model("qwen2.5:7b-instruct")
                .temperature(0.7)
                .topP(0.9)
                .numPredict(2048)
                .build();

        return builder
                .defaultOptions(options)
                .build();
    }

}
package com.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 修复：注入OllamaChatModel而非ChatClient.Builder，手动创建Builder
 */
@Configuration
public class OllamaConfig {

    /**
     * 当 app.model=ollama 时，创建llama3.2模型的ChatClient
     * @param ollamaChatModel Spring AI自动装配的OllamaChatModel（核心：注入具体的ChatModel）
     * @return 绑定llama3.2的ChatClient
     */
    @Bean
    @ConditionalOnProperty(name = "app.model", havingValue = "ollama")
    @Primary // 当匹配时作为默认ChatClient
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        // 正确方式：通过ChatClient.builder(chatModel)创建Builder
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(OllamaOptions.builder()
                        .model("llama3.2:3b")
                        .temperature(0.8)
                        .build())
                .build();
    }

    /**
     * 当 app.model=qwen 时，创建通义千问模型的ChatClient
     * @param ollamaChatModel Spring AI自动装配的OllamaChatModel
     * @return 绑定qwen2.5的ChatClient
     */
    @Bean
    @ConditionalOnProperty(name = "app.model", havingValue = "qwen")
    @Primary // 当匹配时作为默认ChatClient
    public ChatClient qwenChatClient(OllamaChatModel ollamaChatModel) {
        OllamaOptions options = OllamaOptions.builder()
                .model("qwen2.5:1.5b-instruct")
                .temperature(0.7)
                .topP(0.9)
                .numPredict(2048)
                .build();

        // 核心修正：基于注入的ChatModel创建Builder
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(options)
                .build();
    }
}
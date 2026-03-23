package com.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

    // 从配置文件读取默认模型类型
    @Value("${spring.ai.default-model:cloud}") // 默认使用云端
    private String defaultModel;

    /**
     * 本地Ollama的ChatClient
     * @param ollamaChatModel Spring AI自动装配的Ollama模型实例
     * @return 本地ChatClient
     */
    @Bean("localChatClient") // 命名为localChatClient，方便注入
    public ChatClient localChatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }

    /**
     * 云端OpenAI的ChatClient
     * @param openAiChatModel Spring AI自动装配的OpenAI模型实例
     * @return 云端ChatClient
     */
    @Bean("cloudChatClient") // 命名为cloudChatClient，方便注入
    @Primary // 可选：标记默认优先使用的ChatClient（如果业务代码不指定@Qualifier时）
    public ChatClient cloudChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    /**
     * 默认ChatClient（根据配置文件动态切换）
     */
    @Bean
    @Primary
    public ChatClient defaultChatClient(
            @Qualifier("localChatClient") ChatClient localChatClient,
            @Qualifier("cloudChatClient") ChatClient cloudChatClient) {
        return "local".equals(defaultModel) ? localChatClient : cloudChatClient;
    }

}
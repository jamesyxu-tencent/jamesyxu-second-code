package com.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
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

    // 通义千问基础配置
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    /**
     * 本地Ollama的ChatClient
     * @param ollamaChatModel Spring AI自动装配的Ollama模型实例
     * @return 本地ChatClient
     */
    @Bean("ollamaChatClient") // 命名为localChatClient，方便注入
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }

    /**
     * 云端OpenAI的ChatClient
     * @param openAiChatModel Spring AI自动装配的OpenAI模型实例
     * @return 云端ChatClient
     */
    @Bean("qwenTurboChatClient") // 命名为qwenTurboChatClient，方便注入
    @Primary // 可选：标记默认优先使用的ChatClient（如果业务代码不指定@Qualifier时）
    public ChatClient qwenTurboChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    @Bean("qwenPlusChatClient")
    public ChatClient qwenPlusChatClient() {
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, OpenAiChatOptions.builder()
                .model("qwen-plus")  // 关键：这里换成 plus
                .temperature(0.7)
                .maxTokens(2000)
                .topP(0.9)
                .build());
        return ChatClient.create(chatModel);
    }

    /**
     * 默认ChatClient（根据配置文件动态切换）
     */
    @Bean
    @Primary
    public ChatClient defaultChatClient(
            @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
            @Qualifier("qwenTurboChatClient") ChatClient qwenTurboChatClient) {
        return "local".equals(defaultModel) ? ollamaChatClient : qwenTurboChatClient;
    }

}
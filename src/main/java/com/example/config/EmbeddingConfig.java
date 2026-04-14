package com.example.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.model:text-embedding-v2}")
    private String model;

    @Bean
    public OpenAiEmbeddingModel openAiEmbeddingModel() {
        // 通义千问 OpenAI 兼容地址
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);

        // 通义千问向量模型
        return new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model(model)  // 通义向量模型
                        .build()
        );
    }
}
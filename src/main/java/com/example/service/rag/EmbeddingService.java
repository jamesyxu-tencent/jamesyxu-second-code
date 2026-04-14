package com.example.service.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    // 注入你手动配置的 通义千问 EmbeddingModel
    @Resource
    private EmbeddingModel embeddingModel;

    // 注入 Chroma 向量库
    @Resource
    private VectorStore vectorStore;

    // ======================
    // 1. 单个文本向量化
    // ======================
    public float[] embedText(String text) {
        return embeddingModel.embed(text);
    }

    // ======================
    // 2. 批量文本向量化
    // ======================
    public EmbeddingResponse embedBatch(List<String> texts) {
        EmbeddingRequest request = new EmbeddingRequest(texts, null);
        return embeddingModel.call(request);
    }

    // ======================
    // 3. 把文本存入向量库（最常用）
    // ======================
    public void addToVectorStore(String content) {
        Document document = new Document(content);
        vectorStore.add(List.of(document));
    }

    // ======================
    // 4. 批量存入向量库
    // ======================
    public void addBatchToVectorStore(List<String> contents) {
        List<Document> documents = contents.stream()
                .map(Document::new)
                .toList();
        vectorStore.add(documents);
    }

    // ======================
    // 5. 带元数据存入（更实用）
    // ======================
    public void addDocWithMetadata(String content, Map<String, Object> metadata) {
        Document document = new Document(content, metadata);
        vectorStore.add(List.of(document));
    }

    // ======================
    // 6. 语义检索（最核心）
    // ======================
    public List<Document> search(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.7)  // 相似度阈值
                .build();
        return vectorStore.similaritySearch(request);
    }

    // ======================
    // 7. 删除向量
    // ======================
    public void deleteById(String id) {
        vectorStore.delete(List.of(id));
    }

    /**
     * 计算两个文本的相似度
     */
    private double calculateSimilarity(String text1, String text2) {
        float[] vec1 = embedText(text1);
        float[] vec2 = embedText(text2);

        return cosineSimilarity(vec1, vec2);
    }

    /**
     * 余弦相似度计算
     */
    private double cosineSimilarity(float[] vec1, float[] vec2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
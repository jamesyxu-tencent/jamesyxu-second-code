package com.example.service.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    @Autowired
    private ChromaVectorStore vectorStore;

    @Autowired
    private ChunkingService chunkingService;

    @Autowired
    @Qualifier("qwenPlusChatClient")
    private ChatClient qwenPlusChatClient;

    /**
     * 添加文档到知识库
     *
     * 将上传的文档进行分块处理，添加元数据后向量化存储到向量数据库中。
     * 每个文档会被分割成多个固定大小的文本块，每个文本块独立存储并携带原文档的元信息。
     *
     * @param title 文档标题，用于标识和检索文档
     * @param content 文档内容，将被分块处理后存储
     * @param category 文档分类，用于组织和过滤文档
     * @return 成功存储的文档块数量
     */
    public int addDocument(String title, String content, String category) {
        // 1. 按段落分块
        List<String> chunks = chunkingService.chunkBySemantic(content);

        // 2. 创建Document对象
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Document doc = new Document(chunks.get(i));
            doc.getMetadata().put("title", title);
            doc.getMetadata().put("chunkIndex", i);
            doc.getMetadata().put("category", category);
            doc.getMetadata().put("source", "upload");
            doc.getMetadata().put("createdTime", new Date().toString());
            documents.add(doc);
        }

        // 3. 向量化并存储
        vectorStore.add(documents);

        return documents.size();
    }

    /**
     * 搜索相关内容
     */
    public List<Document> search(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.3)  // 相似度阈值
                .build();

        return vectorStore.similaritySearch(request);
    }

    /**
     * 搜索相关内容
     */
    public List<Document> searchAll() {
        SearchRequest request = SearchRequest.builder()
                .query("")
                .topK(1000)
                .similarityThreshold(0.0)  // 设置为0，返回所有文档
                .build();
        return vectorStore.similaritySearch(request);
    }

    /**
     * 搜索相关内容（可自定义阈值）
     */
    public List<Document> searchWithThreshold(String query, int topK, double threshold) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();

        return vectorStore.similaritySearch(request);
    }

    /**
     * 基于知识库回答问题
     */
    public String askQuestion(String question, String systemPrompt) {
        // 1. 搜索相关文档
        List<Document> relevantDocs = search(question, 5);

        if (relevantDocs.isEmpty()) {
            return "抱歉，知识库中没有找到相关信息。";
        }

        // 2. 构建上下文
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // 3. 构建提示词（这里需要集成ChatClient）
        String fullPrompt = buildPrompt(question, context, systemPrompt);

        // 4. 调用AI生成回答
        return callAI(fullPrompt);
    }

    /**
     * 构建RAG提示词
     */
    private String buildPrompt(String question, String context, String systemPrompt) {
        if (systemPrompt == null || systemPrompt.isEmpty()) {
            systemPrompt = "你是一个智能助手，请基于以下参考资料回答用户的问题。" +
                    "如果参考资料中没有相关信息，请诚实告知用户。";
        }

        return String.format("""
            %s
            
            【参考资料】
            %s
            
            【用户问题】
            %s
            
            请基于以上参考资料回答用户的问题。如果参考资料中没有相关信息，请说"根据现有资料，我无法回答这个问题"。
            """, systemPrompt, context, question);
    }

    /**
     * 调用AI（需要集成ChatClient）
     */
    private String callAI(String prompt) {
        // 这里调用您已有的ChatClient
        // 简化实现，实际需要注入
        return qwenPlusChatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 获取知识库统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("collection", "knowledge_base");
        stats.put("message", "向量数据库已就绪");
        return stats;
    }

    /**
     * 清空知识库
     */
    public void clearKnowledgeBase() {
        // ChromaVectorStore 的删除方法
        // vectorStore.delete(ids);
    }
}
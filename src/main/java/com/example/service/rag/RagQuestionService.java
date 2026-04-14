package com.example.service.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagQuestionService {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    @Qualifier("qwenPlusChatClient")
    private ChatClient qwenPlusChatClient;

    /**
     * RAG问答（核心方法）
     */
    public String askWithRag(String question, int topK) {
        // 1. 从向量数据库检索相关文档
        List<Document> relevantDocs = knowledgeBaseService.search(question, topK);

        if (relevantDocs.isEmpty()) {
            return "抱歉，知识库中没有找到与您问题相关的信息。";
        }

        // 2. 构建上下文
        String context = buildContext(relevantDocs);

        // 3. 构建RAG提示词
        String ragPrompt = buildRagPrompt(question, context);

        // 4. 调用AI生成回答
        return qwenPlusChatClient.prompt()
                .user(ragPrompt)
                .call()
                .content();
    }

    /**
     * 带来源引用的RAG问答
     */
    public String askWithSources(String question, int topK) {
        List<Document> relevantDocs = knowledgeBaseService.search(question, topK);

        if (relevantDocs.isEmpty()) {
            return "抱歉，知识库中没有找到相关信息。";
        }

        String context = buildContextWithSources(relevantDocs);
        String ragPrompt = buildRagPromptWithSources(question, context);

        String answer = qwenPlusChatClient.prompt()
                .user(ragPrompt)
                .call()
                .content();

        // 附加来源信息
        String sources = buildSourceList(relevantDocs);
        return answer + "\n\n---\n📚 **参考资料**：\n" + sources;
    }

    /**
     * 构建上下文
     */
    private String buildContext(List<Document> docs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            sb.append("【片段").append(i + 1).append("】\n");
            sb.append(docs.get(i).getText()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 构建带来源的上下文
     */
    private String buildContextWithSources(List<Document> docs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            String title = doc.getMetadata().getOrDefault("title", "未知来源").toString();
            sb.append("【来源：").append(title).append("】\n");
            sb.append(doc.getText()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 构建RAG提示词
     */
    private String buildRagPrompt(String question, String context) {
        return """
            你是一个智能助手，请基于以下参考资料回答用户的问题。
            
            注意事项：
            1. 只使用参考资料中的信息回答
            2. 如果参考资料中没有相关信息，请说"根据现有资料，我无法回答这个问题"
            3. 回答要简洁、准确、有条理
            4. 可以适当引用参考资料中的原文
            
            【参考资料】
            %s
            
            【用户问题】
            %s
            
            请回答：
            """.formatted(context, question);
    }

    /**
     * 构建带来源引用的提示词
     */
    private String buildRagPromptWithSources(String question, String context) {
        return """
            你是一个智能助手，请基于以下参考资料回答用户的问题。
            
            要求：
            1. 回答时请引用具体的资料片段
            2. 如果多个资料有不同观点，请指出差异
            3. 回答要准确、专业
            
            【参考资料】
            %s
            
            【用户问题】
            %s
            
            请回答（如有引用，请注明来源）：
            """.formatted(context, question);
    }

    /**
     * 构建来源列表
     */
    private String buildSourceList(List<Document> docs) {
        return docs.stream()
                .map(doc -> {
                    String title = doc.getMetadata().getOrDefault("title", "未知").toString();
                    return "• " + title;
                })
                .distinct()
                .collect(Collectors.joining("\n"));
    }
}
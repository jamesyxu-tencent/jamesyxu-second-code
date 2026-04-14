package com.example.controller.rag;

import com.example.service.rag.KnowledgeBaseService;
import com.example.service.rag.RagQuestionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin(origins = "*")
public class KnowledgeController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private RagQuestionService ragQuestionService;

    @Autowired
    @Qualifier("qwenPlusChatClient")
    private ChatClient qwenPlusChatClient;

    /**
     * RAG问答
     */
    @PostMapping("/rag/ask")
    public ResponseEntity<Map<String, Object>> ragAsk(
            @RequestParam String question,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(defaultValue = "false") boolean withSources) {

        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String answer;
            if (withSources) {
                answer = ragQuestionService.askWithSources(question, topK);
            } else {
                answer = ragQuestionService.askWithRag(question, topK);
            }

            long duration = System.currentTimeMillis() - startTime;

            response.put("success", true);
            response.put("question", question);
            response.put("answer", answer);
            response.put("duration_ms", duration);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 普通问答 vs RAG问答对比
     */
    @PostMapping("/rag/compare")
    public ResponseEntity<Map<String, Object>> compare(
            @RequestParam String question) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 普通问答（不使用RAG）
            long normalStart = System.currentTimeMillis();
            String normalAnswer = qwenPlusChatClient.prompt().user(question).call().content();
            long normalDuration = System.currentTimeMillis() - normalStart;

            // RAG问答
            long ragStart = System.currentTimeMillis();
            String ragAnswer = ragQuestionService.askWithRag(question, 5);
            long ragDuration = System.currentTimeMillis() - ragStart;

            response.put("success", true);
            response.put("question", question);
            response.put("normal", Map.of(
                    "answer", normalAnswer,
                    "duration_ms", normalDuration
            ));
            response.put("rag", Map.of(
                    "answer", ragAnswer,
                    "duration_ms", ragDuration
            ));

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 上传文本文档
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 读取文件内容
            String content;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), "UTF-8"))) {
                content = reader.lines().collect(Collectors.joining("\n"));
            }

            String docTitle = title != null ? title : file.getOriginalFilename();
            String docCategory = category != null ? category : "general";

            int chunkCount = knowledgeBaseService.addDocument(docTitle, content, docCategory);

            response.put("success", true);
            response.put("message", "文档上传成功");
            response.put("chunkCount", chunkCount);
            response.put("title", docTitle);
            response.put("category", docCategory);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 直接添加文本内容
     */
    @PostMapping("/text")
    public ResponseEntity<Map<String, Object>> addText(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String category) {

        Map<String, Object> response = new HashMap<>();

        try {
            int chunkCount = knowledgeBaseService.addDocument(
                    title, content, category != null ? category : "general");

            response.put("success", true);
            response.put("chunkCount", chunkCount);
            response.put("title", title);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 搜索知识库
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Document> results = knowledgeBaseService.search(query, topK);

            List<Map<String, Object>> formattedResults = results.stream()
                    .map(doc -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("content", doc.getText());
                        item.put("metadata", doc.getMetadata());
                        return item;
                    })
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("query", query);
            response.put("results", formattedResults);
            response.put("count", formattedResults.size());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 基于知识库问答
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(
            @RequestParam String question,
            @RequestParam(required = false) String systemPrompt) {

        Map<String, Object> response = new HashMap<>();

        try {
            String answer = knowledgeBaseService.askQuestion(question, systemPrompt);

            response.put("success", true);
            response.put("question", question);
            response.put("answer", answer);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取知识库统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(knowledgeBaseService.getStats());
    }
}
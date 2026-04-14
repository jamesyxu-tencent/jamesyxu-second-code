package com.example.module.rag;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档实体
 */
@Data
public class KnowledgeDocument {
    private String id;
    private String title;           // 文档标题
    private String content;         // 原始内容
    private String chunkContent;    // 分块内容
    private Integer chunkIndex;     // 分块索引
    private String source;          // 来源（文件名/URL）
    private String category;        // 分类
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public KnowledgeDocument() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
}
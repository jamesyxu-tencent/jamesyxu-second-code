package com.example.service.rag;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块服务
 * 将长文档切分成适合向量化的小块
 */
@Service
public class ChunkingService {

    // 分块大小（字符数）
    private static final int CHUNK_SIZE = 500;
    // 分块重叠（保持上下文连贯）
    private static final int CHUNK_OVERLAP = 50;

    /**
     * 按固定大小分块（简单方法）
     */
    public List<String> chunkByFixedSize(String text) {
        return chunkByFixedSize(text, CHUNK_SIZE, CHUNK_OVERLAP);
    }

    /**
     * 按固定大小分块（可配置参数）
     */
    public List<String> chunkByFixedSize(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        int textLength = text.length();

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);

            // 尝试在句子边界处切割
            if (end < textLength) {
                int lastPeriod = text.lastIndexOf('。', end);
                int lastNewLine = text.lastIndexOf('\n', end);
                int lastSpace = text.lastIndexOf(' ', end);
                int breakPoint = Math.max(lastPeriod, Math.max(lastNewLine, lastSpace));

                if (breakPoint > start) {
                    end = breakPoint + 1;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            start = end - overlap;
        }

        return chunks;
    }

    /**
     * 按段落分块（适合文章）
     */
    public List<String> chunkByParagraph(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // 按换行符分割段落
        String[] paragraphs = text.split("\n\n");

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) continue;

            // 如果当前块加上新段落会超过限制，则保存当前块并开始新块
            if (currentChunk.length() + trimmed.length() > CHUNK_SIZE &&
                    currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }

            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(trimmed);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    /**
     * 按语义分块（基于句子边界）
     */
    public List<String> chunkBySemantic(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // 按句子分割
        String[] sentences = text.split("(?<=[。！？!?])\\s*");

        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.isEmpty()) continue;

            // 如果当前块加上新句子会超过限制
            if (currentChunk.length() + trimmed.length() > CHUNK_SIZE &&
                    currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }

            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(trimmed);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }
}
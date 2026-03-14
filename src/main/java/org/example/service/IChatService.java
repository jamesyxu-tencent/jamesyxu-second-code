package org.example.service;

import org.example.dto.CrispeRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * chatService
 */
public interface IChatService {

    /**
     * 处理同步聊天请求
     *
     * @param prompt
     * @return
     */
    String chat(String prompt);

    /**
     * 处理流式聊天请求
     *
     * @param message
     * @param sessionId
     * @return
     */
    SseEmitter chatStream(String message, String sessionId);

    /**
     * 清理会话
     *
     * @param sessionId
     * @return
     */
    Map<String, String> clearConversation(String sessionId);

    /**
     * 带角色的ask
     *
     * @param question
     * @param role
     * @return
     */
    String chatWithRole(String question, String role);

    /**
     * CRISPE模式提示词
     *
     * @param dto
     * @return
     */
    String chatWithCrispe(CrispeRequestDTO dto);

}

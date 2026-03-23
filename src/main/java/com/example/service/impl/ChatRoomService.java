package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.ChatMessage;
import com.example.entity.ChatSession;
import com.example.mapper.ChatMessageMapper;
import com.example.mapper.ChatSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ChatRoomService {

    // 模型类型常量
    public static final String MODEL_QWEN_TURBO = "qwen-turbo";
    public static final String MODEL_OLLAMA = "ollama";
    public static final String MODEL_AUTO = "auto";

    @Autowired
    private ChatSessionMapper sessionMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    @Autowired
    private ModelRouterService routerService;

    @Autowired
    @Qualifier("localChatClient")
    private ChatClient localChatClient;

    @Autowired
    @Qualifier("cloudChatClient")
    private ChatClient cloudChatClient;

//    // 注入本地Ollama的ChatClient
//    private final ChatClient localChatClient;
//    // 注入云端OpenAI的ChatClient
//    private final ChatClient cloudChatClient;
//
//    // 构造器注入，通过@Qualifier指定Bean名称
//    public ChatRoomService(@Qualifier("localChatClient") ChatClient localChatClient,
//            @Qualifier("cloudChatClient") ChatClient cloudChatClient) {
//        this.localChatClient = localChatClient;
//        this.cloudChatClient = cloudChatClient;
//    }

    // ==================== 会话管理 ====================

    /**
     * 创建新会话
     */
    public ChatSession createSession(String sessionName) {
        ChatSession session;
        if (sessionName != null && !sessionName.isEmpty()) {
            session = new ChatSession(sessionName);
        } else {
            String defaultName = "新会话 " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            session = new ChatSession(defaultName);
        }
        sessionMapper.insert(session);
        log.info("创建会话成功: id={}, name={}", session.getId(), session.getName());
        return session;
    }

    /**
     * 获取所有会话
     */
    public List<ChatSession> getAllSessions() {
        return sessionMapper.selectAllOrderByLastMessageTime();
    }

    /**
     * 获取会话详情
     */
    public Optional<ChatSession> getSession(String sessionId) {
        return Optional.ofNullable(sessionMapper.selectById(sessionId));
    }

    /**
     * 获取会话及其所有消息
     */
    public ChatSession getSessionWithMessages(String sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            List<ChatMessage> messages = messageMapper.selectBySessionIdOrderByTime(sessionId);
            session.setMessages(messages);
        }
        return session;
    }

    /**
     * 删除会话（级联删除消息）
     */
    public void deleteSession(String sessionId) {
        // 先删除消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        messageMapper.delete(wrapper);
        // 再删除会话
        sessionMapper.deleteById(sessionId);
        log.info("删除会话成功: id={}", sessionId);
    }

    // ==================== 消息管理 ====================

    /**
     * 发送消息（单模型）
     */
    public ChatMessage sendMessage(String sessionId, String content, String modelType) {
        // 获取或创建会话
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            session = createSession(null);
            sessionId = session.getId().toString();
        }

        // 保存用户消息
        ChatMessage userMessage = new ChatMessage(sessionId, content);
        messageMapper.insert(userMessage);

        // 选择模型并调用
        long startTime = System.currentTimeMillis();
        String answer;
        String usedModel = null;

        try {
            if (MODEL_AUTO.equals(modelType)) {
                usedModel = routerService.routeModel(content);
                answer = callModel(usedModel, content, sessionId);
            } else {
                usedModel = modelType;
                answer = callModel(modelType, content, sessionId);
            }

            long duration = System.currentTimeMillis() - startTime;

            // 保存AI回答
            ChatMessage assistantMessage = new ChatMessage(sessionId, answer, usedModel, (int) duration);
            messageMapper.insert(assistantMessage);

            // 更新会话信息
            if (session.getMessages() == null || session.getMessages().isEmpty()) {
                // 第一条消息，用内容作为会话名称
                String shortName = content.length() > 20 ? content.substring(0, 20) + "..." : content;
                session.setName(shortName);
            }
            session.setModelType(modelType);
            session.setLastMessageTime(LocalDateTime.now());
            sessionMapper.updateById(session);

            log.info("消息发送成功: session={}, model={}, time={}ms", sessionId, usedModel, duration);
            return assistantMessage;

        } catch (Exception e) {
            log.error("调用模型失败", e);
            ChatMessage errorMessage = new ChatMessage(sessionId,
                    "抱歉，调用模型时出错：" + e.getMessage(), usedModel != null ? usedModel : "error",
                    (int) (System.currentTimeMillis() - startTime));
            messageMapper.insert(errorMessage);
            return errorMessage;
        }
    }

    /**
     * 发送消息（对比模式）
     */
    public Map<String, Object> sendCompareMessage(String sessionId, String content) {
        Map<String, Object> result = new HashMap<>();

        // 获取或创建会话
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            session = createSession(null);
            sessionId = session.getId().toString();
        }

        // 保存用户消息
        ChatMessage userMessage = new ChatMessage(sessionId, content);
        messageMapper.insert(userMessage);

        // 同时调用两个模型
        Map<String, Object> cloudResult = callModelWithTime(MODEL_QWEN_TURBO, content, sessionId);
        Map<String, Object> ollamaResult = callModelWithTime(MODEL_OLLAMA, content, sessionId);

        // 保存两个回答
        ChatMessage cloudMessage = new ChatMessage(sessionId,
                (String) cloudResult.get("answer"), MODEL_QWEN_TURBO, (Integer) cloudResult.get("time"));
        messageMapper.insert(cloudMessage);

        ChatMessage ollamaMessage = new ChatMessage(sessionId,
                (String) ollamaResult.get("answer"), MODEL_OLLAMA, (Integer) ollamaResult.get("time"));
        messageMapper.insert(ollamaMessage);

        // 更新会话
        session.setLastMessageTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        result.put("cloud", cloudResult);
        result.put("ollama", ollamaResult);

        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 调用指定模型
     */
    private String callModel(String modelType, String content, String sessionId) {
        ChatClient client = getChatClient(modelType);

        // 获取最近5条消息作为上下文
        List<ChatMessage> recentMessages = messageMapper.selectRecentMessages(sessionId, 5);
        String context = recentMessages.stream()
                .map(msg -> msg.getRole() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));

        return client.prompt()
                .system("你是一个AI助手，请基于对话历史回答问题。\n历史对话：\n" + context)
                .user(content)
                .call()
                .content();
    }

    /**
     * 调用模型并记录时间
     */
    private Map<String, Object> callModelWithTime(String modelType, String content, String sessionId) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String answer = callModel(modelType, content, sessionId);
            long duration = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("answer", answer);
            result.put("time", (int) duration);
            result.put("model", modelType);

        } catch (Exception e) {
            log.error("调用模型失败: {}", modelType, e);
            result.put("success", false);
            result.put("answer", "调用失败：" + e.getMessage());
            result.put("time", (int) (System.currentTimeMillis() - startTime));
            result.put("model", modelType);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 获取对应的ChatClient
     */
    private ChatClient getChatClient(String modelType) {
        if (modelType.equals(MODEL_OLLAMA)) {
            return localChatClient;
        }
        return cloudChatClient;
    }

    // ==================== 统计与导出 ====================

    /**
     * 获取会话统计信息
     */
    public Map<String, Object> getSessionStats(String sessionId) {
        Map<String, Object> stats = new HashMap<>();

        long totalMessages = messageMapper.countBySessionId(sessionId);
        long userMessages = messageMapper.countUserMessagesBySessionId(sessionId);
        long assistantMessages = messageMapper.countAssistantMessagesBySessionId(sessionId);

        stats.put("totalMessages", totalMessages);
        stats.put("userMessages", userMessages);
        stats.put("assistantMessages", assistantMessages);

        // 计算平均响应时间
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getRole, "assistant")
                .isNotNull(ChatMessage::getResponseTimeMs);
        List<ChatMessage> assistantMsgs = messageMapper.selectList(wrapper);

        double avgResponseTime = assistantMsgs.stream()
                .mapToInt(ChatMessage::getResponseTimeMs)
                .average()
                .orElse(0);
        stats.put("avgResponseTime", (int) avgResponseTime);

        return stats;
    }

    /**
     * 导出会话为文本
     */
    public String exportSession(String sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return "会话不存在";
        }

        List<ChatMessage> messages = messageMapper.selectBySessionIdOrderByTime(sessionId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== 聊天记录 ===\n");
        sb.append("会话：").append(session.getName()).append("\n");
        sb.append("会话ID：").append(session.getId()).append("\n");
        sb.append("创建时间：").append(session.getCreateTime()).append("\n");
        sb.append("导出时间：").append(LocalDateTime.now()).append("\n");
        sb.append("=".repeat(50)).append("\n\n");

        for (ChatMessage msg : messages) {
            String timeStr = msg.getCreateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String prefix = "user".equals(msg.getRole()) ? "👤 用户" : "🤖 AI";

            if ("assistant".equals(msg.getRole()) && msg.getModelUsed() != null) {
                String modelIcon = msg.getModelUsed().contains("ollama") ? "💻" : "☁️";
                prefix += String.format(" %s [%s]", modelIcon, msg.getModelUsed());
            }

            sb.append(String.format("[%s] %s：\n", timeStr, prefix));
            sb.append(msg.getContent()).append("\n");

            if (msg.getResponseTimeMs() != null) {
                sb.append(String.format("   (⏱️ %dms)\n", msg.getResponseTimeMs()));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
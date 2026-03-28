package com.example.service.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.ChatMessage;
import com.example.entity.ChatSession;
import com.example.mapper.ChatMessageMapper;
import com.example.mapper.ChatSessionMapper;
import com.example.service.router.ModelRouterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ChatRoomService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
    @Qualifier("ollamaChatClient")
    private ChatClient ollamaChatClient;

    @Autowired
    @Qualifier("qwenTurboChatClient")
    private ChatClient qwenTurboChatClient;

    @Autowired
    @Qualifier("qwenPlusChatClient")
    private ChatClient qwenPlusChatClient;

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
        String usedModel = null;

        try {
            if (MODEL_AUTO.equals(modelType)) {
                usedModel = routerService.routeModel(content);
            } else {
                usedModel = modelType;
            }

            long duration = System.currentTimeMillis() - startTime;

            String answer = callModel(usedModel, content, sessionId);

            // 保存AI回答
            ChatMessage assistantMessage = new ChatMessage(sessionId, answer, usedModel, (int) duration);
            messageMapper.insert(assistantMessage);

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

        // 获取最近10条消息作为上下文
        String context = getContext(sessionId);

        return client.prompt()
                .system("你是一个AI助手，请基于对话历史回答问题。\n历史对话：\n" + context)
                .user(content)
                .call()
                .content();
    }

    /**
     * 获取最近10条消息作为上下文
     *
     * @param sessionId
     * @return
     */
    private String getContext(String sessionId) {
        List<ChatMessage> recentMessages = messageMapper.selectRecentMessages(sessionId, 10);
        List<Map<String, String>> conversationHistory =  recentMessages.stream()
                .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                .toList();

        String context = "";
        try {
            context = objectMapper.writeValueAsString(conversationHistory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context;
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
            return ollamaChatClient;
        } else if (modelType.equals(MODEL_QWEN_TURBO)) {
            return qwenTurboChatClient;
        } else {
            return qwenPlusChatClient;
        }
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

    // ==================== 新增：消息操作功能 ====================

    /**
     * 删除单条消息
     * 如果删除的是用户消息，同时删除对应的AI回复
     * 如果删除的是AI消息，只删除自己
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        ChatMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            log.warn("消息不存在: {}", messageId);
            return;
        }

        String sessionId = message.getSessionId();

        if ("user".equals(message.getRole())) {
            // 删除用户消息及其对应的AI回复
            // 查找该用户消息之后的AI回复（最近的一条）
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMessage::getSessionId, sessionId)
                    .eq(ChatMessage::getRole, "assistant")
                    .gt(ChatMessage::getCreateTime, message.getCreateTime())
                    .orderByAsc(ChatMessage::getCreateTime)
                    .last("LIMIT 1");

            ChatMessage aiResponse = messageMapper.selectOne(wrapper);
            if (aiResponse != null) {
                messageMapper.deleteById(aiResponse.getId());
                log.info("删除AI回复: {}", aiResponse.getId());
            }

            // 删除用户消息
            messageMapper.deleteById(messageId);
            log.info("删除用户消息: {}", messageId);

        } else {
            // 删除AI消息（只删除自己）
            messageMapper.deleteById(messageId);
            log.info("删除AI消息: {}", messageId);
        }

        // 更新会话的最后消息时间
        updateSessionLastMessageTime(sessionId);
    }

    /**
     * 编辑用户消息并重新生成AI回答
     */
    @Transactional
    public ChatMessage editMessageAndRegenerate(Long messageId, String newContent, String modelType) {
        // 1. 获取原消息
        ChatMessage originalMessage = messageMapper.selectById(messageId);
        if (originalMessage == null || !"user".equals(originalMessage.getRole())) {
            throw new RuntimeException("只能编辑用户消息");
        }

        String sessionId = originalMessage.getSessionId();

        // 2. 删除该消息之后的所有AI回复
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getRole, "assistant")
                .gt(ChatMessage::getCreateTime, originalMessage.getCreateTime());
        List<ChatMessage> aiMessages = messageMapper.selectList(wrapper);
        for (ChatMessage aiMsg : aiMessages) {
            messageMapper.deleteById(aiMsg.getId());
        }
        log.info("删除了 {} 条后续AI回复", aiMessages.size());

        // 3. 更新用户消息内容
        originalMessage.setContent(newContent);
        originalMessage.setCreateTime(LocalDateTime.now());  // 更新时间
        messageMapper.updateById(originalMessage);

        // 4. 重新生成AI回答
        String usedModel;
        if ("auto".equals(modelType)) {
            usedModel = routerService.routeModel(newContent);
        } else {
            usedModel = modelType;
        }

        // 获取最近10条消息作为上下文调用ai生成新的answer
        Map<String, Object> result = callModelWithTime(usedModel, newContent, sessionId);

        // 5. 保存新的AI回答
        ChatMessage newAiMessage = new ChatMessage(sessionId,
                (String) result.get("answer"), MODEL_QWEN_TURBO, (Integer) result.get("time"));
        messageMapper.insert(newAiMessage);

        // 6. 更新会话的最后消息时间
        updateSessionLastMessageTime(sessionId);

        log.info("重新生成回答成功: 原消息={}, 新消息={}", messageId, newAiMessage.getId());

        return newAiMessage;
    }

    /**
     * 重新生成AI回答（不修改用户消息）
     */
    @Transactional
    public ChatMessage regenerateAiResponse(Long messageId, String modelType) {
        // 1. 获取AI消息
        ChatMessage aiMessage = messageMapper.selectById(messageId);
        if (aiMessage == null || !"assistant".equals(aiMessage.getRole())) {
            throw new RuntimeException("只能重新生成AI消息");
        }

        String sessionId = aiMessage.getSessionId();

        // 2. 找到对应的用户消息（上一条消息）
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getRole, "user")
                .lt(ChatMessage::getCreateTime, aiMessage.getCreateTime())
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT 1");

        ChatMessage userMessage = messageMapper.selectOne(wrapper);
        if (userMessage == null) {
            throw new RuntimeException("找不到对应的用户消息");
        }

        // 3. 删除旧的AI回复
        messageMapper.deleteById(messageId);

        // 4. 重新生成回答
        String usedModel;
        if ("auto".equals(modelType)) {
            usedModel = routerService.routeModel(userMessage.getContent());
        } else {
            usedModel = modelType;
        }

        // 获取最近10条消息作为上下文调用ai生成新的answer
        Map<String, Object> result = callModelWithTime(usedModel, userMessage.getContent(), sessionId);

        // 5. 保存新的AI回答
        ChatMessage newAiMessage = new ChatMessage(sessionId,
                (String) result.get("answer"), usedModel, (Integer) result.get("time"));
        messageMapper.insert(newAiMessage);

        // 6. 更新会话的最后消息时间
        updateSessionLastMessageTime(sessionId);

        log.info("重新生成回答成功: 原AI消息={}, 新消息={}", messageId, newAiMessage.getId());

        return newAiMessage;
    }

    /**
     * 更新会话的最后消息时间
     */
    private void updateSessionLastMessageTime(String sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            // 获取最新的消息时间
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMessage::getSessionId, sessionId)
                    .orderByDesc(ChatMessage::getCreateTime)
                    .last("LIMIT 1");
            ChatMessage lastMessage = messageMapper.selectOne(wrapper);

            if (lastMessage != null) {
                session.setLastMessageTime(lastMessage.getCreateTime());
            } else {
                session.setLastMessageTime(session.getCreateTime());
            }
            sessionMapper.updateById(session);
        }
    }

    /**
     * 获取消息详情
     */
    public ChatMessage getMessage(Long messageId) {
        return messageMapper.selectById(messageId);
    }
}
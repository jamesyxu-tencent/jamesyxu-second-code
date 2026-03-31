package com.example.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.ChatMessage;
import com.example.entity.ChatSession;
import com.example.mapper.ChatMessageMapper;
import com.example.mapper.ChatSessionMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库查询工具
 * 让AI能够查询聊天记录数据
 */
@Component
public class DatabaseQueryTools {

    @Autowired
    private ChatSessionMapper sessionMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    /**
     * 查询会话列表
     */
    @Tool(description = "查询所有聊天会话列表，返回会话ID、名称、创建时间")
    public String listSessions() {
        System.out.println("====== 调用数据库工具: listSessions");

        List<ChatSession> sessions = sessionMapper.selectList(null);
        if (sessions.isEmpty()) {
            return "暂无任何聊天会话";
        }

        StringBuilder sb = new StringBuilder("【会话列表】\n");
        for (ChatSession session : sessions) {
            sb.append(String.format("- %s (ID: %s, 创建于: %s)\n",
                    session.getName(),
                    session.getId().toString().substring(0, 8),
                    session.getCreateTime().toString().substring(0, 16)));
        }
        sb.append(String.format("\n共 %d 个会话", sessions.size()));

        return sb.toString();
    }

    /**
     * 查询会话消息统计
     */
    @Tool(description = "统计指定会话的消息数量")
    public String countMessages(
            @ToolParam(description = "会话ID") String sessionId) {

        System.out.println("====== 调用数据库工具: countMessages, " + sessionId);

        Long count = messageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, sessionId));

        return String.format("会话 %s 共有 %d 条消息", sessionId, count);
    }

    /**
     * 查询最近的消息
     */
    @Tool(description = "查询最近的消息记录")
    public String getRecentMessages(
            @ToolParam(description = "会话ID（可选，不传则查询所有会话）", required = false) String sessionId,
            @ToolParam(description = "查询条数，默认5条", required = false) Integer limit) {

        if (limit == null) limit = 5;
        limit = Math.min(limit, 20);

        System.out.println("====== 调用数据库工具: getRecentMessages, session=" + sessionId + ", limit=" + limit);

        List<ChatMessage> messages;
        if (sessionId != null && !sessionId.isEmpty()) {
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMessage::getSessionId, sessionId)
                    .orderByDesc(ChatMessage::getCreateTime)
                    .last("LIMIT " + limit);
            messages = messageMapper.selectList(wrapper);
        } else {
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ChatMessage::getCreateTime)
                    .last("LIMIT " + limit);
            messages = messageMapper.selectList(wrapper);
        }

        if (messages.isEmpty()) {
            return "暂无消息记录";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【最近%d条消息】\n", messages.size()));
        for (ChatMessage msg : messages) {
            String roleIcon = "user".equals(msg.getRole()) ? "👤" : "🤖";
            String content = msg.getContent().length() > 50 ?
                    msg.getContent().substring(0, 50) + "..." : msg.getContent();
            sb.append(String.format("%s %s: %s\n", roleIcon, msg.getRole(), content));
        }

        return sb.toString();
    }
}
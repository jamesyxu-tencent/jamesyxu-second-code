package com.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_message")
public class ChatMessage extends BaseEntity {

    /**
     * 所属会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 角色：user/assistant
     */
    @TableField("role")
    private String role;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 使用的模型
     */
    @TableField("model_used")
    private String modelUsed;

    /**
     * 响应时间（毫秒）
     */
    @TableField("response_time_ms")
    private Integer responseTimeMs;

    /**
     * 构造函数（用户消息）
     */
    public ChatMessage(String sessionId, String content) {
        this.sessionId = sessionId;
        this.role = "user";
        this.content = content;
        this.createTime = LocalDateTime.now();
    }

    /**
     * 构造函数（AI消息）
     */
    public ChatMessage(String sessionId, String content, String modelUsed, Integer responseTimeMs) {
        this.sessionId = sessionId;
        this.role = "assistant";
        this.content = content;
        this.modelUsed = modelUsed;
        this.responseTimeMs = responseTimeMs;
        this.createTime = LocalDateTime.now();
    }

}
package com.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_session")  // 指定表名
public class ChatSession extends BaseEntity {

    /**
     * 会话名称
     */
    @TableField("name")
    private String name;


    /**
     * 最后消息时间
     */
    @TableField(value = "last_message_time")
    private LocalDateTime lastMessageTime;

    /**
     * 当前使用的模型类型
     */
    @TableField("model_type")
    private String modelType = "auto";

    @TableField(exist = false)
    private List<ChatMessage> messages;

    /**
     * 构造函数（创建新会话）
     */
    public ChatSession(String name) {
        this.name = name;
        this.createTime = LocalDateTime.now();
        this.lastMessageTime = this.createTime;
        this.modelType = "auto";
    }

}
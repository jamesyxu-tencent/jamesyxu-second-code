package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 聊天会话实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_feedback")  // 指定表名
public class ChatFeedback extends BaseEntity {

    /**
     * 消息id
     */
    @TableField("message_id")
    private String messageId;


    /**
     * like/dislike
     */
    @TableField("feedback_type")
    private String feedbackType;

    /**
     * 当前使用的模型类型
     */
    @TableField("comment")
    private String comment;

}
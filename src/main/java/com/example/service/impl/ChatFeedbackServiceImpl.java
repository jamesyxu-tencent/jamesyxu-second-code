package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.ChatFeedback;
import com.example.entity.ChatMessage;
import com.example.mapper.ChatFeedbackMapper;
import com.example.service.IChatFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatFeedbackServiceImpl extends ServiceImpl<ChatFeedbackMapper, ChatFeedback> implements IChatFeedbackService {

    @Autowired
    private ChatFeedbackMapper chatFeedbackMapper;

    @Override
    public ResponseEntity<String> toggleFavorite(Long messageId) {
        // 查询是否已收藏
        LambdaQueryWrapper<ChatFeedback> query = Wrappers.lambdaQuery();
        query.eq(ChatFeedback::getMessageId, messageId)
                .eq(ChatFeedback::getFeedbackType, "like")
                .eq(ChatFeedback::getDeleted, false);

        ChatFeedback exist = chatFeedbackMapper.selectOne(query);

        if (exist != null) {
            // 取消收藏（逻辑删除）
            LambdaUpdateWrapper<ChatFeedback> update = Wrappers.lambdaUpdate();
            update.eq(ChatFeedback::getId, exist.getId())
                    .set(ChatFeedback::getDeleted, true);
            chatFeedbackMapper.update(null, update);
            return ResponseEntity.ok("unliked");
        } else {
            // 添加收藏
            ChatFeedback feedback = new ChatFeedback();
            feedback.setMessageId(messageId.toString());
            feedback.setFeedbackType("like");
            feedback.setDeleted(false);
            chatFeedbackMapper.insert(feedback);
            return ResponseEntity.ok("liked");
        }
    }

    @Override
    public List<ChatMessage> getMyFavorites() {
        return chatFeedbackMapper.selectFavoriteMessages();
    }
}
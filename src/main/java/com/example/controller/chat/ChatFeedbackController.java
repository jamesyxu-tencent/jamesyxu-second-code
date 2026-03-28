package com.example.controller.chat;

import com.example.entity.ChatMessage;
import com.example.service.IChatFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/feedback")
public class ChatFeedbackController {

    @Autowired
    private IChatFeedbackService chatFeedbackService;

    /**
     * 收藏 / 取消收藏
     */
    @PostMapping("/toggle-favorite")
    public ResponseEntity<String> toggleFavorite(Long messageId) {
        return chatFeedbackService.toggleFavorite(messageId);
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/my-favorites")
    public ResponseEntity<List<ChatMessage>> myFavorites() {
        return ResponseEntity.ok(chatFeedbackService.getMyFavorites());
    }
}

package com.example.service;

import com.example.entity.ChatMessage;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IChatFeedbackService {

    ResponseEntity<String> toggleFavorite(Long messageId);

    List<ChatMessage> getMyFavorites();
}

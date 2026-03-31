package com.example.controller.chat;

import com.example.entity.ChatMessage;
import com.example.entity.ChatSession;
import com.example.service.chat.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    /**
     * 创建新会话
     */
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(@RequestParam(required = false) String name) {
        ChatSession session = chatRoomService.createSession(name);
        return ResponseEntity.ok(session);
    }

    /**
     * 获取所有会话
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getAllSessions() {
        List<ChatSession> sessions = chatRoomService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * 获取会话详情（包含消息）
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSession> getSession(@PathVariable String sessionId) {
        ChatSession session = chatRoomService.getSessionWithMessages(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, String>> deleteSession(@PathVariable String sessionId) {
        chatRoomService.deleteSession(sessionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 发送消息（普通模式）
     */
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @RequestParam String sessionId,
            @RequestParam String message,
            @RequestParam(defaultValue = "auto") String model) {
        ChatMessage response = chatRoomService.sendMessage(sessionId, message, model);
        return ResponseEntity.ok(response);
    }

    /**
     * 发送消息（对比模式）
     */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> sendCompareMessage(
            @RequestParam String sessionId,
            @RequestParam String message) {
        Map<String, Object> result = chatRoomService.sendCompareMessage(sessionId, message);
        return ResponseEntity.ok(result);
    }

    /**
     * 导出会话
     */
    @GetMapping("/sessions/{sessionId}/export")
    public ResponseEntity<String> exportSession(@PathVariable String sessionId) {
        String content = chatRoomService.exportSession(sessionId);
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain;charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=chat-" + sessionId + ".txt")
                .body(content);
    }

    /**
     * 获取会话统计
     */
    @GetMapping("/sessions/{sessionId}/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats(@PathVariable String sessionId) {
        Map<String, Object> stats = chatRoomService.getSessionStats(sessionId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取可用模型列表
     */
    @GetMapping("/models")
    public ResponseEntity<List<Map<String, String>>> getAvailableModels() {
        List<Map<String, String>> models = List.of(
                Map.of("id", "auto", "name", "🤖 自动路由 (智能选择)", "desc", "根据问题自动选择最合适的模型"),
                Map.of("id", "qwen-turbo", "name", "☁️ 通义千问-turbo (快速)", "desc", "云端模型，响应快，成本低"),
                Map.of("id", "qwen-plus", "name", "☁️ 通义千问-plus (高质量)", "desc", "云端模型，质量高，适合复杂问题"),
                Map.of("id", "ollama", "name", "💻 本地Ollama (数据安全)", "desc", "本地模型，数据安全，无需网络")
        );
        return ResponseEntity.ok(models);
    }

    // ==================== 新增：消息操作接口 ====================

    /**
     * 删除单条消息
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable Long messageId) {
        Map<String, Object> response = new HashMap<>();
        try {
            chatRoomService.deleteMessage(messageId);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除消息失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 编辑用户消息并重新生成
     */
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> editMessage(
            @PathVariable Long messageId,
            @RequestParam String content,
            @RequestParam(defaultValue = "auto") String model) {

        Map<String, Object> response = new HashMap<>();
        try {
            ChatMessage newAiMessage = chatRoomService.editMessageAndRegenerate(messageId, content, model);
            response.put("success", true);
            response.put("message", "编辑成功");
            response.put("newAiMessage", newAiMessage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("编辑消息失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 重新生成AI回答
     */
    @PostMapping("/messages/{messageId}/regenerate")
    public ResponseEntity<Map<String, Object>> regenerateMessage(
            @PathVariable Long messageId,
            @RequestParam(defaultValue = "auto") String model) {

        Map<String, Object> response = new HashMap<>();
        try {
            ChatMessage newAiMessage = chatRoomService.regenerateAiResponse(messageId, model);
            response.put("success", true);
            response.put("message", "重新生成成功");
            response.put("newAiMessage", newAiMessage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("重新生成失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取单条消息详情
     */
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessage> getMessage(@PathVariable Long messageId) {
        ChatMessage message = chatRoomService.getMessage(messageId);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }

    /**
     * 发送消息（支持工具调用）
     */
    @PostMapping("/message/send-with-tools")
    public ResponseEntity<ChatMessage> sendMessageWithTools(
            @RequestParam String sessionId,
            @RequestParam String message,
            @RequestParam(defaultValue = "auto") String model) {
        ChatMessage response = chatRoomService.sendMessageWithTools(sessionId, message, model);
        return ResponseEntity.ok(response);
    }
}
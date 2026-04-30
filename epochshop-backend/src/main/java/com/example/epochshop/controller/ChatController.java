package com.example.epochshop.controller;

import com.example.epochshop.dto.ConversationDTO;
import com.example.epochshop.entity.ChatMessage;
import com.example.epochshop.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, Object> payload) {
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String content = payload.get("content").toString();
        Long productId = payload.containsKey("productId") ? Long.valueOf(payload.get("productId").toString()) : null;
        return ResponseEntity.ok(chatService.sendMessage(receiverId, productId, content));
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<ChatMessage>> getConversation(@PathVariable Long otherUserId) {
        return ResponseEntity.ok(chatService.getConversation(otherUserId));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations() {
        return ResponseEntity.ok(chatService.getConversationList());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        chatService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 标记整个对话已读
    @PutMapping("/conversation/{otherUserId}/read")
    public ResponseEntity<Void> markConversationAsRead(@PathVariable Long otherUserId) {
        chatService.markConversationAsRead(otherUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", chatService.getUnreadCount()));
    }
}
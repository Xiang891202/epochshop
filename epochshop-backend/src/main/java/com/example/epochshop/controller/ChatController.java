package com.example.epochshop.controller;

import com.example.epochshop.dto.ConversationDTO;
import com.example.epochshop.entity.ChatMessage;
import com.example.epochshop.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "聊天訊息", description = "客戶與賣家即時對話 API")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "發送訊息")
    @PostMapping
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, Object> payload) {
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String content = payload.get("content").toString();
        Long productId = payload.containsKey("productId") ? Long.valueOf(payload.get("productId").toString()) : null;
        return ResponseEntity.ok(chatService.sendMessage(receiverId, productId, content));
    }

    @Operation(summary = "獲取與特定用戶的對話記錄")
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<ChatMessage>> getConversation(@PathVariable Long otherUserId) {
        return ResponseEntity.ok(chatService.getConversation(otherUserId));
    }

    @Operation(summary = "獲取當前用戶的對話列表")
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations() {
        return ResponseEntity.ok(chatService.getConversationList());
    }

    @Operation(summary = "標記訊息為已讀")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        chatService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "標記整個對話為已讀")
    @PutMapping("/conversation/{otherUserId}/read")
    public ResponseEntity<Void> markConversationAsRead(@PathVariable Long otherUserId) {
        chatService.markConversationAsRead(otherUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "獲取未讀訊息數量")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", chatService.getUnreadCount()));
    }
}
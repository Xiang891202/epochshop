package com.example.epochshop.dto;

import lombok.Data;

@Data
public class ConversationDTO {
    private Long otherUserId;
    private String otherUserName;      // 对方显示名称
    private String lastMessage;
    private String lastTimestamp;
    private long unreadCount;
}
package com.example.epochshop.service;

import com.example.epochshop.dto.ConversationDTO;
import com.example.epochshop.entity.ChatMessage;
import com.example.epochshop.entity.User;
import com.example.epochshop.repository.ChatMessageRepository;
import com.example.epochshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepo;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public ChatMessage sendMessage(Long receiverId, Long productId, String content) {
        User sender = getCurrentUser();
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("接收者不存在"));
        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        msg.setTimestamp(java.time.LocalDateTime.now());
        msg.setIsRead(false);
        return chatMessageRepo.save(msg);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getConversation(Long otherUserId) {
        User currentUser = getCurrentUser();
        return chatMessageRepo.findConversation(currentUser.getId(), otherUserId);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationList() {
        User currentUser = getCurrentUser();
        List<ChatMessage> lastMessages = chatMessageRepo.findLastMessagesByUser(currentUser.getId());
        
        // 根据角色过滤对方用户
        boolean isAdmin = currentUser.getRole().equals("ROLE_ADMIN");
        lastMessages = lastMessages.stream().filter(msg -> {
            User other = msg.getSender().getId().equals(currentUser.getId()) ? msg.getReceiver() : msg.getSender();
            if (isAdmin) {
                return other.getRole().equals("ROLE_USER"); // 管理员只显示买家
            } else {
                return other.getRole().equals("ROLE_ADMIN"); // 买家只显示管理员
            }
        }).collect(Collectors.toList());

        List<ConversationDTO> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        for (ChatMessage msg : lastMessages) {
            User other = msg.getSender().getId().equals(currentUser.getId()) ? msg.getReceiver() : msg.getSender();
            long unread = chatMessageRepo.countUnreadFromSender(currentUser, other);
            ConversationDTO dto = new ConversationDTO();
            dto.setOtherUserId(other.getId());
            dto.setOtherUserName(other.getDisplayName() != null ? other.getDisplayName() : other.getUsername());
            dto.setLastMessage(msg.getContent());
            dto.setLastTimestamp(msg.getTimestamp().format(fmt));
            dto.setUnreadCount(unread);
            list.add(dto);
        }
        return list;
    }

    @Transactional
    public void markAsRead(Long messageId) {
        ChatMessage msg = chatMessageRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("訊息不存在"));
        msg.setIsRead(true);
        chatMessageRepo.save(msg);
    }

    @Transactional
    public void markConversationAsRead(Long otherUserId) {
        User currentUser = getCurrentUser();
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        chatMessageRepo.markAsReadFromSender(currentUser, other);
    }

    public long getUnreadCount() {
        User currentUser = getCurrentUser();
        return chatMessageRepo.countByReceiverAndIsReadFalse(currentUser);
    }
}
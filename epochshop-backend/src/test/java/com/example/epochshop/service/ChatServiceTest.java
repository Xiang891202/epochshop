package com.example.epochshop.service;

import com.example.epochshop.entity.ChatMessage;
import com.example.epochshop.entity.User;
import com.example.epochshop.repository.ChatMessageRepository;
import com.example.epochshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ChatService chatService;

    private User sender;
    private User receiver;
    private ChatMessage message;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("testuser");
        sender.setRole("ROLE_USER");
        sender.setDisplayName("买家");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("admin");
        receiver.setRole("ROLE_ADMIN");
        receiver.setDisplayName("管理员");

        message = new ChatMessage();
        message.setId(1L);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent("你好");
        message.setTimestamp(LocalDateTime.now());
        message.setIsRead(false);

        // 模拟 SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void sendMessage_ShouldSaveAndReturn() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(chatMessageRepo.save(any(ChatMessage.class))).thenReturn(message);

        ChatMessage result = chatService.sendMessage(2L, null, "你好");

        assertThat(result.getSender()).isEqualTo(sender);
        assertThat(result.getReceiver()).isEqualTo(receiver);
        assertThat(result.getContent()).isEqualTo("你好");
        verify(chatMessageRepo).save(any(ChatMessage.class));
    }

    @Test
    void getConversation_ShouldReturnMessagesBetweenUsers() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sender));
        when(chatMessageRepo.findConversation(1L, 2L)).thenReturn(List.of(message));

        List<ChatMessage> messages = chatService.getConversation(2L);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("你好");
        verify(chatMessageRepo).findConversation(1L, 2L);
    }

    @Test
    void getConversationList_ShouldReturnFilteredLastMessages() {
        // 模拟当前用户是买家，只显示与卖家的对话
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sender));

        // 模拟仓库返回的最后消息（包含与自己的对话，应被过滤掉）
        ChatMessage selfMessage = new ChatMessage();
        selfMessage.setId(2L);
        selfMessage.setSender(sender);
        selfMessage.setReceiver(sender); // 自己发给自己，应被过滤
        selfMessage.setContent("不应该出现");
        selfMessage.setTimestamp(LocalDateTime.now());

        // 模拟一条正常发给管理员的最后消息
        message.setContent("管理员你好");
        when(chatMessageRepo.findLastMessagesByUser(1L)).thenReturn(List.of(selfMessage, message));
        when(chatMessageRepo.countUnreadFromSender(sender, receiver)).thenReturn(2L);

        List<com.example.epochshop.dto.ConversationDTO> list = chatService.getConversationList();

        // 应只包含管理员对话，没有自己发给自己的
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getOtherUserId()).isEqualTo(2L);
        assertThat(list.get(0).getOtherUserName()).isEqualTo("管理员");
        assertThat(list.get(0).getUnreadCount()).isEqualTo(2L);
    }

    @Test
    void markAsRead_ShouldSetIsReadTrue() {
        when(chatMessageRepo.findById(1L)).thenReturn(Optional.of(message));

        chatService.markAsRead(1L);

        assertThat(message.getIsRead()).isTrue();
        verify(chatMessageRepo).save(message);
    }

    @Test
    void getUnreadCount_ShouldReturnCorrectCount() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sender));
        when(chatMessageRepo.countByReceiverAndIsReadFalse(sender)).thenReturn(5L);

        long count = chatService.getUnreadCount();

        assertThat(count).isEqualTo(5L);
    }
}
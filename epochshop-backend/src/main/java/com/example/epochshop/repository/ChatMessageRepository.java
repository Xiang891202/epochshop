package com.example.epochshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epochshop.entity.ChatMessage;
import com.example.epochshop.entity.User;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.sender JOIN FETCH m.receiver WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id) ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // 当前用户的所有对话的最后一条消息
    @Query("SELECT m FROM ChatMessage m WHERE m.id IN " +
           "(SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.sender.id = :userId OR m2.receiver.id = :userId " +
           "GROUP BY CASE WHEN m2.sender.id = :userId THEN m2.receiver.id ELSE m2.sender.id END)")
    List<ChatMessage> findLastMessagesByUser(@Param("userId") Long userId);

    // 计算某用户未读消息总数
    long countByReceiverAndIsReadFalse(User receiver);

    // 计算与特定用户的未读消息数量
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver = :receiver AND m.sender = :sender AND m.isRead = false")
    long countUnreadFromSender(@Param("receiver") User receiver, @Param("sender") User sender);

    // 批量标记与某用户的所有未读消息为已读
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiver = :receiver AND m.sender = :sender AND m.isRead = false")
    int markAsReadFromSender(@Param("receiver") User receiver, @Param("sender") User sender);
}
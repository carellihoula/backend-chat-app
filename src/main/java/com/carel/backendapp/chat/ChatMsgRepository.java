package com.carel.backendapp.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMsgRepository extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findByChatId(String s);

    @Query("FROM ChatMessage chat WHERE (chat.senderId = :id OR chat.recipientId = :id)")
    List<ChatMessage> findChatsByCurrentUser(@Param("id") Integer id);
}

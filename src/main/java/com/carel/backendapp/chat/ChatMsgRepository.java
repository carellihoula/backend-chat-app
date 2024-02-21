package com.carel.backendapp.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMsgRepository extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findByChatId(String s);
}

package com.carel.backendapp.chatRoom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    Optional<ChatRoom> findBySenderIdAndRecipientId(Integer senderId, Integer recipientId);
}

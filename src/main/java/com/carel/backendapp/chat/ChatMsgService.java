package com.carel.backendapp.chat;


import com.carel.backendapp.chatRoom.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChatMsgService {

    private final ChatMsgRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage){
        var chatId = chatRoomService.getIdChatRoom(
                        chatMessage.getSenderId(),
                        chatMessage.getRecipientId(),
                        true
                )
                .orElseThrow(
                        () -> new NoSuchElementException("Aucune salle de chat trouvée pour les IDs donnés")
                );

        chatMessage.setChatId(chatId);
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> findChatMessages(
            Integer senderId,
            Integer recipientId
    ){
        var chatId = chatRoomService.getIdChatRoom(
                senderId,
                recipientId,
                false
        );

        return chatId.map(chatMessageRepository::findByChatId).orElse(new ArrayList<>());
    }

    public List<ChatMessage> findChatsByCurrentUser(Integer id){
        return chatMessageRepository.findChatsByCurrentUser(id);
    }
    public void deleteMessagesByChatId(String chatId) {
        chatMessageRepository.deleteByChatId(chatId);
    }

}

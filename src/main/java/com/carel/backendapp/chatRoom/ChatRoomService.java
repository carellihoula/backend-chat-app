package com.carel.backendapp.chatRoom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public Optional<String> getIdChatRoom(
        Integer senderId,
        Integer recipientId,
        boolean createNewRoomIfNotExist
    ){
        return chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId)
                .map(ChatRoom::getChatId)
                .or(() -> {
                        if(createNewRoomIfNotExist){
                            var chatId = createChatId(senderId, recipientId);
                        }
                        return Optional.empty();
                });

    }

    private String createChatId(Integer senderId, Integer recipientId) {
        String chatId = String.format("%s_%s", senderId, recipientId);

        ChatRoom.builder()
                .senderId(senderId)
                .recipientId(recipientId)
                .chatId(chatId)
                .build();

        ChatRoom.builder()
                .senderId(recipientId)
                .recipientId(senderId)
                .chatId(chatId)
                .build();

        return chatId;
    }
}

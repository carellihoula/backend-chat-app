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
        Optional<String> chatId = chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId)
                .map(ChatRoom::getChatId);
        if(chatId.isEmpty() && createNewRoomIfNotExist ){
            Optional.of(createChatId(senderId, recipientId));
        }
                /*.or(() -> {
                        if(createNewRoomIfNotExist){
                            var chatId = createChatId(senderId, recipientId);
                        }
                        return Optional.empty();
                });*/
        return chatId;
    }

    private String createChatId(Integer senderId, Integer recipientId) {
        String chatId = String.format("%s_%s", senderId, recipientId);

        ChatRoom senderToRecipient = ChatRoom.builder()
                .senderId(senderId)
                .recipientId(recipientId)
                .chatId(chatId)
                .build();

        ChatRoom recipientToSender = ChatRoom.builder()
                .senderId(recipientId)
                .recipientId(senderId)
                .chatId(chatId)
                .build();
        //save to database
        chatRoomRepository.save(senderToRecipient);
        chatRoomRepository.save(recipientToSender);
        return chatId;
    }
}

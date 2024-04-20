package com.carel.backendapp.chat;

import com.carel.backendapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatMsgController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMsgService chatMessageService;

    @MessageMapping("/chat")
    public void processMessage(
            @Payload ChatMessage chatMessage
    ){
        ChatMessage savedMsg = chatMessageService.save(chatMessage);

        ChatNotification notification = ChatNotification.builder()
                .id(savedMsg.getId())
                .chatId(savedMsg.getChatId())
                .read(savedMsg.isRead())
                .senderId(savedMsg.getSenderId())
                .recipientId(savedMsg.getRecipientId())
                .content(savedMsg.getContent())
                .timestamp(savedMsg.getTimestamp())
                .build();
        //To recipient
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId().toString(),
                "/queue/messages",
                notification
        );
        //To send if sender != recipient => Receive the msg that you sent
        if(!chatMessage.getSenderId().equals(chatMessage.getRecipientId())){
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getSenderId().toString(),
                    "/queue/messages",
                    notification
            );
        }
    } ;

    @MessageMapping("/chat/history")
    @SendToUser("/queue/history")
    public List<ChatMessage> fetchHistory(@Payload Map<String, Object> messagePayload){
        Integer userId = (Integer) messagePayload.get("userId");
        return chatMessageService.findChatsByCurrentUser(userId);
    }


    @GetMapping("api/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(
            @PathVariable Integer recipientId,
            @PathVariable Integer senderId
    ){
        return ResponseEntity.ok(
                chatMessageService.findChatMessages(senderId, recipientId)
        );
    }

    @GetMapping("api/messages/{currentUserId}")
    public ResponseEntity<List<ChatMessage>> findChatsByCurrentUser(
            @PathVariable Integer currentUserId
    ){
        return ResponseEntity.ok(
                chatMessageService.findChatsByCurrentUser(currentUserId)
        );
    }
    @DeleteMapping("api/messages/{chatId}")
    public ResponseEntity<String> deleteMessagesByChatId(
            @PathVariable String chatId
    ){
        try {
            chatMessageService.deleteMessagesByChatId(chatId);
            return ResponseEntity.ok("Messages deleted successfully.");
        } catch (Exception e) {
            // Capture et gestion  des exceptions spécifiques si nécessaire
            return ResponseEntity.internalServerError().body("An error occurred while deleting messages.");
        }
    }
}

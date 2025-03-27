package org.feynix.interfaces.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.feynix.application.chat.service.ChatService;
import org.feynix.interfaces.dto.ChatRequest;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/stream")
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        return chatService.streamChat(request.getMessage());
    }
} 
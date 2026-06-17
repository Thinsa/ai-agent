package com.yun.yunaiagent.chat;

import com.yun.yunaiagent.user.AppUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class ChatHistoryService {

    public static final String ROLE_USER = "user";

    public static final String ROLE_ASSISTANT = "assistant";

    private final ChatConversationRepository conversationRepository;

    private final ChatMessageRepository messageRepository;

    public ChatHistoryService(ChatConversationRepository conversationRepository, ChatMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ChatMessage appendUserMessage(String module, String chatId, String content, AppUser user) {
        return appendMessage(module, chatId, ROLE_USER, content, user);
    }

    @Transactional
    public ChatMessage appendAssistantMessage(String module, String chatId, String content, AppUser user) {
        return appendMessage(module, chatId, ROLE_ASSISTANT, content, user);
    }

    @Transactional(readOnly = true)
    public List<ChatDtos.SessionSummary> listSessions(String module, AppUser user) {
        if (user == null) {
            return List.of();
        }
        return conversationRepository.findByModuleAndUserOrderByUpdatedAtDesc(required(module, "module"), user)
                .stream()
                .map(ChatDtos.SessionSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatDtos.ConversationDetail getConversation(String chatId, AppUser user) {
        ChatConversation conversation = conversationRepository.findByChatId(required(chatId, "chatId"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        if (user != null && conversation.getUser() != null && !conversation.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        return ChatDtos.ConversationDetail.from(conversation, messageRepository.findByConversationOrderByCreatedAtAsc(conversation));
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> recentMessages(String module, String chatId, int limit) {
        return conversationRepository.findByModuleAndChatId(required(module, "module"), required(chatId, "chatId"))
                .map(conversation -> messageRepository.findByConversationOrderByCreatedAtDesc(conversation, PageRequest.of(0, Math.max(1, limit)))
                        .stream()
                        .sorted(Comparator.comparing(ChatMessage::getCreatedAt).thenComparing(ChatMessage::getId))
                        .toList())
                .orElseGet(List::of);
    }

    private ChatMessage appendMessage(String module, String chatId, String role, String content, AppUser user) {
        String normalizedModule = required(module, "module");
        String normalizedChatId = required(chatId, "chatId");
        String normalizedContent = required(content, "content");
        ChatConversation conversation = conversationRepository.findByModuleAndChatId(normalizedModule, normalizedChatId)
                .orElseGet(() -> conversationRepository.save(ChatConversation.create(normalizedModule, normalizedChatId, user, titleFrom(normalizedContent))));
        conversation.bindUserIfMissing(user);
        conversation.touch();
        ChatMessage message = ChatMessage.create(conversation, user, role, normalizedContent);
        return messageRepository.save(message);
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    private String titleFrom(String content) {
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 40) {
            return normalized;
        }
        return normalized.substring(0, 40);
    }
}

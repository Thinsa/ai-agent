package com.yun.yunaiagent.chat;

import com.yun.yunaiagent.common.ValidationUtils;
import com.yun.yunaiagent.constants.Constants;
import com.yun.yunaiagent.user.AppUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

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
        return conversationRepository.findByModuleAndUserOrderByUpdatedAtDesc(ValidationUtils.required(module, "module"), user)
                .stream()
                .map(ChatDtos.SessionSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatDtos.ConversationDetail getConversation(String chatId, AppUser user) {
        String normalizedChatId = ValidationUtils.required(chatId, "chatId");
        ChatConversation conversation = (user == null
                ? conversationRepository.findByChatIdAndUserIsNull(normalizedChatId)
                : conversationRepository.findByChatIdAndUser(normalizedChatId, user))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        return ChatDtos.ConversationDetail.from(conversation, messageRepository.findByConversationOrderByCreatedAtAsc(conversation));
    }

    @Transactional(readOnly = true)
    public ChatDtos.ConversationDetail getConversation(String module, String chatId, AppUser user) {
        ChatConversation conversation = findConversation(module, chatId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        return ChatDtos.ConversationDetail.from(conversation, messageRepository.findByConversationOrderByCreatedAtAsc(conversation));
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> recentMessages(String module, String chatId, int limit) {
        return recentMessages(module, chatId, limit, null);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> recentMessages(String module, String chatId, int limit, AppUser user) {
        return findConversation(module, chatId, user)
                .map(conversation -> messageRepository.findByConversationOrderByCreatedAtDesc(conversation, PageRequest.of(0, Math.max(1, limit)))
                        .stream()
                        .sorted(Comparator.comparing(ChatMessage::getCreatedAt).thenComparing(ChatMessage::getId))
                        .toList())
                .orElseGet(List::of);
    }

    private ChatMessage appendMessage(String module, String chatId, String role, String content, AppUser user) {
        String normalizedModule = ValidationUtils.required(module, "module");
        String normalizedChatId = ValidationUtils.required(chatId, "chatId");
        String normalizedContent = ValidationUtils.required(content, "content");
        ChatConversation conversation = findConversation(normalizedModule, normalizedChatId, user)
                .orElseGet(() -> conversationRepository.save(ChatConversation.create(normalizedModule, normalizedChatId, user, titleFrom(normalizedContent))));
        conversation.touch();
        ChatMessage message = ChatMessage.create(conversation, user, role, normalizedContent);
        return messageRepository.save(message);
    }

    private java.util.Optional<ChatConversation> findConversation(String module, String chatId, AppUser user) {
        String normalizedModule = ValidationUtils.required(module, "module");
        String normalizedChatId = ValidationUtils.required(chatId, "chatId");
        if (user == null) {
            return conversationRepository.findByModuleAndChatIdAndUserIsNull(normalizedModule, normalizedChatId);
        }
        return conversationRepository.findByModuleAndChatIdAndUser(normalizedModule, normalizedChatId, user);
    }

    private String titleFrom(String content) {
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= Constants.CONVERSATION_TITLE_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, Constants.CONVERSATION_TITLE_MAX_LENGTH);
    }
}

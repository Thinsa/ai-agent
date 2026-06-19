package com.yun.yunaiagent.chat;

import com.yun.yunaiagent.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByModuleAndChatIdAndUser(String module, String chatId, AppUser user);

    Optional<ChatConversation> findByModuleAndChatIdAndUserIsNull(String module, String chatId);

    Optional<ChatConversation> findByChatIdAndUser(String chatId, AppUser user);

    Optional<ChatConversation> findByChatIdAndUserIsNull(String chatId);

    List<ChatConversation> findByModuleAndUserOrderByUpdatedAtDesc(String module, AppUser user);
}

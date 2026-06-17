package com.yun.yunaiagent.chat;

import com.yun.yunaiagent.user.AppUser;
import com.yun.yunaiagent.user.UserDtos;
import com.yun.yunaiagent.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat-history-service-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.ai.openai.api-key=test-openai-key",
        "spring.ai.dashscope.api-key=test-dashscope-key",
        "spring.autoconfigure.exclude=org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration",
        "app.jwt.secret=test-secret-key-test-secret-key-test-secret-key",
        "app.jwt.expiration=3600000"
})
class ChatHistoryServiceTest {

    @Autowired
    ChatHistoryService chatHistoryService;

    @Autowired
    UserService userService;

    @Test
    void savesListsAndLoadsConversationHistoryForOneUser() {
        AppUser user = userService.register(new UserDtos.RegisterRequest("history-user", "password123", "History User", "history@example.com"));
        AppUser otherUser = userService.register(new UserDtos.RegisterRequest("other-user", "password123", "Other User", "other@example.com"));

        chatHistoryService.appendUserMessage("love", "love_abc", "Java 是什么？", user);
        chatHistoryService.appendAssistantMessage("love", "love_abc", "Java 是一种编程语言。", user);
        chatHistoryService.appendUserMessage("super", "super_abc", "生成报告", otherUser);

        List<ChatDtos.SessionSummary> sessions = chatHistoryService.listSessions("love", user);
        assertThat(sessions).hasSize(1);
        assertThat(sessions.getFirst().chatId()).isEqualTo("love_abc");
        assertThat(sessions.getFirst().module()).isEqualTo("love");
        assertThat(sessions.getFirst().title()).isEqualTo("Java 是什么？");

        ChatDtos.ConversationDetail detail = chatHistoryService.getConversation("love_abc", user);
        assertThat(detail.chatId()).isEqualTo("love_abc");
        assertThat(detail.messages()).extracting(ChatDtos.MessageResponse::role)
                .containsExactly("user", "assistant");
        assertThat(detail.messages()).extracting(ChatDtos.MessageResponse::content)
                .containsExactly("Java 是什么？", "Java 是一种编程语言。");

        assertThat(chatHistoryService.recentMessages("love", "love_abc", 20))
                .extracting(ChatMessage::getContent)
                .containsExactly("Java 是什么？", "Java 是一种编程语言。");
    }
}

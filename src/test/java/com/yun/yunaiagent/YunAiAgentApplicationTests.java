package com.yun.yunaiagent;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:context-loads;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.ai.openai.api-key=test-openai-key",
        "spring.ai.model.embedding=dashscope",
        "spring.ai.dashscope.api-key=test-dashscope-key",
        "spring.autoconfigure.exclude=org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration",
        "app.jwt.secret=test-secret-key-test-secret-key-test-secret-key"
})
class YunAiAgentApplicationTests {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Test
    void embeddingModelUsesDashScopeOnly() {
        assertThat(applicationContext.getBean(EmbeddingModel.class))
                .isInstanceOf(DashScopeEmbeddingModel.class);
        assertThat(applicationContext.containsBean("openAiEmbeddingModel")).isFalse();
    }
}

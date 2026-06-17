package com.yun.yunaiagent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoveAppRagServiceTest {

    @Test
    void retrieveContextUsesVectorStoreMatches() {
        FakeVectorStore vectorStore = new FakeVectorStore();
        LoveAppRagService service = new LoveAppRagService(
                new LoveAppDocumentLoader(),
                new QueryRewriter(new FakeChatModel()),
                provider(vectorStore)
        );

        String context = service.retrieveContext("how to communicate");

        assertThat(context).contains("retrieved relationship context");
        assertThat(vectorStore.lastRequest).isNotNull();
        assertThat(vectorStore.lastRequest.getTopK()).isEqualTo(4);
    }

    @Test
    void startupIndexesClasspathDocumentsWhenVectorStoreExists() {
        FakeVectorStore vectorStore = new FakeVectorStore();
        LoveAppRagService service = new LoveAppRagService(
                new LoveAppDocumentLoader(),
                new QueryRewriter(new FakeChatModel()),
                provider(vectorStore)
        );

        service.run(null);

        assertThat(vectorStore.addedDocuments).isNotEmpty();
    }

    private static ObjectProvider<VectorStore> provider(VectorStore vectorStore) {
        return new ObjectProvider<>() {
            @Override
            public VectorStore getObject(Object... args) {
                return vectorStore;
            }

            @Override
            public VectorStore getIfAvailable() {
                return vectorStore;
            }

            @Override
            public VectorStore getIfUnique() {
                return vectorStore;
            }

            @Override
            public VectorStore getObject() {
                return vectorStore;
            }
        };
    }

    private static class FakeVectorStore implements VectorStore {

        private final List<Document> addedDocuments = new ArrayList<>();

        private SearchRequest lastRequest;

        @Override
        public String getName() {
            return "fake";
        }

        @Override
        public void add(List<Document> documents) {
            addedDocuments.addAll(documents);
        }

        @Override
        public void delete(List<String> idList) {
        }

        @Override
        public void delete(org.springframework.ai.vectorstore.filter.Filter.Expression filterExpression) {
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            this.lastRequest = request;
            return List.of(new Document("retrieved relationship context"));
        }
    }

    private static class FakeChatModel implements ChatModel {

        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage("rewritten query"))));
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            return Flux.empty();
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return null;
        }
    }
}

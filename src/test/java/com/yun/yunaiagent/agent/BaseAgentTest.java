package com.yun.yunaiagent.agent;

import com.yun.yunaiagent.agent.model.AgentState;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class BaseAgentTest {

    @Test
    void runStreamCompletesQuietlyWhenClientDisconnects() throws Exception {
        DisconnectingEmitter emitter = new DisconnectingEmitter();
        TestAgent agent = new TestAgent(emitter);

        agent.runStream("generate image");

        assertThat(emitter.awaitSend()).isTrue();
        assertThat(agent.awaitFinished()).isTrue();
        assertThat(emitter.completedWithError).isFalse();
        assertThat(agent.currentState()).isEqualTo(AgentState.FINISHED);
    }

    static class TestAgent extends BaseAgent {

        private final SseEmitter emitter;

        private final CountDownLatch finished = new CountDownLatch(1);

        TestAgent(SseEmitter emitter) {
            this.emitter = emitter;
        }

        @Override
        protected SseEmitter createEmitter(long timeoutMillis) {
            return emitter;
        }

        @Override
        protected String step(String userPrompt, int stepNumber) {
            state = AgentState.FINISHED;
            return "图片生成成功：https://oss.example/generated.png";
        }

        @Override
        protected void afterRun(String userPrompt, String result) {
            finished.countDown();
        }

        AgentState currentState() {
            return state;
        }

        boolean awaitFinished() throws InterruptedException {
            return finished.await(1, TimeUnit.SECONDS);
        }
    }

    static class DisconnectingEmitter extends SseEmitter {

        private final CountDownLatch sendAttempted = new CountDownLatch(1);

        private volatile boolean completedWithError;

        @Override
        public void send(Object object) throws IOException {
            sendAttempted.countDown();
            throw new IOException("你的主机中的软件中止了一个已建立的连接。");
        }

        @Override
        public void completeWithError(Throwable ex) {
            completedWithError = true;
        }

        boolean awaitSend() throws InterruptedException {
            return sendAttempted.await(1, TimeUnit.SECONDS);
        }
    }
}

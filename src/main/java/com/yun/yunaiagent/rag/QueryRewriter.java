package com.yun.yunaiagent.rag;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 用户查询改写器。
 *
 * <p>用于把口语化问题改写成更适合检索的查询表达。</p>
 */
@Component
public class QueryRewriter {

    private final ChatModel chatModel;

    public QueryRewriter(@Qualifier("openAiChatModel") ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 改写用户查询；当前保持原样返回。
     */
    public String rewrite(String query) {
        if (query == null || query.isBlank()) {
            return "空问题";
        }
        return chatModel.call("请将下面的用户问题改写为适合知识库检索的短查询，只返回改写后的查询：" + query.trim());
    }
}

package com.yun.yunaiagent.tools;

import com.yun.yunaiagent.constants.Constants;
import org.jsoup.Jsoup;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 网页抓取工具占位实现。
 *
 * <p>后续可基于 jsoup 拉取网页正文，并补充 robots、超时和内容清洗逻辑。</p>
 */
@Component
public class WebScrapingTool implements AgentTool {

    @Override
    public String name() {
        return "scrapeWebPage";
    }

    @Override
    public String description() {
        return "网页抓取工具骨架";
    }

    /**
     * 抓取指定网页内容的预留方法。
     */
    @Tool(description = "抓取网页并返回正文文本")
    public String scrapeWebPage(String url) {
        try {
            return Jsoup.connect(url)
                    .timeout((int) (long) Constants.WEB_SCRAPE_TIMEOUT_MS)
                    .get()
                    .body()
                    .text();
        } catch (Exception e) {
            return "网页抓取失败：" + e.getMessage();
        }
    }
}

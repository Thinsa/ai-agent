package com.yun.yuimagesearchmcpserver.tools;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ImageSearchToolTest {

    @Test
    void returnsClearErrorWhenPexelsApiKeyIsMissing() {
        ImageSearchTool tool = new ImageSearchTool("", "https://api.pexels.com/v1/search");

        String result = tool.searchImage("cat");

        assertThat(result).contains("PEXELS_API_KEY");
    }

    @Test
    void returnsMediumImageUrlsFromPexelsResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(header("Authorization", "test-key"))
                .andExpect(queryParam("query", "cat"))
                .andRespond(withSuccess("""
                        {
                          "photos": [
                            {"src": {"medium": "https://images.example/cat-1.jpeg"}},
                            {"src": {"medium": "https://images.example/cat-2.jpeg"}}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));
        ImageSearchTool tool = new ImageSearchTool("test-key", "https://api.pexels.test/v1/search", builder.build());

        String result = tool.searchImage("cat");

        assertThat(result).contains("https://images.example/cat-1.jpeg");
        assertThat(result).contains("https://images.example/cat-2.jpeg");
        server.verify();
    }
}

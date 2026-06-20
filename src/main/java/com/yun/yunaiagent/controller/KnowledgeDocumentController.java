package com.yun.yunaiagent.controller;

import com.yun.yunaiagent.rag.KnowledgeDocumentService;
import com.yun.yunaiagent.rag.KnowledgeDocumentService.CreateDocumentRequest;
import com.yun.yunaiagent.rag.KnowledgeDocumentService.DocumentCountDto;
import com.yun.yunaiagent.rag.KnowledgeDocumentService.KnowledgeDocumentDto;
import com.yun.yunaiagent.rag.KnowledgeDocumentService.ReindexResultDto;
import com.yun.yunaiagent.rag.KnowledgeDocumentService.UpdateDocumentRequest;
import com.yun.yunaiagent.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/knowledge-documents")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService documentService;

    private final UserService userService;

    public KnowledgeDocumentController(KnowledgeDocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KnowledgeDocumentDto> listDocuments(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly,
            Authentication authentication) {
        requireAuth(authentication);
        return documentService.listAll(enabledOnly);
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentCountDto getCount(Authentication authentication) {
        requireAuth(authentication);
        return documentService.getCount();
    }

    @GetMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getCategories(Authentication authentication) {
        requireAuth(authentication);
        return documentService.getCategories();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public KnowledgeDocumentDto createDocument(@RequestBody Map<String, String> body, Authentication authentication) {
        requireAuth(authentication);
        String title = body.get("title");
        String content = body.get("content");
        String category = body.get("category");
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }
        if (category == null || category.isBlank()) {
            category = "自定义";
        }
        return documentService.create(new CreateDocumentRequest(title.trim(), content.trim(), category.trim()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public KnowledgeDocumentDto updateDocument(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        requireAuth(authentication);
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String category = (String) body.get("category");
        boolean enabled = body.containsKey("enabled") ? (Boolean) body.get("enabled") : true;
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }
        if (category == null || category.isBlank()) {
            category = "自定义";
        }
        return documentService.update(id, new UpdateDocumentRequest(title.trim(), content.trim(), category.trim(), enabled));
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable Long id, Authentication authentication) {
        requireAuth(authentication);
        documentService.delete(id);
    }

    @PostMapping("/reindex")
    public ReindexResultDto reindex(Authentication authentication) {
        requireAuth(authentication);
        return documentService.reindex();
    }

    private void requireAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
    }
}

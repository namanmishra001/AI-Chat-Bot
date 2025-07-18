package com.ai.bot.controller;

import com.ai.bot.util.ResponseSanitizer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ai.bot.constants.Prompts.APEX_SYSTEM_PROMPT;

@RestController
@RequestMapping("/api/apex")
public class ApexController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    ApexController(VectorStore vectorStore, ChatClient.Builder chatClient){
        this.vectorStore = vectorStore;
        this.chatClient = chatClient
                // retrieval-based responses
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping
    public String chat(@RequestParam(value = "prompt") String prompt) {
        List<Document> relatedDocuments = vectorStore.similaritySearch(prompt);
        String documents = relatedDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
        String userPrompt = "Follow these strict instructions:\n"+ APEX_SYSTEM_PROMPT + "\n\nUser Query: "+ prompt;
        String rawResponse = this.chatClient
                .prompt()
                .system(s -> s.text(APEX_SYSTEM_PROMPT).params(Map.of("documents", documents)))
                .user(userPrompt)
                .call()
                .content();
        return ResponseSanitizer.sanitize(rawResponse);
    }
}

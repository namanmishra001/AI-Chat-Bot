package com.ai.bot.controller;

import com.ai.bot.util.ResponseSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.ai.bot.constants.Prompts.*;

@RestController
@RequestMapping("/api/prompt")
@Slf4j
public class GymBotController {


    private final Map<String, String> cache = new ConcurrentHashMap<>();

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    public GymBotController(ChatClient.Builder chatClient,  VectorStore vectorStore, EmbeddingModel embeddingModel) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.chatClient = chatClient
                // retrieval-based responses
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    //@PostConstruct
    public void initializeIndex() {
        List<String> gymData = List.of(
                "Monday: Chest & Triceps",
                "Tuesday: Back & Biceps",
                "Wednesday: Rest Day",
                "Thursday: Legs & Shoulders",
                "Friday: Cardio & Core",
                "Saturday: Active Recovery",
                "Sunday: Full Body Workout"
        );

        reindexData(gymData, embeddingModel);
    }

    @CrossOrigin(origins = "*")
    @GetMapping
    public String simplify(@RequestParam(value = "question",
            defaultValue = "List all the Gym Schedule") String question) {
        System.out.println("API is working fine. Received: " + question);
        long startTime = System.nanoTime();
        if (cache.containsKey(question)) {
            System.out.println("Returning response from cache, for question: " + question);
            return cache.get(question);
        }
        List<Document> relevantDocs = vectorStore.similaritySearch(question);
        String documents = relevantDocs.stream()
                .limit(1)
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        String generatedPrompt = LLM_INSTRUCTION + LLM_PROMPT.formatted(documents, question) + QUERY + question;

        // Get AI response
        String rawResponse = this.chatClient
                .prompt()
                //.system(s -> s.text(LLM_PROMPT.formatted(documents, question)).params(Map.of("documents", documents)))
                .system(s -> s.text(documents))
                .user(generatedPrompt)
                .call()
                .content();
        long endTime = System.nanoTime();
        long durationInNano = endTime - startTime;

        System.out.println("Time taken: " + (durationInNano / 1_000_000_000.0) + " seconds");

        String sanitizedResponse = ResponseSanitizer.sanitize(rawResponse);
        if (!sanitizedResponse.toLowerCase().contains("Opps!")) {
            cache.put(question, sanitizedResponse);
        } else {
            System.out.println("Skipped caching due to response containing 'oops'");
        }
        return sanitizedResponse;
    }

    public void reindexData(List<String> gymSchedules, EmbeddingModel embeddingModel) {
        List<Document> documents = new ArrayList<>(); // Store multiple documents
        for (String gymSchedule : gymSchedules) {
            // Convert float[] to List<Double>
            float[] embeddingArray = embeddingModel.embed(gymSchedule);
            List<Double> embedding = new ArrayList<>();
            for (float f : embeddingArray) {
                // Convert float to Double
                embedding.add((double) f);
            }
            // Store embeddings in metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("embedding", embedding);
            // Use the correct constructor
            String id = UUID.randomUUID().toString();
            Document doc = new Document(id, gymSchedule, metadata);
            // Add to the list
            documents.add(doc);
        }
        //save documents into the vector store
        vectorStore.accept(documents);
    }


    @Scheduled(fixedRate = 30000)
    public void keepWarm() {
        chatClient.prompt().user("Ping").call();
    }
}

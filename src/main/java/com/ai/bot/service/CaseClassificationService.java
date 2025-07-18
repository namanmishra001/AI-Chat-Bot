package com.ai.bot.service;

import com.ai.bot.model.CaseVector;
import com.ai.bot.repository.CaseVectorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class CaseClassificationService {

    private final CaseVectorRepository caseVectorRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OLLAMA_API_URL = "http://localhost:11434/api/embeddings";

    public CaseClassificationService(CaseVectorRepository caseVectorRepository) {
        this.caseVectorRepository = caseVectorRepository;
    }

    /**
     * Populates the vector database with technical and non-technical case vectors
     */

    @PostConstruct
    public void populateInitialVectors() {
        // Define some sample sentences for classification
        List<String> technicalSentences = Arrays.asList(
                "The server is down and needs to be rebooted.",
                "Invitation failure with blank error msg",
                "The application crashed due to a null pointer exception."
        );
        List<String> nonTechnicalSentences = Arrays.asList(
                "How do I update address?",
                "Can I change my account password?",
                "How to fill partner profile",
                "How do I add contact",
                "I need help with 3rd party "
        );
        // Store embeddings for each technical sentence
        for (String sentence : technicalSentences) {
            storeEmbedding(sentence, "Technical");
        }
        // Store embeddings for each non-technical sentence
        for (String sentence : nonTechnicalSentences) {
            storeEmbedding(sentence, "Non-Technical");
        }
        System.out.println("Sample labels loaded in Vector db for processing");
    }

    /**
     * Classifies a case as Technical or Non-Technical based on vector similarity
     */
    public String classifyCase(String inputText) {
        float[] inputVector = getEmbedding(inputText);
        inputVector = normalizeVector(inputVector);

        List<CaseVector> caseVectors = caseVectorRepository.findAll();
        if (caseVectors.isEmpty()) {
            System.out.println("No stored cases found. Defaulting to Non-Technical.");
            return "Non-Technical";
        }
        String classification = "Non-Technical";
        double highestSimilarity = -1.0;
        for (CaseVector caseVector : caseVectors) {
            float[] storedVector = normalizeVector(caseVector.getVector());
            double similarity = cosineSimilarity(inputVector, storedVector);

            System.out.print("Similarity with " + caseVector.getLabel() + ": " + similarity);

            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                classification = caseVector.getLabel();
            }
        }
        System.out.println("Final classification: " + classification);
        return classification;
    }

    /**
     * Normalizes vector to unit length
     */
    private float[] normalizeVector(float[] vector) {
        double norm = 0.0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        if (norm == 0) return vector;

        float[] normalizedVector = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalizedVector[i] = (float) (vector[i] / norm);
        }
        return normalizedVector;
    }

    /**
     * Computes Cosine Similarity between two vectors
     */
    private double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vectors must be of the same length for cosine similarity.");
        }

        double dotProduct = 0.0;
        double normVec1 = 0.0;
        double normVec2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            normVec1 += Math.pow(vec1[i], 2);
            normVec2 += Math.pow(vec2[i], 2);
        }

        return dotProduct / (Math.sqrt(normVec1) * Math.sqrt(normVec2));
    }

    /**
     * Generates embedding for input text using Ollama
     */
    public float[] getEmbedding(String text) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> payload = new HashMap<>();
            payload.put("prompt", text);
            payload.put("model", "deepseek-r1:1.5b");

            String response = restTemplate.postForObject(OLLAMA_API_URL, payload, String.class);
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            // Extract embedding
            List<Number> vectorList = (List<Number>) responseMap.get("embedding");
            float[] embedding = new float[vectorList.size()];
            for (int i = 0; i < vectorList.size(); i++) {
                embedding[i] = vectorList.get(i).floatValue();
            }
            return embedding;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching embedding from Ollama");
        }
    }

    /**
     * Helper method to store embeddings in the database
     */
    private void storeEmbedding(String text, String label) {
        float[] embedding = getEmbedding(text);

        // Create a new CaseVector object and save to the database
        CaseVector caseVector = new CaseVector();
        caseVector.setLabel(label);
        caseVector.setVector(embedding);
        caseVectorRepository.save(caseVector);
    }
}
package com.ai.bot.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResponseSanitizer {
    public static String sanitize(String rawResponse) {
            String cleaned = rawResponse
                    .replaceAll("(?s)<think>.*?</think>", "") // strip <think>
                    .replaceAll("[*#]", "")                  // remove markdown bullets
                    .replaceAll("\\[.*?\\]", "")             // strip square brackets
                    .replaceAll("\\s{2,}", " ")              // collapse extra spaces
                    .trim();
            System.out.println("Sanitization of response is done");
            return cleaned;
        }
    }

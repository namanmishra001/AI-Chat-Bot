package com.ai.bot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {

    // This maps to chatbot.html in src/main/resources/templates
    @GetMapping("/chatbot")
    public String showChatbotPage() {

        return "chatbot";
    }
}
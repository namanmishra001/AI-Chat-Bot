package com.ai.bot.controller;

import com.ai.bot.dto.CaseRequest;
import com.ai.bot.service.CaseClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CaseClassifierController {

    @Autowired
    private CaseClassificationService caseClassificationService;

    @PostMapping("/classify-case")
    public String classifyCase(@RequestBody CaseRequest caseRequest) {
        String description = caseRequest.getDescription();
        return caseClassificationService.classifyCase(description);
    }
}
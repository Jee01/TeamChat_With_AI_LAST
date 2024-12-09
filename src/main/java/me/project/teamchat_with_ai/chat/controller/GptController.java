package me.project.teamchat_with_ai.chat.controller;

import me.project.teamchat_with_ai.chat.service.OpenAiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gpt")
public class GptController {

    private final OpenAiService openAiService;

    public GptController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String prompt) {
        try {
            String answer = openAiService.getCompletion(prompt);
            return answer;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }
}



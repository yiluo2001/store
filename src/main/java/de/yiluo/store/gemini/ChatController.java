package de.yiluo.store.gemini;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final GeminiService geminiService;

    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        // Use request.message() instead of request.getMessage()
        String aiReply = geminiService.chat(request.message());
        return new ChatResponse(aiReply);
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String reply) {}
}
package org.SmartAI_Agent.api;

import org.SmartAI_Agent.api.dto.ChatRequest;
import org.SmartAI_Agent.api.dto.ChatResponse;
import org.SmartAI_Agent.core.MainOrchestrator;
import org.SmartAI_Agent.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://10.0.2.2:8080", "http://localhost:8080"})
public class SmartHomeController {

    private final MainOrchestrator orchestrator;

    @Autowired
    public SmartHomeController(MainOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String userId = request.getUserId() != null ? request.getUserId() : "webUser";
        Response response = orchestrator.processUserRequest(request.getMessage(), userId);
        return new ChatResponse(
            response.getMessage(),
            response.isRequiresConfirmation(),
            response.isSuccess()
        );
    }

    @PostMapping("/confirm")
    public ChatResponse confirm(@RequestBody Map<String, String> request) {
        String userId = request.getOrDefault("userId", "webUser");
        String action = request.getOrDefault("action", "confirm");
        
        Response response;
        if ("confirm".equalsIgnoreCase(action) || "yes".equalsIgnoreCase(action)) {
            response = orchestrator.processConfirmedCommand(userId);
        } else {
            response = orchestrator.cancelPendingCommand(userId);
        }
        
        return new ChatResponse(
            response.getMessage(),
            response.isRequiresConfirmation(),
            response.isSuccess()
        );
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return orchestrator.getSystemStatus();
    }
}

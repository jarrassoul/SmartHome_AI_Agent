package org.SmartAI_Agent.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.SmartAI_Agent.models.Intent;
import org.SmartAI_Agent.models.Response;

/**
 * ResponseBuilder - Formats and builds user-friendly responses
 * Converts agent responses into natural language
 */
public class ResponseBuilder {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // Response templates for different scenarios
    private static final Map<String, String> RESPONSE_TEMPLATES = new HashMap<>();

    static {
        initializeTemplates();
    }

    /**
     * Build a simple response
     */
    public Response buildResponse(boolean success, String message) {
        return buildResponse(success, message, "NORMAL");
    }

    /**
     * Build a response with priority
     */
    public Response buildResponse(boolean success, String message, String priority) {
        Response response = new Response();
        response.setSuccess(success);
        response.setMessage(formatMessage(message));
        response.setPriority(priority);
        response.setTimestamp(new Date());
        response.setResponseTime(System.currentTimeMillis());

        System.out.println("Built response: success=" + success + ", message=" + message);

        return response;
    }

    /**
     * Build an error response
     */
    public Response buildErrorResponse(String errorMessage) {
        Response response = new Response();
        response.setSuccess(false);
        response.setMessage("[ERROR] Sorry, I encountered an error: " + errorMessage);
        response.setPriority("HIGH");
        response.setTimestamp(new Date());
        response.setError(errorMessage);

        System.err.println("Error response built: " + errorMessage);

        return response;
    }

    /**
     * Build a scenario response
     */
    public Response buildScenarioResponse(String scenario, List<Response> agentResponses, String message) {
        Response response = new Response();

        // Check if all operations succeeded
        boolean allSuccess = agentResponses.stream().allMatch(Response::isSuccess);
        long successCount = agentResponses.stream().filter(Response::isSuccess).count();

        response.setSuccess(allSuccess);
        response.setScenario(scenario);
        response.setTimestamp(new Date());

        // Build detailed message
        StringBuilder details = new StringBuilder();
        details.append(message).append("\n\n");

        if (allSuccess) {
            details.append("[SUCCESS] All operations completed successfully!\n");
        } else {
            details.append(String.format("[WARNING] %d of %d operations completed.\n",
                    successCount, agentResponses.size()));
        }

        // Add details from each agent
        details.append(buildAgentDetails(agentResponses));

        response.setMessage(details.toString());
        response.setDetails(extractDetails(agentResponses));

        System.out.println("Scenario '" + scenario + "' response built: " + successCount + "/" + agentResponses.size() + " successful");

        return response;
    }

    /**
     * Aggregate multiple responses
     */
    public Response aggregateResponses(List<Response> responses, Intent intent) {
        if (responses.isEmpty()) {
            return buildErrorResponse("No responses received from agents");
        }

        if (responses.size() == 1) {
            return responses.get(0);
        }

        Response aggregated = new Response();

        // Check overall success
        boolean allSuccess = responses.stream().allMatch(Response::isSuccess);
        long successCount = responses.stream().filter(Response::isSuccess).count();

        aggregated.setSuccess(allSuccess);
        aggregated.setTimestamp(new Date());

        // Build aggregated message
        String mainMessage = buildAggregatedMessage(intent, responses, allSuccess, successCount);
        aggregated.setMessage(mainMessage);

        // Combine all details
        Map<String, Object> combinedDetails = new HashMap<>();
        responses.forEach(r -> {
            if (r.getDetails() != null) {
                String agentName = r.getAgentId() != null ? r.getAgentId() : "unknown";
                combinedDetails.put(agentName, r.getDetails());
            }
        });
        aggregated.setDetails(combinedDetails);

        // Set priority based on any high priority responses
        if (responses.stream().anyMatch(r -> "CRITICAL".equals(r.getPriority()))) {
            aggregated.setPriority("CRITICAL");
        } else if (responses.stream().anyMatch(r -> "HIGH".equals(r.getPriority()))) {
            aggregated.setPriority("HIGH");
        } else {
            aggregated.setPriority("NORMAL");
        }

        System.out.println("Aggregated " + responses.size() + " responses: " + successCount + "/" + responses.size() + " successful");

        return aggregated;
    }

    /**
     * Build a natural language message based on intent
     */
    public Response buildIntentResponse(Intent intent, boolean success) {
        Response response = new Response();
        response.setSuccess(success);
        response.setTimestamp(new Date());

        String action = intent.getAction();
        String domain = intent.getDomain();
        Map<String, Object> params = intent.getParameters();

        // Get appropriate template
        String template = RESPONSE_TEMPLATES.getOrDefault(action,
                success ? "[SUCCESS] Command executed successfully." : "[FAILED] Failed to execute command.");

        // Replace placeholders
        String message = template;
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                message = message.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }

        // Add time context
        message = addTimeContext(message);

        // Add friendly touch
        message = addFriendlyTouch(message, success);

        response.setMessage(message);

        return response;
    }

    /**
     * Format message with appropriate prefixes
     */
    private String formatMessage(String message) {
        // Add appropriate prefix based on content
        if (message.toLowerCase().contains("emergency")) {
            return "[EMERGENCY] " + message;
        }
        if (message.toLowerCase().contains("locked")) {
            return "[SECURITY] " + message;
        }
        if (message.toLowerCase().contains("unlocked")) {
            return "[SECURITY] " + message;
        }
        if (message.toLowerCase().contains("temperature")) {
            return "[CLIMATE] " + message;
        }
        if (message.toLowerCase().contains("light")) {
            return "[APPLIANCE] " + message;
        }
        if (message.toLowerCase().contains("coffee")) {
            return "[APPLIANCE] " + message;
        }
        if (message.toLowerCase().contains("morning")) {
            return "[ROUTINE] " + message;
        }
        if (message.toLowerCase().contains("night")) {
            return "[ROUTINE] " + message;
        }

        return message;
    }

    /**
     * Build aggregated message from multiple responses
     */
    private String buildAggregatedMessage(Intent intent, List<Response> responses,
                                          boolean allSuccess, long successCount) {
        StringBuilder message = new StringBuilder();

        // Main status
        if (allSuccess) {
            message.append("[SUCCESS] All systems responded successfully!\n\n");
        } else if (successCount > 0) {
            message.append(String.format("[PARTIAL] Partial success: %d of %d operations completed.\n\n",
                    successCount, responses.size()));
        } else {
            message.append("[FAILED] Operation failed. Please try again.\n\n");
        }

        // Add individual responses
        for (Response r : responses) {
            String agentName = formatAgentName(r.getAgentId());
            String status = r.isSuccess() ? "[OK]" : "[FAIL]";
            String agentMessage = r.getMessage() != null ? r.getMessage() : "No message";

            message.append(String.format("%s %s: %s\n", status, agentName, agentMessage));
        }

        return message.toString().trim();
    }

    /**
     * Build details from agent responses
     */
    private String buildAgentDetails(List<Response> responses) {
        StringBuilder details = new StringBuilder();

        for (Response r : responses) {
            String agentName = formatAgentName(r.getAgentId());
            String status = r.isSuccess() ? "[OK]" : "[FAIL]";

            details.append(String.format("\n%s %s:", status, agentName));
            if (r.getMessage() != null && !r.getMessage().isEmpty()) {
                details.append(" ").append(r.getMessage());
            }

            // Add specific details
            if (r.getDetails() != null && r.getDetails() instanceof Map) {
                Map<String, Object> agentDetails = (Map<String, Object>) r.getDetails();
                if (!agentDetails.isEmpty()) {
                    details.append("\n   Details: ");
                    agentDetails.forEach((key, value) ->
                            details.append(String.format("%s=%s ", key, value)));
                }
            }
        }

        return details.toString();
    }

    /**
     * Extract details from responses
     */
    private Map<String, Object> extractDetails(List<Response> responses) {
        Map<String, Object> details = new HashMap<>();

        for (Response r : responses) {
            String agentName = r.getAgentId() != null ? r.getAgentId() : "unknown";

            Map<String, Object> agentInfo = new HashMap<>();
            agentInfo.put("success", r.isSuccess());
            agentInfo.put("message", r.getMessage());

            if (r.getDetails() != null) {
                agentInfo.put("data", r.getDetails());
            }

            details.put(agentName, agentInfo);
        }

        details.put("total_agents", responses.size());
        details.put("successful", responses.stream().filter(Response::isSuccess).count());

        return details;
    }

    /**
     * Format agent name for display
     */
    private String formatAgentName(String agentId) {
        if (agentId == null) return "System";

        switch (agentId.toLowerCase()) {
            case "security":
                return "Security System";
            case "appliance":
                return "Appliance Control";
            case "climate":
                return "Climate Control";
            default:
                return agentId.substring(0, 1).toUpperCase() + agentId.substring(1);
        }
    }

    /**
     * Add time context to message
     */
    private String addTimeContext(String message) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        // Don't add time context to all messages, just relevant ones
        if (message.toLowerCase().contains("morning") ||
                message.toLowerCase().contains("night") ||
                message.toLowerCase().contains("routine")) {
            return message + " (Current time: " + now.format(TIME_FORMAT) + ")";
        }

        return message;
    }

    /**
     * Add friendly touch to messages
     */
    private String addFriendlyTouch(String message, boolean success) {
        if (!success) {
            return message + " Please let me know if you need help!";
        }

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        // Add time-appropriate greetings
        if (hour >= 5 && hour < 12 && message.toLowerCase().contains("morning")) {
            return message + " Have a great day!";
        }
        if (hour >= 21 || hour < 5 && message.toLowerCase().contains("night")) {
            return message + " Sleep well!";
        }

        return message;
    }

    /**
     * Initialize response templates
     */
    private static void initializeTemplates() {
        // Security templates
        RESPONSE_TEMPLATES.put("lock_doors", "[SECURITY] All doors have been locked. Your home is secure.");
        RESPONSE_TEMPLATES.put("unlock_doors", "[SECURITY] Doors unlocked. Welcome!");
        RESPONSE_TEMPLATES.put("arm_security", "[SECURITY] Security system armed. Your home is protected.");
        RESPONSE_TEMPLATES.put("disarm_security", "[SECURITY] Security system disarmed. Welcome home!");
        RESPONSE_TEMPLATES.put("show_camera", "[SECURITY] Camera feed is now available.");

        // Appliance templates
        RESPONSE_TEMPLATES.put("turn_on_lights", "[APPLIANCE] Lights are now on.");
        RESPONSE_TEMPLATES.put("turn_off_lights", "[APPLIANCE] Lights have been turned off.");
        RESPONSE_TEMPLATES.put("brew_coffee", "[APPLIANCE] Your coffee is brewing! It'll be ready in a few minutes.");
        RESPONSE_TEMPLATES.put("dim_lights", "[APPLIANCE] Lights dimmed to {value}%.");

        // Climate templates
        RESPONSE_TEMPLATES.put("set_temperature", "[CLIMATE] Temperature set to {temperature} degrees.");
        RESPONSE_TEMPLATES.put("increase_temperature", "[CLIMATE] Heating up! Temperature increased.");
        RESPONSE_TEMPLATES.put("decrease_temperature", "[CLIMATE] Cooling down! Temperature decreased.");
        RESPONSE_TEMPLATES.put("eco_mode", "[CLIMATE] Eco mode activated. Saving energy!");

        // Scenario templates
        RESPONSE_TEMPLATES.put("morning_routine", "[ROUTINE] Good morning! Your morning routine has been activated.");
        RESPONSE_TEMPLATES.put("night_routine", "[ROUTINE] Good night! Sleep mode activated.");
        RESPONSE_TEMPLATES.put("leaving_home", "[ROUTINE] Have a safe trip! Your home is secured.");
        RESPONSE_TEMPLATES.put("arriving_home", "[ROUTINE] Welcome home! Making everything comfortable for you.");
        RESPONSE_TEMPLATES.put("emergency", "[EMERGENCY] EMERGENCY PROTOCOL ACTIVATED! All safety measures engaged.");

        // Query templates
        RESPONSE_TEMPLATES.put("check_status", "[STATUS] Here's your system status:");
        RESPONSE_TEMPLATES.put("unknown", "[INFO] I'm not sure how to handle that request.");
    }

    /**
     * Build a status report
     */
    public Response buildStatusReport(Map<String, Object> systemStatus) {
        Response response = new Response();
        response.setSuccess(true);
        response.setTimestamp(new Date());

        StringBuilder report = new StringBuilder();
        report.append("[STATUS REPORT] System Status\n");
        report.append("================================\n\n");

        systemStatus.forEach((key, value) -> {
            String formattedKey = key.replace("_", " ")
                    .substring(0, 1).toUpperCase() + key.substring(1);
            report.append(String.format("- %s: %s\n", formattedKey, value));
        });

        report.append("\n[OK] All systems operational");

        response.setMessage(report.toString());
        response.setDetails(systemStatus);

        return response;
    }

    /**
     * Build a confirmation request
     */
    public Response buildConfirmationRequest(String action, Map<String, Object> params) {
        Response response = new Response();
        response.setSuccess(true);
        response.setRequiresConfirmation(true);
        response.setType(Response.ResponseType.CONFIRMATION);
        response.setTimestamp(new Date());

        StringBuilder message = new StringBuilder();
        message.append("[CONFIRMATION REQUIRED]\n\n");
        message.append("You asked me to: ").append(action.replace("_", " ")).append("\n");
        
        if (params != null && !params.isEmpty()) {
            message.append("With parameters:\n");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                message.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        message.append("\nPlease confirm:");
        message.append("\n1. Yes - Execute this command");
        message.append("\n2. No - Cancel this command");
        message.append("\n3. Edit - Modify the command (not yet implemented)");
        
        response.setMessage(message.toString());
        
        return response;
    }

    /**
     * Build a confirmation request for a compound command (multiple sub-commands)
     */
    public Response buildCompoundConfirmationRequest(List<String> commands, String originalText) {
        Response response = new Response();
        response.setSuccess(true);
        response.setRequiresConfirmation(true);
        response.setType(Response.ResponseType.CONFIRMATION);
        response.setTimestamp(new Date());

        StringBuilder message = new StringBuilder();
        message.append("[CONFIRMATION REQUIRED]\n\n");
        message.append("You asked me to: ").append(originalText).append("\n\n");
        message.append("This will execute ").append(commands.size()).append(" commands.\n\n");
        
        message.append("Please confirm:");
        message.append("\n1. Yes - Execute all commands");
        message.append("\n2. No - Cancel all commands");
        message.append("\n3. Edit - Modify the commands (not yet implemented)");
        
        response.setMessage(message.toString());
        
        return response;
    }

    /**
     * Build an unknown command response
     */
    public Response buildUnknownCommandResponse(String originalCommand) {
        Response response = new Response();
        response.setSuccess(false);
        response.setTimestamp(new Date());
        
        StringBuilder message = new StringBuilder();
        message.append("[COMMAND NOT RECOGNIZED]\n\n");
        message.append("I'm sorry, I didn't understand: \"").append(originalCommand).append("\"\n\n");
        message.append("Here's what I can help you with:\n");
        message.append("\n[SECURITY]\n");
        message.append("  • Lock/unlock doors\n");
        message.append("  • Arm/disarm security system\n");
        message.append("  • Show camera feeds\n");
        message.append("\n[APPLIANCES]\n");
        message.append("  • Turn on/off lights\n");
        message.append("  • Control TV (play music, etc.)\n");
        message.append("  • Brew coffee\n");
        message.append("  • Control washing machine, dishwasher\n");
        message.append("\n[CLIMATE]\n");
        message.append("  • Set temperature\n");
        message.append("  • Turn on/off air conditioner\n");
        message.append("  • Control fans\n");
        message.append("\n[ROUTINES]\n");
        message.append("  • Morning routine, Night routine\n");
        message.append("  • I'm leaving home, I'm arriving home\n");
        message.append("\nPlease try rephrasing your command.");
        
        response.setMessage(message.toString());
        
        return response;
    }
}
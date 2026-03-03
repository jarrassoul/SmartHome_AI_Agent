package org.SmartAI_Agent.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.SmartAI_Agent.agents.ApplianceAgent;
import org.SmartAI_Agent.agents.ClimateAgent;
import org.SmartAI_Agent.agents.SecurityAgent;
import org.SmartAI_Agent.models.Command;
import org.SmartAI_Agent.models.Intent;
import org.SmartAI_Agent.models.Response;

/**
 * MainOrchestrator - Central coordinator for the smart home system
 * Routes user requests to appropriate agents and manages overall system flow
 */
public class MainOrchestrator {
    
    private final SecurityAgent securityAgent;
    private final ApplianceAgent applianceAgent;
    private final ClimateAgent climateAgent;
    private final IntentAnalyzer intentAnalyzer;
    private final ResponseBuilder responseBuilder;
    private final SystemContext systemContext;
    
    public MainOrchestrator(SecurityAgent securityAgent, 
                           ApplianceAgent applianceAgent, 
                           ClimateAgent climateAgent,
                           IntentAnalyzer intentAnalyzer, 
                           ResponseBuilder responseBuilder,
                           SystemContext systemContext) {
        this.securityAgent = securityAgent;
        this.applianceAgent = applianceAgent;
        this.climateAgent = climateAgent;
        this.intentAnalyzer = intentAnalyzer;
        this.responseBuilder = responseBuilder;
        this.systemContext = systemContext;
    }

    /**
     * Process user request and route to appropriate agent
     */
    public Response processUserRequest(String userInput, String userId) {
        System.out.println("Processing request from user " + userId + ": " + userInput);
        
        try {
            // Update context for the user
            SystemContext.UserContext userContext = systemContext.getUserContext(userId);
            userContext.addRecentRequest(userInput);
            System.out.println("Updating context for user: " + userId);
            
            // Analyze the intent
            System.out.println("Starting intent analysis...");
            Intent intent = intentAnalyzer.analyze(userInput);
            System.out.println("Intent analysis completed");
            
            // Check if this is a compound command
            if ("compound_command".equals(intent.getAction())) {
                System.out.println("Processing compound command");
                return processCompoundCommand(intent, userId);
            }
            
            // Route to appropriate agent based on intent
            System.out.println("Detected intent: " + intent.getAction() + " with confidence: " + intent.getConfidence());
            System.out.println("Routing to domain: " + intent.getDomain() + ", action: " + intent.getAction());
            
            // Handle unknown intent
            if ("unknown".equals(intent.getAction())) {
                return responseBuilder.buildUnknownCommandResponse(userInput);
            }
            
            // Create command from intent
            Command command = new Command();
            command.setAction(intent.getAction());
            command.setParameters(intent.getParameters());
            command.addContext("userId", userId);
            command.setTimestamp(new Date());
            
            // Check if confirmation is needed
            if (intent.needsConfirmation() || intent.hasLowConfidence()) {
                // Store the pending command in context for later execution
                userContext.setPendingCommand(command);
                userContext.setPendingIntent(intent);
                
                // Build and return confirmation request
                return responseBuilder.buildConfirmationRequest(intent.getAction(), intent.getParameters());
            }
            
            // Route based on domain and action
            String domain = intent.getDomain();
            Response response;
            
            switch (domain.toLowerCase()) {
                case "security":
                    response = securityAgent.processCommand(command);
                    break;
                    
                case "appliance":
                    response = applianceAgent.processCommand(command);
                    break;
                    
                case "climate":
                    response = climateAgent.processCommand(command);
                    break;
                    
                case "all":
                    // Handle cross-domain commands
                    response = handleCrossDomainCommand(command, intent);
                    break;
                    
                default:
                    // Try to route based on action if domain is unclear
                    response = routeBasedOnAction(command, intent.getAction());
                    break;
            }
            
            // Update context with response
            userContext.addRecentResponse(response.getMessage());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("Error processing user request: " + e.getMessage());
            System.err.println("Error details: " + e.toString());
            return responseBuilder.buildErrorResponse("Sorry, I encountered an error: " + e.getMessage());
        }
    }
    
    /**
     * Process a confirmed command
     */
    public Response processConfirmedCommand(String userId) {
        SystemContext.UserContext userContext = systemContext.getUserContext(userId);
        
        // Retrieve the pending command and intent
        Command command = userContext.getPendingCommand();
        Intent intent = userContext.getPendingIntent();
        
        if (command == null || intent == null) {
            return responseBuilder.buildErrorResponse("No pending command found for confirmation");
        }
        
        // Check if this is a compound sequence command
        if ("execute_compound_sequence".equals(intent.getAction())) {
            return executeCompoundSequence(userId);
        }
        
        // Check if there are remaining commands after this one (BEFORE clearing)
        boolean hasRemainingCommands = userContext.hasRemainingCompoundCommands();
        
        // Clear only the pending command/intent (keep remaining commands queue)
        userContext.setPendingCommand(null);
        userContext.setPendingIntent(null);
        
        // Route to appropriate agent based on intent
        String domain = intent.getDomain();
        Response response;
        
        switch (domain.toLowerCase()) {
            case "security":
                response = securityAgent.processCommand(command);
                break;
                
            case "appliance":
                response = applianceAgent.processCommand(command);
                break;
                
            case "climate":
                response = climateAgent.processCommand(command);
                break;
                
            case "all":
                response = handleCrossDomainCommand(command, intent);
                break;
                
            default:
                response = routeBasedOnAction(command, intent.getAction());
                break;
        }
        
        // Update context with response
        userContext.addRecentResponse(response.getMessage());
        
        // If there are remaining commands, process the next one
        if (hasRemainingCommands) {
            Response nextResponse = processNextCompoundCommand(userId);
            if (nextResponse != null) {
                String combinedMessage = response.getMessage() + "\n\n" + nextResponse.getMessage();
                response.setMessage(combinedMessage);
                response.setRequiresConfirmation(nextResponse.isRequiresConfirmation());
            }
        }
        
        return response;
    }
    
    /**
     * Process the next command in a compound command queue
     */
    private Response processNextCompoundCommand(String userId) {
        SystemContext.UserContext userContext = systemContext.getUserContext(userId);
        String nextCommandText = userContext.pollNextRemainingCommand();
        
        if (nextCommandText == null) {
            return null;
        }
        
        System.out.println("Processing next compound command: " + nextCommandText);
        
        try {
            Intent intent = intentAnalyzer.analyzeSingleCommand(nextCommandText);
            
            // Handle unknown action
            if ("unknown".equals(intent.getAction())) {
                return responseBuilder.buildUnknownCommandResponse(nextCommandText);
            }
            
            Command command = new Command();
            command.setAction(intent.getAction());
            command.setParameters(intent.getParameters());
            command.addContext("userId", userId);
            command.setTimestamp(new Date());
            
            if (intent.needsConfirmation() || intent.hasLowConfidence()) {
                userContext.setPendingCommand(command);
                userContext.setPendingIntent(intent);
                
                Response confirmationResponse = responseBuilder.buildConfirmationRequest(
                    intent.getAction(), intent.getParameters());
                confirmationResponse.setRequiresConfirmation(true);
                return confirmationResponse;
            } else {
                // Execute immediately and check for more commands
                Response response = routeToAgent(intent, command);
                
                // Recursively process next command if any
                if (userContext.hasRemainingCompoundCommands()) {
                    Response nextResponse = processNextCompoundCommand(userId);
                    if (nextResponse != null) {
                        String combinedMessage = response.getMessage() + "\n\n" + nextResponse.getMessage();
                        response.setMessage(combinedMessage);
                        response.setRequiresConfirmation(nextResponse.isRequiresConfirmation());
                    }
                }
                return response;
            }
        } catch (Exception e) {
            System.err.println("Error processing next compound command: " + e.getMessage());
            return responseBuilder.buildErrorResponse("Failed to process: " + e.getMessage());
        }
    }
    
    /**
     * Cancel a pending command
     */
    public Response cancelPendingCommand(String userId) {
        SystemContext.UserContext userContext = systemContext.getUserContext(userId);
        Intent intent = userContext.getPendingIntent();
        
        userContext.clearPendingCommand();
        
        String action = intent != null ? intent.getAction() : "command";
        return responseBuilder.buildResponse(
            true, 
            String.format("Cancelled %s as requested.", action)
        );
    }

    /**
     * Process compound command with multiple intents
     */
    private Response processCompoundCommand(Intent compoundIntent, String userId) {
        Map<String, Object> params = compoundIntent.getParameters();
        List<String> allCommands = new ArrayList<>();
        
        // Extract all sub-commands from the compound intent
        int commandCount = 1;
        String commandKey = "command" + commandCount;
        
        while (params.containsKey(commandKey)) {
            allCommands.add((String) params.get(commandKey));
            commandCount++;
            commandKey = "command" + commandCount;
        }
        
        System.out.println("Processing compound command with " + allCommands.size() + " sub-commands");
        
        if (allCommands.isEmpty()) {
            return responseBuilder.buildErrorResponse("No commands found in compound request");
        }
        
        // Get user context
        SystemContext.UserContext userContext = systemContext.getUserContext(userId);
        
        // Store all commands for execution after confirmation
        userContext.setRemainingCompoundCommands(allCommands);
        
        // Create a special compound command that holds all sub-commands
        Command compoundCommand = new Command();
        compoundCommand.setAction("execute_compound_sequence");
        compoundCommand.setTimestamp(new Date());
        
        // Store the compound command for execution after confirmation
        userContext.setPendingCommand(compoundCommand);
        
        // Create a special intent for the compound command
        Intent pendingIntent = new Intent();
        pendingIntent.setAction("execute_compound_sequence");
        pendingIntent.setDomain("all");
        pendingIntent.setConfidence(0.95);
        pendingIntent.setOriginalText(compoundIntent.getOriginalText());
        userContext.setPendingIntent(pendingIntent);
        
        // Build ONE confirmation request for the entire compound command
        return responseBuilder.buildCompoundConfirmationRequest(allCommands, compoundIntent.getOriginalText());
    }
    
    /**
     * Route intent to appropriate agent
     */
    private Response routeToAgent(Intent intent, Command command) {
        String domain = intent.getDomain();
        
        switch (domain.toLowerCase()) {
            case "security":
                return securityAgent.processCommand(command);
            case "appliance":
                return applianceAgent.processCommand(command);
            case "climate":
                return climateAgent.processCommand(command);
            case "all":
                return handleCrossDomainCommand(command, intent);
            default:
                return routeBasedOnAction(command, intent.getAction());
        }
    }
    
    /**
     * Build a compound response from multiple responses
     */
    private Response buildCompoundResponse(List<Response> responses) {
        Response response = new Response();
        response.setTimestamp(new Date());
        
        // Check if any responses require confirmation
        boolean hasConfirmationRequest = responses.stream()
                .anyMatch(r -> r.isRequiresConfirmation() || 
                              (r.getMessage() != null && r.getMessage().contains("[CONFIRMATION REQUIRED]")));
        
        // Check if all operations succeeded (excluding confirmation requests)
        boolean allSuccess = !hasConfirmationRequest && 
                            responses.stream().allMatch(Response::isSuccess);
        long successCount = responses.stream().filter(Response::isSuccess).count();
        
        response.setSuccess(allSuccess);
        
        // Build detailed message
        StringBuilder details = new StringBuilder();
        
        if (hasConfirmationRequest) {
            // Don't say "executed successfully" when we're just asking for confirmation
            details.append("Processing multiple commands:\n\n");
        } else {
            details.append("Executing multiple commands:\n\n");
        }
        
        for (int i = 0; i < responses.size(); i++) {
            Response r = responses.get(i);
            details.append(String.format("%d. %s\n", i + 1, r.getMessage()));
        }
        
        if (hasConfirmationRequest) {
            // Remove any false success messages
            response.setRequiresConfirmation(true);
        } else if (allSuccess) {
            details.append("\n[SUCCESS] All commands executed successfully!");
        } else {
            details.append(String.format("\n[WARNING] %d of %d commands completed.", 
                                        successCount, responses.size()));
        }
        
        response.setMessage(details.toString());
        
        System.out.println("Built compound response: " + successCount + "/" + responses.size() + " successful");
        
        return response;
    }
    
    /**
     * Handle cross-domain commands
     */
    private Response handleCrossDomainCommand(Command command, Intent intent) {
        String action = intent.getAction();
        
        // Handle scenario-based commands
        switch (action.toLowerCase()) {
            case "morning_routine":
                return executeMorningRoutine();
                
            case "night_routine":
                return executeNightRoutine();
                
            case "leaving_home":
                return executeLeavingHomeRoutine();
                
            case "arriving_home":
                return executeArrivingHomeRoutine();
                
            case "emergency":
                return executeEmergencyProtocol();
                
            default:
                return responseBuilder.buildErrorResponse("Unknown cross-domain command: " + action);
        }
    }
    
    /**
     * Route command based on action when domain is unclear
     */
    private Response routeBasedOnAction(Command command, String action) {
        String actionLower = action.toLowerCase();
        
        // Security-related actions
        if (actionLower.contains("lock") || actionLower.contains("unlock") || 
            actionLower.contains("arm") || actionLower.contains("disarm") ||
            actionLower.contains("camera") || actionLower.contains("alarm")) {
            return securityAgent.processCommand(command);
        }
        
        // Appliance-related actions
        else if (actionLower.contains("light") || actionLower.contains("tv") || 
                 actionLower.contains("coffee") || actionLower.contains("brew") ||
                 actionLower.contains("washer") || actionLower.contains("dryer") ||
                 actionLower.contains("dishwasher") || actionLower.contains("oven") ||
                 actionLower.contains("microwave") || actionLower.contains("fridge")) {
            return applianceAgent.processCommand(command);
        }
        
        // Climate-related actions
        else if (actionLower.contains("temperature") || actionLower.contains("heat") || 
                 actionLower.contains("cool") || actionLower.contains("ac") ||
                 actionLower.contains("air") || actionLower.contains("hvac") ||
                 actionLower.contains("thermostat") || actionLower.contains("fan")) {
            return climateAgent.processCommand(command);
        }
        
        // If still unclear, return error
        else {
            return responseBuilder.buildErrorResponse(
                "I'm not sure which system you're referring to. I can help with:\n" +
                "- Security (locks, cameras, alarms)\n" +
                "- Appliances (lights, coffee maker, washing machine)\n" +
                "- Climate (temperature, HVAC)\n" +
                "Could you please be more specific about what you'd like to control?"
            );
        }
    }
    
    /**
     * Execute morning routine
     */
    private Response executeMorningRoutine() {
        List<Response> responses = new ArrayList<>();
        
        // Security: Disarm system
        Command disarmCommand = new Command();
        disarmCommand.setAction("disarm_security");
        responses.add(securityAgent.processCommand(disarmCommand));
        
        // Appliances: Turn on lights
        Command lightsCommand = new Command();
        lightsCommand.setAction("turn_on_lights");
        responses.add(applianceAgent.processCommand(lightsCommand));
        
        // Climate: Set comfortable temperature
        Command tempCommand = new Command();
        tempCommand.setAction("set_temperature");
        Map<String, Object> tempParams = new HashMap<>();
        tempParams.put("temperature", 72); // 22°C = 72°F
        tempParams.put("mode", "comfort");
        tempCommand.setParameters(tempParams);
        responses.add(climateAgent.processCommand(tempCommand));
        
        return buildScenarioResponse("Morning routine executed", responses, "Morning routine executed successfully");
    }
    
    /**
     * Execute night routine
     */
    private Response executeNightRoutine() {
        List<Response> responses = new ArrayList<>();
        
        // Security: Arm system
        Command armCommand = new Command();
        armCommand.setAction("arm_security");
        responses.add(securityAgent.processCommand(armCommand));
        
        // Appliances: Turn off lights
        Command lightsCommand = new Command();
        lightsCommand.setAction("turn_off_lights");
        responses.add(applianceAgent.processCommand(lightsCommand));
        
        // Climate: Set energy-saving temperature
        Command tempCommand = new Command();
        tempCommand.setAction("set_temperature");
        Map<String, Object> tempParams = new HashMap<>();
        tempParams.put("temperature", 65); // Energy saving temperature
        tempParams.put("mode", "eco");
        tempCommand.setParameters(tempParams);
        responses.add(climateAgent.processCommand(tempCommand));
        
        return buildScenarioResponse("Night routine executed", responses, "Night routine executed successfully");
    }
    
    /**
     * Execute leaving home routine
     */
    private Response executeLeavingHomeRoutine() {
        List<Response> responses = new ArrayList<>();
        
        // Security: Arm system in away mode
        Command armCommand = new Command();
        armCommand.setAction("arm_security");
        Map<String, Object> armParams = new HashMap<>();
        armParams.put("mode", "away");
        armCommand.setParameters(armParams);
        responses.add(securityAgent.processCommand(armCommand));
        
        // Appliances: Turn off lights
        Command lightsCommand = new Command();
        lightsCommand.setAction("turn_off_lights");
        responses.add(applianceAgent.processCommand(lightsCommand));
        
        // Climate: Set energy-saving temperature
        Command tempCommand = new Command();
        tempCommand.setAction("set_temperature");
        Map<String, Object> tempParams = new HashMap<>();
        tempParams.put("temperature", 60); // Energy saving temperature when away
        tempParams.put("mode", "eco");
        tempCommand.setParameters(tempParams);
        responses.add(climateAgent.processCommand(tempCommand));
        
        return buildScenarioResponse("Leaving home routine executed", responses, "Leaving home routine executed successfully");
    }
    
    /**
     * Execute arriving home routine
     */
    private Response executeArrivingHomeRoutine() {
        List<Response> responses = new ArrayList<>();
        
        // Security: Disarm system
        Command disarmCommand = new Command();
        disarmCommand.setAction("disarm_security");
        responses.add(securityAgent.processCommand(disarmCommand));
        
        // Appliances: Turn on lights
        Command lightsCommand = new Command();
        lightsCommand.setAction("turn_on_lights");
        responses.add(applianceAgent.processCommand(lightsCommand));
        
        // Climate: Set comfortable temperature
        Command tempCommand = new Command();
        tempCommand.setAction("set_temperature");
        Map<String, Object> tempParams = new HashMap<>();
        tempParams.put("temperature", 72); // Comfortable temperature
        tempParams.put("mode", "comfort");
        tempCommand.setParameters(tempParams);
        responses.add(climateAgent.processCommand(tempCommand));
        
        return buildScenarioResponse("Arriving home routine executed", responses, "Arriving home routine executed successfully");
    }
    
    /**
     * Execute emergency protocol
     */
    private Response executeEmergencyProtocol() {
        List<Response> responses = new ArrayList<>();
        
        // Security: Trigger alarm
        Command alarmCommand = new Command();
        alarmCommand.setAction("trigger_alarm");
        responses.add(securityAgent.processCommand(alarmCommand));
        
        // Climate: Emergency stop
        Command emergencyCommand = new Command();
        emergencyCommand.setAction("emergency_stop");
        responses.add(climateAgent.processCommand(emergencyCommand));
        
        return buildScenarioResponse("Emergency protocol executed", responses, "Emergency protocol executed successfully");
    }
    
    /**
     * Build a scenario response
     */
    private Response buildScenarioResponse(String scenario, List<Response> agentResponses, String message) {
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

        System.out.println("Scenario '" + scenario + "' response built: " + successCount + "/" + agentResponses.size() + " successful");

        return response;
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
        }

        return details.toString();
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
     * Enhanced intent analysis using both pattern matching and AI with better feedback
     */
    private Intent analyzeUserIntent(String userInput) {
        System.out.println("Starting intent analysis...");
        
        // First try pattern matching
        System.out.println("Performing quick pattern matching...");
        Intent intent = intentAnalyzer.analyze(userInput);
        
        // If confidence is low or action is unknown, use AI for better interpretation
        if (intent.getConfidence() < 0.7 || "unknown".equals(intent.getAction()) || "general".equals(intent.getDomain())) {
            System.out.println("Pattern matching confidence low or action unknown, using AI for complex request interpretation...");
            try {
                // Use AI for complex interpretation
                Intent aiIntent = getAIInterpretation(userInput);
                if (aiIntent != null && aiIntent.getConfidence() > intent.getConfidence()) {
                    System.out.println("AI interpretation used for better accuracy");
                    return aiIntent;
                }
            } catch (Exception e) {
                System.err.println("AI interpretation failed: " + e.getMessage());
                // Fall back to original intent
            }
        }
        
        System.out.println("Intent analysis completed");
        return intent;
    }
    
    /**
     * Get AI interpretation of user input
     */
    private Intent getAIInterpretation(String userInput) {
        try {
            // Use the intent analyzer's AI capabilities
            return intentAnalyzer.analyze(userInput);
        } catch (Exception e) {
            System.err.println("AI interpretation failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all devices managed by all agents
     */
    public List<String> getAllDevices() {
        List<String> allDevices = new ArrayList<>();
        // Note: The agents don't have a listManagedDevices method, so we'll need to implement this differently
        return allDevices;
    }
    
    /**
     * Find devices by name or ID
     */
    public List<String> findDevices(String searchTerm) {
        List<String> results = new ArrayList<>();
        // Note: The agents don't have a searchDevices method, so we'll need to implement this differently
        return results;
    }
    
    /**
     * Get devices by category
     */
    public List<String> getDevicesByCategory(String category) {
        // Note: The agents don't have a listManagedDevices method, so we'll need to implement this differently
        return new ArrayList<>();
    }
    
    /**
     * Get devices by type
     */
    public List<String> getDevicesByType(String type) {
        List<String> results = new ArrayList<>();
        // Note: The agents don't have a getDevicesByType method, so we'll need to implement this differently
        return results;
    }
    
    /**
     * Check which agent manages a specific device
     */
    public String getAgentForDevice(String deviceId) {
        if (securityAgent.managesDevice(deviceId)) {
            return "security";
        } else if (applianceAgent.managesDevice(deviceId)) {
            return "appliance";
        } else if (climateAgent.managesDevice(deviceId)) {
            return "climate";
        }
        return "unknown";
    }
    
    /**
     * Get system status
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("security_agent", securityAgent.getStatus());
        status.put("appliance_agent", applianceAgent.getStatus());
        status.put("climate_agent", climateAgent.getStatus());
        status.put("total_devices", getAllDevices().size());
        return status;
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        // Note: The agents don't have a getStatistics method, so we'll need to implement this differently
        stats.put("analyzer_stats", intentAnalyzer.getStatistics());
        return stats;
    }
    
    /**
     * Execute all commands in the compound sequence
     */
    private Response executeCompoundSequence(String userId) {
        SystemContext.UserContext userContext = systemContext.getUserContext(userId);
        List<String> allCommands = userContext.getRemainingCompoundCommands();
        
        // Clear pending command/intent
        userContext.setPendingCommand(null);
        userContext.setPendingIntent(null);
        
        if (allCommands == null || allCommands.isEmpty()) {
            return responseBuilder.buildErrorResponse("No commands to execute");
        }
        
        List<Response> responses = new ArrayList<>();
        List<String> successfulActions = new ArrayList<>();
        List<String> failedActions = new ArrayList<>();
        
        // Execute each command sequentially
        for (String commandText : allCommands) {
            try {
                Intent intent = intentAnalyzer.analyzeSingleCommand(commandText);
                Command command = new Command();
                command.setAction(intent.getAction());
                command.setParameters(intent.getParameters());
                command.addContext("userId", userId);
                command.setTimestamp(new Date());
                
                Response response = routeToAgent(intent, command);
                responses.add(response);
                
                // Extract simple action name for summary
                String actionName = getSimpleActionName(intent.getAction());
                if (response.isSuccess()) {
                    successfulActions.add(actionName);
                } else {
                    failedActions.add(actionName);
                }
                
            } catch (Exception e) {
                failedActions.add(commandText);
                responses.add(responseBuilder.buildErrorResponse("Failed: " + e.getMessage()));
            }
        }
        
        // Clear the command queue
        userContext.getRemainingCompoundCommands().clear();
        
        // Build natural language response
        String message = buildNaturalLanguageResponse(successfulActions, failedActions);
        
        // Build final response
        Response finalResponse = new Response();
        finalResponse.setSuccess(responses.stream().allMatch(Response::isSuccess));
        finalResponse.setTimestamp(new Date());
        finalResponse.setMessage(message);
        
        return finalResponse;
    }
    
    /**
     * Get simple action name for natural language response
     */
    private String getSimpleActionName(String action) {
        if (action == null) return null;
        
        switch (action.toLowerCase()) {
            case "turn_on_lights":
                return "lights";
            case "turn_off_lights":
                return "lights";
            case "turn_on_tv":
                return "TV";
            case "turn_off_tv":
                return "TV";
            case "control_tv":
                return "TV";
            case "turn_on_ac":
                return "AC";
            case "turn_off_ac":
                return "AC";
            case "control_ac":
                return "AC";
            case "set_temperature":
                return "temperature";
            case "brew_coffee":
                return "coffee maker";
            case "lock_doors":
                return "doors";
            case "unlock_doors":
                return "doors";
            default:
                // For unknown actions, return null to filter them out
                if (action.equals("unknown") || action.contains("unknown")) {
                    return null;
                }
                // Try to clean up the action name
                return action.replace("turn_on_", "").replace("turn_off_", "").replace("_", " ");
        }
    }
    
    /**
     * Build natural language response from successful and failed actions
     */
    private String buildNaturalLanguageResponse(List<String> successfulActions, List<String> failedActions) {
        // Remove nulls and duplicates
        List<String> uniqueSuccess = successfulActions.stream()
                .filter(a -> a != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        List<String> uniqueFailed = failedActions.stream()
                .filter(a -> a != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        StringBuilder message = new StringBuilder();
        
        if (!uniqueSuccess.isEmpty()) {
            // Check if we have temperature-related actions
            boolean hasTemp = uniqueSuccess.stream().anyMatch(a -> 
                a.equalsIgnoreCase("temperature") || a.equalsIgnoreCase("AC"));
            boolean hasOther = uniqueSuccess.stream().anyMatch(a -> 
                !a.equalsIgnoreCase("temperature") && !a.equalsIgnoreCase("AC"));
            
            if (hasTemp && !hasOther) {
                // Only temperature/AC actions
                message.append("I set the ");
                message.append(String.join(" and ", uniqueSuccess));
                message.append(" successfully.");
            } else {
                // Mixed or non-temperature actions
                message.append("I turned on the ");
                message.append(String.join(" and ", uniqueSuccess));
                message.append(" successfully.");
            }
        }
        
        if (!uniqueFailed.isEmpty()) {
            if (!uniqueSuccess.isEmpty()) {
                message.append(" However, ");
            }
            message.append("I couldn't process the ");
            message.append(String.join(" and ", uniqueFailed));
            message.append(" command.");
        }
        
        return message.toString();
    }
}
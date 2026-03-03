package org.SmartAI_Agent.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.SmartAI_Agent.LLM.AIProvider;
import org.SmartAI_Agent.LLM.DeepSeekService;
import org.SmartAI_Agent.LLM.GeminiService;
import org.SmartAI_Agent.LLM.OpenAIService;
import org.SmartAI_Agent.models.Intent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * IntentAnalyzer - Analyzes user input to determine intent
 * Uses multiple LLM providers in sequence, falling back to pattern matching
 */
public class IntentAnalyzer {
    
    private List<AIProvider> aiProviders;
    
    /**
     * Constructor - initializes all available LLM providers
     */
    public IntentAnalyzer() {
        this.aiProviders = new ArrayList<>();
        
        // Try Gemini first (often has better free tier)
        GeminiService gemini = new GeminiService();
        if (gemini.isAvailable()) {
            this.aiProviders.add(gemini);
            System.out.println("Gemini provider registered");
        }
        
        // Try OpenAI second
        OpenAIService openAI = new OpenAIService();
        if (openAI.isAvailable()) {
            this.aiProviders.add(openAI);
            System.out.println("OpenAI provider registered");
        }
        
        // Try DeepSeek third
        DeepSeekService deepSeek = new DeepSeekService();
        if (deepSeek.isAvailable()) {
            this.aiProviders.add(deepSeek);
            System.out.println("DeepSeek provider registered");
        }
        
        if (this.aiProviders.isEmpty()) {
            System.out.println("No LLM providers available. Using pattern matching only.");
        } else {
            System.out.println("Total LLM providers available: " + this.aiProviders.size());
        }
    }
    
    /**
     * Analyze user input using available LLMs, falling back to pattern matching
     */
    public Intent analyze(String userInput) {
        Intent intent = new Intent();
        intent.setOriginalText(userInput);
        intent.setTimestamp(new Date());
        
        // Try each LLM provider in sequence
        for (int i = 0; i < aiProviders.size(); i++) {
            AIProvider provider = aiProviders.get(i);
            try {
                System.out.println("Trying LLM provider: " + provider.getProviderName());
                String response = provider.analyze(userInput);
                
                if (response != null && !response.isEmpty()) {
                    Intent parsedIntent = parseLLMResponse(response, userInput);
                    if (parsedIntent != null && parsedIntent.getConfidence() > 0.5) {
                        System.out.println("Successfully parsed with " + provider.getProviderName());
                        return parsedIntent;
                    }
                }
            } catch (Exception e) {
                System.err.println(provider.getProviderName() + " failed: " + e.getMessage());
                // Continue to next provider
            }
        }
        
        // All LLMs failed, use pattern matching
        System.out.println("All LLM providers failed. Using pattern matching fallback.");
        return analyzeWithPatterns(userInput);
    }
    
    /**
     * Parse LLM JSON response and create Intent object
     */
    private Intent parseLLMResponse(String response, String originalText) {
        try {
            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            boolean isCompound = jsonResponse.has("is_compound") && 
                                 jsonResponse.get("is_compound").getAsBoolean();
            
            if (isCompound) {
                return createCompoundIntentFromLLM(jsonResponse, originalText);
            } else {
                return createSingleIntentFromLLM(jsonResponse, originalText);
            }
            
        } catch (Exception e) {
            System.err.println("LLM response parsing failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create a compound intent from LLM JSON response
     */
    private Intent createCompoundIntentFromLLM(JsonObject jsonResponse, String originalText) {
        Intent compoundIntent = new Intent();
        compoundIntent.setAction("compound_command");
        compoundIntent.setDomain("all");
        compoundIntent.setConfidence(0.95);
        compoundIntent.setOriginalText(originalText);
        
        Map<String, Object> params = new HashMap<>();
        JsonArray commands = jsonResponse.getAsJsonArray("commands");
        
        for (int i = 0; i < commands.size(); i++) {
            JsonObject cmdObj = commands.get(i).getAsJsonObject();
            String originalTextCmd = cmdObj.has("original_text") ? 
                                     cmdObj.get("original_text").getAsString() : 
                                     "command " + (i + 1);
            params.put("command" + (i + 1), originalTextCmd);
        }
        
        compoundIntent.setParameters(params);
        
        System.out.println("LLM detected compound command with " + commands.size() + " sub-commands");
        return compoundIntent;
    }

    /**
     * Create a single intent from LLM JSON response
     */
    private Intent createSingleIntentFromLLM(JsonObject jsonResponse, String originalText) {
        Intent intent = new Intent();
        intent.setOriginalText(originalText);
        intent.setTimestamp(new Date());
        
        if (jsonResponse.has("commands")) {
            JsonArray commands = jsonResponse.getAsJsonArray("commands");
            if (commands.size() > 0) {
                JsonObject cmdObj = commands.get(0).getAsJsonObject();
                
                String action = cmdObj.has("action") ? cmdObj.get("action").getAsString() : "unknown";
                String domain = cmdObj.has("domain") ? cmdObj.get("domain").getAsString() : "general";
                double confidence = cmdObj.has("confidence") ? cmdObj.get("confidence").getAsDouble() : 0.8;
                
                intent.setAction(action);
                intent.setDomain(domain);
                intent.setConfidence(confidence);
                
                // Parse parameters
                if (cmdObj.has("parameters") && !cmdObj.get("parameters").isJsonNull()) {
                    JsonObject paramsObj = cmdObj.getAsJsonObject("parameters");
                    Map<String, Object> params = new HashMap<>();
                    
                    for (Map.Entry<String, JsonElement> entry : paramsObj.entrySet()) {
                        JsonElement value = entry.getValue();
                        if (value.isJsonPrimitive()) {
                            if (value.getAsJsonPrimitive().isNumber()) {
                                params.put(entry.getKey(), value.getAsInt());
                            } else {
                                params.put(entry.getKey(), value.getAsString());
                            }
                        }
                    }
                    intent.setParameters(params);
                }
                
                System.out.println("LLM parsed: action=" + action + ", domain=" + domain + 
                                   ", confidence=" + confidence + ", params=" + intent.getParameters());
            }
        }
        
        return intent;
    }

    /**
     * Fallback pattern-based analysis
     */
    private Intent analyzeWithPatterns(String userInput) {
        String cleanedInput = userInput.replaceAll("[?!.]+$", "").trim();
        String lowerInput = cleanedInput.toLowerCase();

        Intent intent = new Intent();
        intent.setOriginalText(userInput);
        intent.setTimestamp(new Date());

        // Check for compound commands FIRST (before individual pattern matching)
        // This ensures "brew coffee and turn on lights" is handled as a compound command
        System.out.println("Pattern matching: checking for compound commands in: " + cleanedInput);
        if (lowerInput.contains(" and ") || lowerInput.contains(" then ")) {
            System.out.println("Pattern matching: found 'and' or 'then', checking if it's a climate command...");
            // But first check if it's actually a climate command with "and"
            // (e.g., "turn the AC on and put it on 20C" - single command)
            // Use word boundaries to avoid matching partial words like "machine" containing "he" or "kitchen" containing "air"
            boolean hasTempWords = lowerInput.contains("temperature") || lowerInput.contains("thermostat");
            boolean hasACWords = lowerInput.matches(".*\\bac\\b.*") || lowerInput.contains("air conditioner");
            boolean hasHeaterWords = lowerInput.contains("heater") || lowerInput.contains("heating");
            boolean hasCoolingWords = lowerInput.contains("cooling");
            boolean hasNumber = lowerInput.matches(".*\\d+.*");
            boolean hasActionWords = lowerInput.contains("put") || lowerInput.contains("set") ||
                                    lowerInput.contains("make") || lowerInput.contains("adjust");
            boolean hasClimateTarget = lowerInput.matches(".*\\bac\\b.*") || 
                                    lowerInput.contains("temp") || 
                                    lowerInput.matches(".*\\bheat\\b.*") ||
                                    lowerInput.contains("cooler") || 
                                    lowerInput.contains("cooling") ||
                                    lowerInput.matches(".*\\bcool\\b.*");
            
            boolean isClimateCommand = hasTempWords || hasACWords || hasHeaterWords || hasCoolingWords ||
                    (hasNumber && hasActionWords && hasClimateTarget);
            
            System.out.println("  hasTempWords=" + hasTempWords + ", hasACWords=" + hasACWords + 
                               ", hasHeaterWords=" + hasHeaterWords + ", hasCoolingWords=" + hasCoolingWords);
            System.out.println("  hasNumber=" + hasNumber + ", hasActionWords=" + hasActionWords + 
                               ", hasClimateTarget=" + hasClimateTarget);
            System.out.println("Pattern matching: isClimateCommand = " + isClimateCommand);
            
            if (!isClimateCommand) {
                System.out.println("Pattern matching: processing as compound command");
                return analyzeCompoundCommandFallback(cleanedInput);
            }
        }

        // Check for coffee/appliance commands
        if (lowerInput.contains("coffee") || lowerInput.contains("brew")) {
            intent.setAction("brew_coffee");
            intent.setDomain("appliance");
            intent.setConfidence(0.9);
            return intent;
        }

        // Check for climate/temperature commands
        if (lowerInput.contains("temperature") || lowerInput.contains("thermostat") ||
                lowerInput.contains("ac") || lowerInput.contains("air conditioner") ||
                lowerInput.contains("heater") || lowerInput.contains("heating") ||
                lowerInput.contains("cooling") ||
                // Match "put it on [number]" or "set it to [number]" when talking about AC/temp
                (lowerInput.matches(".*\\d+.*") &&
                        (lowerInput.contains("put") || lowerInput.contains("set") ||
                                lowerInput.contains("make") || lowerInput.contains("adjust")) &&
                        (lowerInput.contains("ac") || lowerInput.contains("air") ||
                                lowerInput.contains("temp") || lowerInput.contains("heat") ||
                                lowerInput.contains("cooler") || lowerInput.contains("cooling") ||
                                lowerInput.matches(".*\\bcool\\b.*")))) {
            // Handle AC/Heater temperature commands with C/F support
            intent.setAction("set_temperature");
            intent.setDomain("climate");
            intent.setConfidence(0.9);

            // Extract temperature with unit detection
            Map<String, Object> params = extractTemperatureWithUnit(userInput);
            if (!params.isEmpty()) {
                intent.setParameters(params);
            }
            return intent;
        }

        // Simple pattern matching for common commands
        if (lowerInput.contains("light") && lowerInput.contains("on")) {
            intent.setAction("turn_on_lights");
            intent.setDomain("appliance");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("light") && lowerInput.contains("off")) {
            intent.setAction("turn_off_lights");
            intent.setDomain("appliance");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("tv") || lowerInput.contains("television")) {
            // Handle TV commands
            if (lowerInput.contains("on")) {
                intent.setAction("turn_on_tv");
            } else if (lowerInput.contains("off")) {
                intent.setAction("turn_off_tv");
            } else {
                intent.setAction("control_tv");
            }
            intent.setDomain("appliance");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("lock")) {
            intent.setAction("lock_doors");
            intent.setDomain("security");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("unlock")) {
            intent.setAction("unlock_doors");
            intent.setDomain("security");
            intent.setConfidence(0.9);
        } else {
            intent.setAction("unknown");
            intent.setDomain("general");
            intent.setConfidence(0.3);
        }
        
        return intent;
    }

    /**
     * Fallback compound command analysis
     */
    private Intent analyzeCompoundCommandFallback(String userInput) {
        List<String> parts = new ArrayList<>();
        
        // Replace "Air conditioner" with "AC" temporarily to avoid splitting on " and "
        String protectedInput = userInput.replaceAll("(?i)air\\s+conditioner", "AC");
        
        // Split by "then" first (with word boundaries)
        String[] thenParts = protectedInput.split("\\s+then\\s+");
        for (String thenPart : thenParts) {
            // Then split by "and" (with word boundaries)
            String[] andParts = thenPart.split("\\s+and\\s+");
            for (String part : andParts) {
                String trimmed = part.trim();
                // Restore "AC" back to "air conditioner"
                trimmed = trimmed.replaceAll("(?i)\\bAC\\b", "air conditioner");
                if (!trimmed.isEmpty()) {
                    parts.add(trimmed);
                }
            }
        }
        
        Intent compoundIntent = new Intent();
        compoundIntent.setAction("compound_command");
        compoundIntent.setDomain("all");
        compoundIntent.setConfidence(0.8);
        compoundIntent.setOriginalText(userInput);
        
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < parts.size(); i++) {
            params.put("command" + (i + 1), parts.get(i));
        }
        compoundIntent.setParameters(params);
        
        return compoundIntent;
    }

    /**
     * Analyze a single command (no compound detection)
     */
    public Intent analyzeSingleCommand(String userInput) {
        // Try LLM first - use the first available provider (index 0)
        if (aiProviders != null && !aiProviders.isEmpty()) {
            try {
                String response = aiProviders.get(0).analyze(userInput);
                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                return createSingleIntentFromLLM(jsonResponse, userInput);
            } catch (Exception e) {
                System.err.println("LLM analysis failed for single command: " + e.getMessage());
            }
        }
        
        // Fallback - direct pattern matching WITHOUT compound detection
        return analyzeSingleWithPatterns(userInput);
    }
    
    /**
     * Analyze single command with patterns (no compound detection)
     */
    private Intent analyzeSingleWithPatterns(String userInput) {
        String cleanedInput = userInput.replaceAll("[?!.]+$", "").trim();
        String lowerInput = cleanedInput.toLowerCase();
        
        Intent intent = new Intent();
        intent.setOriginalText(userInput);
        intent.setTimestamp(new Date());
        
        // Simple pattern matching for common commands - NO compound detection
        if (lowerInput.contains("light") && lowerInput.contains("on")) {
            intent.setAction("turn_on_lights");
            intent.setDomain("appliance");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("light") && lowerInput.contains("off")) {
            intent.setAction("turn_off_lights");
            intent.setDomain("appliance");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("tv") || lowerInput.contains("television")) {
            // Handle TV commands
            if (lowerInput.contains("on")) {
                intent.setAction("turn_on_tv");
            } else if (lowerInput.contains("off")) {
                intent.setAction("turn_off_tv");
            } else {
                intent.setAction("control_tv");
            }
            intent.setDomain("appliance");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("temperature") || lowerInput.contains("thermostat") || 
                   (lowerInput.contains("air") && lowerInput.contains("conditioner"))) {
            // Handle AC/temperature commands
            intent.setAction("set_temperature");
            intent.setDomain("climate");
            intent.setConfidence(0.9);
            // Extract temperature if present
            java.util.regex.Pattern tempPattern = java.util.regex.Pattern.compile("(\\d+)");
            java.util.regex.Matcher matcher = tempPattern.matcher(userInput);
            if (matcher.find()) {
                Map<String, Object> params = new HashMap<>();
                params.put("temperature", Integer.parseInt(matcher.group(1)));
                intent.setParameters(params);
            }
        } else if (lowerInput.contains("lock")) {
            intent.setAction("lock_doors");
            intent.setDomain("security");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("unlock")) {
            intent.setAction("unlock_doors");
            intent.setDomain("security");
            intent.setConfidence(0.9);
        } else if (lowerInput.contains("coffee") || lowerInput.contains("brew")) {
            intent.setAction("brew_coffee");
            intent.setDomain("appliance");
            intent.setConfidence(0.9);
        } else {
            intent.setAction("unknown");
            intent.setDomain("general");
            intent.setConfidence(0.3);
        }
        
        return intent;
    }

    /**
     * Extract temperature value and unit from user input
     * Converts Celsius to Fahrenheit for internal consistency
     */
    private Map<String, Object> extractTemperatureWithUnit(String userInput) {
        Map<String, Object> params = new HashMap<>();
        String lowerInput = userInput.toLowerCase();
        
        // Pattern to match temperature: number followed by optional unit (C, C, Celsius, Fahrenheit)
        java.util.regex.Pattern tempPattern = java.util.regex.Pattern.compile("(\\d+)\\s*(c|celsius|f|fahrenheit)?");
        java.util.regex.Matcher matcher = tempPattern.matcher(lowerInput);
        
        if (matcher.find()) {
            int temperature = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            // Detect unit if not explicitly stated
            if (unit == null) {
                // Check for context clues
                if (lowerInput.contains("celsius") || lowerInput.contains("c ")) {
                    unit = "c";
                } else if (lowerInput.contains("fahrenheit") || lowerInput.contains("f ")) {
                    unit = "f";
                } else {
                    // Default to Fahrenheit if no unit specified (US standard)
                    unit = "f";
                }
            }
            
            // Convert to Fahrenheit for internal consistency
            int tempFahrenheit;
            if (unit.startsWith("c")) {
                // Convert Celsius to Fahrenheit
                tempFahrenheit = (int) Math.round(temperature * 9.0 / 5.0 + 32);
                params.put("original_unit", "C");
                params.put("original_value", temperature);
            } else {
                // Already Fahrenheit
                tempFahrenheit = temperature;
                params.put("original_unit", "F");
                params.put("original_value", temperature);
            }
            
            params.put("temperature", tempFahrenheit);
            params.put("display_temperature", temperature);
            params.put("display_unit", unit.startsWith("c") ? "C" : "F");
        }
        
        return params;
    }

    /**
     * Check if analyzer is ready
     */
    public boolean isReady() {
        return true;
    }

    /**
     * Get analyzer statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("ai_available", aiProviders != null && !aiProviders.isEmpty());
        return stats;
    }
}

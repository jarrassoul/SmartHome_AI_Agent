package org.SmartAI_Agent.LLM;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * OpenAIService - Implementation for OpenAI API integration
 * Provides access to OpenAI's GPT models for natural language processing
 */
public class OpenAIService extends AIProvider {
    
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";
    
    private static final String SYSTEM_PROMPT = """
        You are a smart home assistant that analyzes user commands and extracts structured information.
        
        Analyze the user's command and respond in this EXACT JSON format:
        {
          "is_compound": true/false,
          "commands": [
            {
              "original_text": "the exact text of this sub-command",
              "action": "one of: turn_on_lights, turn_off_lights, lock_doors, unlock_doors, arm_security, disarm_security, set_temperature, increase_temperature, decrease_temperature, turn_on_ac, turn_off_ac, brew_coffee, dim_lights, control_appliance, show_camera, play_music, emergency, morning_routine, night_routine, leaving_home, arriving_home, unknown",
              "domain": "one of: security, appliance, climate, all",
              "parameters": {
                "appliance": "tv/light/lights/coffee_maker/washing_machine/dishwasher/air_conditioner (if applicable)",
                "room": "living_room/bedroom/kitchen/bathroom (if specified)",
                "temperature": number (if specified, always in Fahrenheit),
                "artist": "artist name (if music request)",
                "song": "song name (if specified)",
                "genre": "relaxed/ambient/background (if specified)",
                "action": "turn_on/turn_off/play_music (for control_appliance)",
                "brightness": number (for dim_lights)
              },
              "confidence": 0.0-1.0
            }
          ]
        }
        
        DOMAINS:
        - security: locks, cameras, alarms, security system
        - appliance: lights, TV, coffee maker, washing machine, dishwasher, microwave, oven
        - climate: temperature, AC, HVAC, thermostat, fans
        - all: scenarios that affect multiple domains (morning routine, night routine, emergency, etc.)
        
        COMPOUND COMMANDS:
        - A compound command has multiple actions separated by "and", "then", or commas
        - Split them into separate commands
        - Example: "turn on the lights and play music on the tv" -> 2 commands
        
        EXAMPLES:
        
        User: "turn on the lights"
        Response: {"is_compound": false, "commands": [{"original_text": "turn on the lights", "action": "turn_on_lights", "domain": "appliance", "parameters": {}, "confidence": 0.95}]}
        
        User: "can you turn the light on and play music on the tv"
        Response: {"is_compound": true, "commands": [{"original_text": "turn the light on", "action": "turn_on_lights", "domain": "appliance", "parameters": {}, "confidence": 0.95}, {"original_text": "play music on the tv", "action": "control_appliance", "domain": "appliance", "parameters": {"appliance": "tv", "action": "play_music"}, "confidence": 0.95}]}
        
        User: "set temperature to 72 degrees"
        Response: {"is_compound": false, "commands": [{"original_text": "set temperature to 72 degrees", "action": "set_temperature", "domain": "climate", "parameters": {"temperature": 72}, "confidence": 0.95}]}
        
        User: "lock all doors and arm the security system"
        Response: {"is_compound": true, "commands": [{"original_text": "lock all doors", "action": "lock_doors", "domain": "security", "parameters": {}, "confidence": 0.95}, {"original_text": "arm the security system", "action": "arm_security", "domain": "security", "parameters": {}, "confidence": 0.95}]}
        
        User: "play some Adele music on the tv"
        Response: {"is_compound": false, "commands": [{"original_text": "play some Adele music on the tv", "action": "control_appliance", "domain": "appliance", "parameters": {"appliance": "tv", "action": "play_music", "artist": "Adele"}, "confidence": 0.95}]}
        
        User: "switch off the lights"
        Response: {"is_compound": false, "commands": [{"original_text": "switch off the lights", "action": "turn_off_lights", "domain": "appliance", "parameters": {}, "confidence": 0.95}]}
        
        User: "shutdown the TV"
        Response: {"is_compound": false, "commands": [{"original_text": "shutdown the TV", "action": "control_appliance", "domain": "appliance", "parameters": {"appliance": "tv", "action": "turn_off"}, "confidence": 0.95}]}
        
        IMPORTANT:
        - Always respond with valid JSON only
        - Understand synonyms: "switch off" = "turn off", "shutdown" = "turn off", "kill" = "turn off"
        - Understand context: "it's too hot" = decrease temperature, "make it brighter" = increase brightness
        """;
    
    private String apiKey;
    private final Gson gson = new Gson();
    
    /**
     * Constructor - loads API key from .env file or environment variable
     */
    public OpenAIService() {
        super("OpenAI");
        
        // Try to load from .env file first
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            this.apiKey = dotenv.get("OPENAI_API_KEY");
            System.out.println("Loaded API key from .env file");
        } catch (Exception e) {
            System.out.println("Could not load .env file: " + e.getMessage());
            this.apiKey = null;
        }
        
        // Fallback to environment variable if .env not found
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = System.getenv("OPENAI_API_KEY");
            System.out.println("Loaded API key from environment variable");
        }
        
        setAvailable(this.apiKey != null && !this.apiKey.isEmpty());
        
        if (isAvailable()) {
            System.out.println("OpenAI service initialized successfully");
        } else {
            System.out.println("OpenAI service not available (no API key found). Using fallback pattern matching.");
        }
    }
    
    /**
     * Analyze text using OpenAI API
     */
    @Override
    public String analyze(String prompt) {
        if (!isAvailable()) {
            throw new RuntimeException("OpenAI service not available");
        }
        
        try {
            return callOpenAIAPI(prompt);
        } catch (Exception e) {
            System.err.println("OpenAI API call failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Analyze and return structured JSON response
     */
    public String analyzeStructured(String prompt) {
        return analyze(prompt);
    }
    
    /**
     * Call OpenAI API with retry logic for rate limiting
     */
    private String callOpenAIAPI(String prompt) throws Exception {
        int maxRetries = 3;
        int retryDelayMs = 1000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return makeAPICall(prompt);
            } catch (Exception e) {
                if (e.getMessage().contains("429") && attempt < maxRetries) {
                    System.out.println("Rate limited by OpenAI. Retrying in " + retryDelayMs + "ms... (attempt " + attempt + "/" + maxRetries + ")");
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2;
                } else {
                    throw e;
                }
            }
        }
        
        throw new Exception("Max retries exceeded");
    }
    
    /**
     * Make actual API call
     */
    private String makeAPICall(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + this.apiKey);
        con.setDoOutput(true);
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        
        JsonArray messages = new JsonArray();
        
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", SYSTEM_PROMPT);
        messages.add(systemMessage);
        
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 500);
        
        String jsonInput = gson.toJson(requestBody);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        
        if (responseCode == 429) {
            throw new Exception("API call failed with response code: 429");
        }
        
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new Exception("API call failed with response code: " + responseCode + 
                                  ", error: " + errorResponse.toString());
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray choices = jsonResponse.getAsJsonArray("choices");
        if (choices != null && choices.size() > 0) {
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            return message.get("content").getAsString();
        }

        return null;
    }
    
    /**
     * Fallback rule-based analysis when API is not available
     */
    private String fallbackAnalysis(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        
        // Build a JSON-like response for consistency
        StringBuilder result = new StringBuilder();
        
        // Check for compound commands
        if (lowerPrompt.contains(" and ") || lowerPrompt.contains(" then ")) {
            List<String> parts = splitCompoundCommand(prompt);
            
            result.append("{\"is_compound\": true, \"commands\": [");
            for (int i = 0; i < parts.size(); i++) {
                if (i > 0) result.append(", ");
                result.append(analyzeSingleCommand(parts.get(i)));
            }
            result.append("]}");
        } else {
            result.append("{\"is_compound\": false, \"commands\": [");
            result.append(analyzeSingleCommand(prompt));
            result.append("]}");
        }
        
        return result.toString();
    }
    
    /**
     * Split compound command into parts
     */
    private List<String> splitCompoundCommand(String prompt) {
        List<String> parts = new ArrayList<>();
        String lowerPrompt = prompt.toLowerCase();
        
        // Split by "then" first
        String[] thenParts = prompt.split(" then ");
        for (String thenPart : thenParts) {
            // Then split by "and"
            String[] andParts = thenPart.split(" and ");
            for (String part : andParts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    parts.add(trimmed);
                }
            }
        }
        
        return parts;
    }
    
    /**
     * Analyze a single command (fallback)
     */
    private String analyzeSingleCommand(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        String action = "unknown";
        String domain = "general";
        StringBuilder params = new StringBuilder();
        double confidence = 0.8;
        
        // Light controls
        if (lowerPrompt.contains("light")) {
            action = "turn_on_lights";
            domain = "appliance";
            if (lowerPrompt.contains("off")) action = "turn_off_lights";
            if (lowerPrompt.contains("dim")) action = "dim_lights";
            confidence = 0.9;
        }
        // TV controls
        else if (lowerPrompt.contains("tv") || lowerPrompt.contains("television")) {
            action = "control_appliance";
            domain = "appliance";
            params.append("\"appliance\": \"tv\"");
            if (lowerPrompt.contains("music") || lowerPrompt.contains("play")) {
                params.append(", \"action\": \"play_music\"");
            } else if (lowerPrompt.contains("off")) {
                params.append(", \"action\": \"turn_off\"");
            } else {
                params.append(", \"action\": \"turn_on\"");
            }
            confidence = 0.9;
        }
        // Temperature/Climate
        else if (lowerPrompt.contains("temperature") || lowerPrompt.contains("ac") || 
                 lowerPrompt.contains("air conditioner") || lowerPrompt.contains("degrees")) {
            domain = "climate";
            Pattern tempPattern = Pattern.compile("(\\d+)");
            Matcher matcher = tempPattern.matcher(lowerPrompt);
            if (matcher.find()) {
                action = "set_temperature";
                params.append("\"temperature\": ").append(matcher.group(1));
            } else if (lowerPrompt.contains("increase") || lowerPrompt.contains("raise")) {
                action = "increase_temperature";
            } else if (lowerPrompt.contains("decrease") || lowerPrompt.contains("lower")) {
                action = "decrease_temperature";
            } else {
                action = "turn_on_ac";
            }
            confidence = 0.9;
        }
        // Security
        else if (lowerPrompt.contains("lock")) {
            action = "lock_doors";
            domain = "security";
            confidence = 0.9;
        }
        else if (lowerPrompt.contains("unlock")) {
            action = "unlock_doors";
            domain = "security";
            confidence = 0.9;
        }
        else if (lowerPrompt.contains("arm")) {
            action = "arm_security";
            domain = "security";
            confidence = 0.9;
        }
        else if (lowerPrompt.contains("disarm")) {
            action = "disarm_security";
            domain = "security";
            confidence = 0.9;
        }
        // Coffee
        else if (lowerPrompt.contains("coffee") || lowerPrompt.contains("brew")) {
            action = "brew_coffee";
            domain = "appliance";
            confidence = 0.9;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("{\"original_text\": \"").append(prompt.replace("\"", "\\\"")).append("\"");
        result.append(", \"action\": \"").append(action).append("\"");
        result.append(", \"domain\": \"").append(domain).append("\"");
        result.append(", \"parameters\": {").append(params).append("}");
        result.append(", \"confidence\": ").append(confidence);
        result.append("}");
        
        return result.toString();
    }
}
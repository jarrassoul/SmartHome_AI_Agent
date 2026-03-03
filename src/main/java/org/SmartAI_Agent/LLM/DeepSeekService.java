package org.SmartAI_Agent.LLM;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * DeepSeekService - Implementation for DeepSeek API integration
 * Provides access to DeepSeek's models for natural language processing
 */
public class DeepSeekService extends AIProvider {
    
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-chat";
    
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
                "brightness": number (for dim_lights)"
              },
              "confidence": 0.0-1.0
            }
          ]
        }
        
        DOMAINS:
        - security: locks, cameras, alarms, security system
        - appliance: lights, TV, coffee maker, washing machine, dishwasher, microwave, oven
        - climate: temperature, AC, HVAC, thermostat, fans, heater, heating
        - all: scenarios that affect multiple domains (morning routine, night routine, emergency, etc.)
        
        COMPOUND COMMANDS:
        - A compound command has multiple actions separated by "and", "then", or commas
        - Split them into separate commands
        - Example: "turn on the lights and play music on the tv" -> 2 commands
        
        TEMPERATURE:
        - Understand both Celsius and Fahrenheit
        - Convert Celsius to Fahrenheit in the temperature parameter
        - Example: "20C" -> temperature: 68, original_unit: "C", original_value: 20
        
        EXAMPLES:
        
        User: "turn on the lights"
        Response: {"is_compound": false, "commands": [{"original_text": "turn on the lights", "action": "turn_on_lights", "domain": "appliance", "parameters": {}, "confidence": 0.95}]}
        
        User: "turn the AC on and put it on 20C"
        Response: {"is_compound": false, "commands": [{"original_text": "turn the AC on and put it on 20C", "action": "set_temperature", "domain": "climate", "parameters": {"temperature": 68, "original_unit": "C", "original_value": 20}, "confidence": 0.95}]}
        
        User: "set temperature to 72 degrees"
        Response: {"is_compound": false, "commands": [{"original_text": "set temperature to 72 degrees", "action": "set_temperature", "domain": "climate", "parameters": {"temperature": 72, "original_unit": "F", "original_value": 72}, "confidence": 0.95}]}
        """;
    
    private String apiKey;
    private final Gson gson = new Gson();
    
    /**
     * Constructor - loads API key from .env file or environment variable
     */
    public DeepSeekService() {
        super("DeepSeek");
        
        // Try to load from .env file first
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            this.apiKey = dotenv.get("DEEPSEEK_API_KEY");
            if (this.apiKey != null && !this.apiKey.isEmpty()) {
                System.out.println("Loaded DeepSeek API key from .env file");
            }
        } catch (Exception e) {
            System.out.println("Could not load .env file: " + e.getMessage());
            this.apiKey = null;
        }
        
        // Fallback to environment variable if .env not found
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = System.getenv("DEEPSEEK_API_KEY");
            if (this.apiKey != null && !this.apiKey.isEmpty()) {
                System.out.println("Loaded DeepSeek API key from environment variable");
            }
        }
        
        setAvailable(this.apiKey != null && !this.apiKey.isEmpty());
        
        if (isAvailable()) {
            System.out.println("DeepSeek service initialized successfully");
        } else {
            System.out.println("DeepSeek service not available (no API key found)");
        }
    }
    
    @Override
    public String analyze(String prompt) {
        if (!isAvailable()) {
            throw new RuntimeException("DeepSeek service not available");
        }
        
        try {
            return callDeepSeekAPI(prompt);
        } catch (Exception e) {
            System.err.println("DeepSeek API call failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Call DeepSeek API with retry logic
     */
    private String callDeepSeekAPI(String prompt) throws Exception {
        int maxRetries = 3;
        int retryDelayMs = 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return makeAPICall(prompt);
            } catch (Exception e) {
                if (e.getMessage().contains("429") && attempt < maxRetries) {
                    System.out.println("Rate limited by DeepSeek. Retrying in " + retryDelayMs + "ms... (attempt " + attempt + "/" + maxRetries + ")");
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("Retry interrupted", ie);
                    }
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
}
package org.SmartAI_Agent.LLM;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * GeminiService - Implementation for Google Gemini API integration
 * Provides access to Google's Gemini models for natural language processing
 */
public class GeminiService extends AIProvider {
    
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String MODEL = "gemini-2.0-flash";
    
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
    public GeminiService() {
        super("Gemini");
        
        // Try to load from .env file first
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            this.apiKey = dotenv.get("GEMINI_API_KEY");
            System.out.println("Loaded Gemini API key from .env file");
        } catch (Exception e) {
            System.out.println("Could not load .env file for Gemini: " + e.getMessage());
            this.apiKey = null;
        }
        
        // Fallback to environment variable if .env not found
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = System.getenv("GEMINI_API_KEY");
            System.out.println("Loaded Gemini API key from environment variable");
        }
        
        setAvailable(this.apiKey != null && !this.apiKey.isEmpty());
        
        if (isAvailable()) {
            System.out.println("Gemini service initialized successfully");
        } else {
            System.out.println("Gemini service not available (no API key found)");
        }
    }
    
    /**
     * Analyze text using Gemini API
     */
    @Override
    public String analyze(String prompt) {
        if (!isAvailable()) {
            throw new RuntimeException("Gemini service not available");
        }
        
        try {
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            System.err.println("Gemini API call failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Call Gemini API with retry logic
     */
    private String callGeminiAPI(String prompt) throws Exception {
        int maxRetries = 3;
        int retryDelayMs = 1000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return makeAPICall(prompt);
            } catch (Exception e) {
                if ((e.getMessage().contains("429") || e.getMessage().contains("503")) && attempt < maxRetries) {
                    System.out.println("Rate limited by Gemini. Retrying in " + retryDelayMs + "ms... (attempt " + attempt + "/" + maxRetries + ")");
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
     * Create HTTP connection for Gemini API
     */
    private HttpURLConnection createConnection() throws Exception {
        String urlWithKey = API_URL + "?key=" + this.apiKey;
        URI uri = new URI(urlWithKey);
        URL url = uri.toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);
        return con;
    }
    
    /**
     * Build Gemini API request body
     */
    private JsonObject buildRequestBody(String prompt) {
        JsonObject requestBody = new JsonObject();
        
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        
        JsonArray parts = new JsonArray();
        
        // Add system instruction as first part
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", SYSTEM_PROMPT);
        parts.add(systemPart);
        
        // Add user prompt
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", "User: \"" + prompt + "\"");
        parts.add(userPart);
        
        content.add("parts", parts);
        content.addProperty("role", "user");
        contents.add(content);
        
        requestBody.add("contents", contents);
        
        // Add generation config
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.2);
        generationConfig.addProperty("maxOutputTokens", 1024);
        generationConfig.addProperty("topP", 0.8);
        generationConfig.addProperty("topK", 40);
        requestBody.add("generationConfig", generationConfig);
        
        return requestBody;
    }
    
    /**
     * Make actual API call to Gemini
     */
    private String makeAPICall(String prompt) throws Exception {
        HttpURLConnection con = createConnection();
        
        JsonObject requestBody = buildRequestBody(prompt);

        // Send request
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                // Parse Gemini response and extract the text
                return extractTextFromGeminiResponse(response.toString());
            }
        } else {
            // Read error response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
                throw new Exception("API call failed with response code: " + responseCode + ", error: " + errorResponse.toString());
            }
        }
    }
    
    /**
     * Extract text content from Gemini API response
     */
    private String extractTextFromGeminiResponse(String jsonResponse) throws Exception {
        try {
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            if (responseObj.has("candidates") && !responseObj.getAsJsonArray("candidates").isEmpty()) {
                JsonObject candidate = responseObj.getAsJsonArray("candidates").get(0).getAsJsonObject();
                
                if (candidate.has("content")) {
                    JsonObject content = candidate.getAsJsonObject("content");
                    
                    if (content.has("parts") && content.getAsJsonArray("parts").size() > 0) {
                        JsonObject part = content.getAsJsonArray("parts").get(0).getAsJsonObject();
                        
                        if (part.has("text")) {
                            String text = part.get("text").getAsString();
                            // Extract JSON from markdown code blocks if present
                            text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
                            return text;
                        }
                    }
                }
            }
            
            throw new Exception("Could not extract text from Gemini response");
        } catch (Exception e) {
            throw new Exception("Failed to parse Gemini response: " + e.getMessage());
        }
    }
}

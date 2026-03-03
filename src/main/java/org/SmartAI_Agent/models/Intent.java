package org.SmartAI_Agent.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Intent - Represents the analyzed user intention
 * Contains what the user wants to do, extracted from their natural language input
 */
public class Intent {

    private String id;
    private String action;                  // What to do (lock, unlock, turn_on, set_temperature)
    private String domain;                  // Which agent (security, appliance, climate, all)
    private Map<String, Object> parameters; // Additional parameters (temperature=22, room=bedroom)
    private double confidence;              // How confident we are (0.0 to 1.0)
    private String originalText;            // Original user input
    private Date timestamp;                 // When the intent was created
    private IntentType type;               // Type of intent
    private Priority priority;             // Priority level
    private boolean requiresConfirmation;  // Whether user confirmation is needed

    /**
     * Intent types
     */
    public enum IntentType {
        COMMAND,        // Direct command to execute
        QUERY,          // Information query
        SCENARIO,       // Pre-defined scenario (morning routine, etc.)
        AUTOMATION,     // Automation trigger
        EMERGENCY,      // Emergency action
        UNKNOWN         // Could not determine type
    }

    /**
     * Priority levels
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }

    /**
     * Default constructor
     */
    public Intent() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.parameters = new HashMap<>();
        this.confidence = 0.0;
        this.type = IntentType.COMMAND;
        this.priority = Priority.NORMAL;
        this.requiresConfirmation = false;
    }

    /**
     * Constructor with basic fields
     */
    public Intent(String action, String domain) {
        this();
        this.action = action;
        this.domain = domain;
    }

    /**
     * Constructor with all basic fields
     */
    public Intent(String action, String domain, Map<String, Object> parameters, double confidence) {
        this();
        this.action = action;
        this.domain = domain;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.confidence = confidence;
    }

    /**
     * Add a parameter to the intent
     */
    public void addParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(key, value);
    }

    /**
     * Get a parameter value
     */
    public Object getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }

    /**
     * Check if intent has high confidence
     */
    public boolean hasHighConfidence() {
        return confidence >= 0.8;
    }

    /**
     * Check if intent has medium confidence
     */
    public boolean hasMediumConfidence() {
        return confidence >= 0.5 && confidence < 0.8;
    }

    /**
     * Check if intent has low confidence
     */
    public boolean hasLowConfidence() {
        return confidence < 0.5;
    }

    /**
     * Check if this is an emergency intent
     */
    public boolean isEmergency() {
        return type == IntentType.EMERGENCY || priority == Priority.CRITICAL;
    }

    /**
     * Check if this is a query intent
     */
    public boolean isQuery() {
        return type == IntentType.QUERY;
    }

    /**
     * Check if this is a scenario intent
     */
    public boolean isScenario() {
        return type == IntentType.SCENARIO;
    }

    /**
     * Determine if confirmation is needed based on confidence and type
     * MODIFIED: Always require confirmation for ALL commands
     */
    public boolean needsConfirmation() {
        // ALWAYS require confirmation for every command
        // This ensures users must explicitly confirm all actions
        return true;
        
        /*
        // Original logic (commented out):
        // Always require confirmation for low confidence
        if (hasLowConfidence()) {
            return true;
        }

        // Emergency actions might need confirmation
        if (isEmergency() && requiresConfirmation) {
            return true;
        }

        // Security disarm might need confirmation
        if ("security".equals(domain) && action != null && action.contains("disarm")) {
            return true;
        }

        return requiresConfirmation;
        */
    }

    /**
     * Create a response message for low confidence
     */
    public String getLowConfidenceMessage() {
        return String.format(
                "[UNCERTAIN] I think you want to %s in the %s system. Is that correct?",
                action != null ? action.replace("_", " ") : "perform an action",
                domain != null ? domain : "smart home"
        );
    }

    /**
     * Convert intent to string representation
     */
    @Override
    public String toString() {
        return String.format(
                "Intent[action=%s, domain=%s, confidence=%.2f, type=%s, priority=%s]",
                action, domain, confidence, type, priority
        );
    }

    /**
     * Convert to map for serialization
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("action", action);
        map.put("domain", domain);
        map.put("parameters", parameters);
        map.put("confidence", confidence);
        map.put("type", type.toString());
        map.put("priority", priority.toString());
        map.put("timestamp", timestamp);
        map.put("original_text", originalText);
        return map;
    }

    /**
     * Create intent from user action
     */
    public static Intent fromAction(String action, String domain) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setDomain(domain);
        intent.setConfidence(1.0);
        return intent;
    }

    /**
     * Create an emergency intent
     */
    public static Intent emergency(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setDomain("all");
        intent.setType(IntentType.EMERGENCY);
        intent.setPriority(Priority.CRITICAL);
        intent.setConfidence(1.0);
        return intent;
    }

    /**
     * Create a query intent
     */
    public static Intent query(String query, String domain) {
        Intent intent = new Intent();
        intent.setAction(query);
        intent.setDomain(domain);
        intent.setType(IntentType.QUERY);
        intent.setPriority(Priority.NORMAL);
        intent.setConfidence(0.9);
        return intent;
    }

    // ============== Getters and Setters ==============

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Map<String, Object> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public IntentType getType() {
        return type;
    }

    public void setType(IntentType type) {
        this.type = type;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }
}
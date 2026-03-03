package org.SmartAI_Agent.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Request - Represents a user request to the smart home system
 * Contains the original user input and all associated metadata
 */
public class Request {

    private String id;                      // Unique request identifier
    private String userId;                  // User who made the request
    private String input;                   // Original user input/command
    private RequestSource source;           // Where request came from
    private RequestType type;               // Type of request
    private Map<String, Object> context;    // Additional context information
    private Map<String, String> metadata;   // Request metadata
    private Date timestamp;                 // When request was created
    private String sessionId;               // Session identifier
    private String priority;                // Request priority
    private String status;                  // Current status
    private Long timeout;                   // Timeout in milliseconds

    /**
     * Request source
     */
    public enum RequestSource {
        VOICE,          // Voice assistant (Alexa, Google)
        MOBILE_APP,     // Mobile application
        WEB,            // Web interface
        API,            // Direct API call
        AUTOMATION,     // Automated trigger
        SCHEDULED,      // Scheduled task
        SENSOR,         // Sensor triggered
        SYSTEM          // System generated
    }

    /**
     * Request type
     */
    public enum RequestType {
        COMMAND,        // Direct command
        QUERY,          // Information query
        SCENARIO,       // Scenario execution
        AUTOMATION,     // Automation rule
        EMERGENCY,      // Emergency request
        CONFIRMATION,   // Confirmation response
        FEEDBACK        // User feedback
    }

    /**
     * Request status
     */
    public static class Status {
        public static final String PENDING = "PENDING";
        public static final String PROCESSING = "PROCESSING";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
        public static final String CANCELLED = "CANCELLED";
        public static final String TIMEOUT = "TIMEOUT";
    }

    /**
     * Default constructor
     */
    public Request() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.context = new HashMap<>();
        this.metadata = new HashMap<>();
        this.source = RequestSource.WEB;
        this.type = RequestType.COMMAND;
        this.status = Status.PENDING;
        this.priority = "NORMAL";
        this.timeout = 10000L; // 10 seconds default
    }

    /**
     * Constructor with user input
     */
    public Request(String userId, String input) {
        this();
        this.userId = userId;
        this.input = input;
    }

    /**
     * Constructor with all basic fields
     */
    public Request(String id, String userId, String input) {
        this();
        this.id = id;
        this.userId = userId;
        this.input = input;
    }

    /**
     * Builder pattern constructor
     */
    public static RequestBuilder builder() {
        return new RequestBuilder();
    }

    /**
     * Add context information
     */
    public void addContext(String key, Object value) {
        if (context == null) {
            context = new HashMap<>();
        }
        context.put(key, value);
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Get context value
     */
    public Object getContextValue(String key) {
        return context != null ? context.get(key) : null;
    }

    /**
     * Get metadata value
     */
    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * Check if request is from voice source
     */
    public boolean isVoiceRequest() {
        return source == RequestSource.VOICE;
    }

    /**
     * Check if request is automated
     */
    public boolean isAutomated() {
        return source == RequestSource.AUTOMATION || source == RequestSource.SCHEDULED;
    }

    /**
     * Check if request is emergency
     */
    public boolean isEmergency() {
        return type == RequestType.EMERGENCY || "CRITICAL".equals(priority);
    }

    /**
     * Check if request has timed out
     */
    public boolean hasTimedOut() {
        if (timeout == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - timestamp.getTime();
        return elapsed > timeout;
    }

    /**
     * Update request status
     */
    public void updateStatus(String newStatus) {
        this.status = newStatus;
        addMetadata("status_updated", new Date().toString());
    }

    /**
     * Mark request as completed
     */
    public void markCompleted() {
        this.status = Status.COMPLETED;
        addMetadata("completed_at", new Date().toString());
    }

    /**
     * Mark request as failed
     */
    public void markFailed(String reason) {
        this.status = Status.FAILED;
        addMetadata("failed_at", new Date().toString());
        addMetadata("failure_reason", reason);
    }

    /**
     * Calculate request age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp.getTime();
    }

    /**
     * Convert to string representation
     */
    @Override
    public String toString() {
        return String.format(
                "Request[id=%s, userId=%s, input=%s, source=%s, type=%s, status=%s]",
                id, userId, input, source, type, status
        );
    }

    /**
     * Convert to map for serialization
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("user_id", userId);
        map.put("input", input);
        map.put("source", source.toString());
        map.put("type", type.toString());
        map.put("status", status);
        map.put("priority", priority);
        map.put("timestamp", timestamp);
        map.put("context", context);
        map.put("metadata", metadata);
        return map;
    }

    /**
     * Create a voice request
     */
    public static Request voiceRequest(String userId, String input) {
        Request request = new Request(userId, input);
        request.setSource(RequestSource.VOICE);
        return request;
    }

    /**
     * Create an emergency request
     */
    public static Request emergencyRequest(String userId, String input) {
        Request request = new Request(userId, input);
        request.setType(RequestType.EMERGENCY);
        request.setPriority("CRITICAL");
        request.setTimeout(5000L); // 5 second timeout for emergency
        return request;
    }

    /**
     * Create an automated request
     */
    public static Request automatedRequest(String systemId, String command) {
        Request request = new Request(systemId, command);
        request.setSource(RequestSource.AUTOMATION);
        request.setType(RequestType.AUTOMATION);
        return request;
    }

    // ============== Request Builder ==============

    public static class RequestBuilder {
        private Request request;

        public RequestBuilder() {
            request = new Request();
        }

        public RequestBuilder id(String id) {
            request.id = id;
            return this;
        }

        public RequestBuilder userId(String userId) {
            request.userId = userId;
            return this;
        }

        public RequestBuilder input(String input) {
            request.input = input;
            return this;
        }

        public RequestBuilder source(RequestSource source) {
            request.source = source;
            return this;
        }

        public RequestBuilder type(RequestType type) {
            request.type = type;
            return this;
        }

        public RequestBuilder priority(String priority) {
            request.priority = priority;
            return this;
        }

        public RequestBuilder context(Map<String, Object> context) {
            request.context = context;
            return this;
        }

        public RequestBuilder sessionId(String sessionId) {
            request.sessionId = sessionId;
            return this;
        }

        public RequestBuilder timeout(Long timeout) {
            request.timeout = timeout;
            return this;
        }

        public Request build() {
            return request;
        }
    }

    // ============== Getters and Setters ==============

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public RequestSource getSource() {
        return source;
    }

    public void setSource(RequestSource source) {
        this.source = source;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public Map<String, Object> getContext() {
        if (context == null) {
            context = new HashMap<>();
        }
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
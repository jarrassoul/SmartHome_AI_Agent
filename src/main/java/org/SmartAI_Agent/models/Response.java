package org.SmartAI_Agent.models;

import java.util.*;

/**
 * Response - Represents the response from agents or orchestrator
 * Contains the result of processing a user request
 */
public class Response {

    private String id;                      // Response identifier
    private String requestId;               // Original request ID
    private String agentId;                 // Agent that generated response
    private boolean success;                // Whether operation succeeded
    private String message;                 // Human-readable message
    private ResponseType type;              // Type of response
    private String priority;                // Response priority
    private Map<String, Object> data;       // Response data/payload
    private Map<String, Object> details;    // Additional details
    private List<String> affectedDevices;   // Devices that were affected
    private String error;                   // Error message if failed
    private String warning;                 // Warning message
    private Date timestamp;                 // When response was created
    private Long responseTime;              // Processing time in ms
    private String scenario;                // Scenario name if applicable
    private boolean requiresConfirmation;   // Needs user confirmation
    private List<Action> suggestedActions;  // Suggested follow-up actions

    /**
     * Response types
     */
    public enum ResponseType {
        SUCCESS,        // Operation completed successfully
        PARTIAL,        // Partially successful
        FAILED,         // Operation failed
        ERROR,          // Error occurred
        WARNING,        // Completed with warnings
        INFO,           // Informational response
        CONFIRMATION,   // Confirmation required
        QUERY_RESULT,   // Query response
        STATUS          // Status update
    }

    /**
     * Suggested action
     */
    public static class Action {
        private String action;
        private String description;
        private String command;

        public Action(String action, String description, String command) {
            this.action = action;
            this.description = description;
            this.command = command;
        }

        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
    }

    /**
     * Default constructor
     */
    public Response() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.success = false;
        this.type = ResponseType.INFO;
        this.priority = "NORMAL";
        this.data = new HashMap<>();
        this.details = new HashMap<>();
        this.affectedDevices = new ArrayList<>();
        this.suggestedActions = new ArrayList<>();
    }

    /**
     * Constructor with success and message
     */
    public Response(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
        this.type = success ? ResponseType.SUCCESS : ResponseType.FAILED;
    }

    /**
     * Constructor with all basic fields
     */
    public Response(String requestId, boolean success, String message, String agentId) {
        this();
        this.requestId = requestId;
        this.success = success;
        this.message = message;
        this.agentId = agentId;
        this.type = success ? ResponseType.SUCCESS : ResponseType.FAILED;
    }

    /**
     * Builder pattern
     */
    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    /**
     * Add data to response
     */
    public void addData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }

    /**
     * Add detail to response
     */
    public void addDetail(String key, Object value) {
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(key, value);
    }

    /**
     * Add affected device
     */
    public void addAffectedDevice(String deviceId) {
        if (affectedDevices == null) {
            affectedDevices = new ArrayList<>();
        }
        affectedDevices.add(deviceId);
    }

    /**
     * Add suggested action
     */
    public void addSuggestedAction(String action, String description, String command) {
        if (suggestedActions == null) {
            suggestedActions = new ArrayList<>();
        }
        suggestedActions.add(new Action(action, description, command));
    }

    /**
     * Check if response is successful
     */
    public boolean isSuccessful() {
        return success && (type == ResponseType.SUCCESS || type == ResponseType.INFO);
    }

    /**
     * Check if response has errors
     */
    public boolean hasError() {
        return !success || type == ResponseType.ERROR || type == ResponseType.FAILED;
    }

    /**
     * Check if response has warnings
     */
    public boolean hasWarning() {
        return warning != null || type == ResponseType.WARNING;
    }

    /**
     * Check if this is a partial success
     */
    public boolean isPartialSuccess() {
        return type == ResponseType.PARTIAL;
    }

    /**
     * Get data value by key
     */
    public Object getDataValue(String key) {
        return data != null ? data.get(key) : null;
    }

    /**
     * Get detail value by key
     */
    public Object getDetailValue(String key) {
        return details != null ? details.get(key) : null;
    }

    /**
     * Set response time based on start time
     */
    public void calculateResponseTime(long startTime) {
        this.responseTime = System.currentTimeMillis() - startTime;
    }

    /**
     * Create a success response
     */
    public static Response success(String message) {
        Response response = new Response();
        response.setSuccess(true);
        response.setMessage(message);
        response.setType(ResponseType.SUCCESS);
        return response;
    }

    /**
     * Create an error response
     */
    public static Response error(String message) {
        Response response = new Response();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(message);
        response.setType(ResponseType.ERROR);
        return response;
    }

    /**
     * Create a warning response
     */
    public static Response warning(String message) {
        Response response = new Response();
        response.setSuccess(true);
        response.setMessage(message);
        response.setWarning(message);
        response.setType(ResponseType.WARNING);
        return response;
    }

    /**
     * Create an info response
     */
    public static Response info(String message) {
        Response response = new Response();
        response.setSuccess(true);
        response.setMessage(message);
        response.setType(ResponseType.INFO);
        return response;
    }

    /**
     * Merge multiple responses
     */
    public static Response merge(List<Response> responses) {
        Response merged = new Response();

        if (responses == null || responses.isEmpty()) {
            return merged;
        }

        // Check overall success
        boolean allSuccess = responses.stream().allMatch(Response::isSuccess);
        long successCount = responses.stream().filter(Response::isSuccess).count();

        merged.setSuccess(allSuccess);

        // Determine type
        if (allSuccess) {
            merged.setType(ResponseType.SUCCESS);
        } else if (successCount > 0) {
            merged.setType(ResponseType.PARTIAL);
        } else {
            merged.setType(ResponseType.FAILED);
        }

        // Merge messages
        StringBuilder message = new StringBuilder();
        for (Response r : responses) {
            if (r.getMessage() != null) {
                message.append(r.getMessage()).append(". ");
            }
        }
        merged.setMessage(message.toString().trim());

        // Merge affected devices
        Set<String> devices = new HashSet<>();
        for (Response r : responses) {
            if (r.getAffectedDevices() != null) {
                devices.addAll(r.getAffectedDevices());
            }
        }
        merged.setAffectedDevices(new ArrayList<>(devices));

        return merged;
    }

    /**
     * Convert to string representation
     */
    @Override
    public String toString() {
        return String.format(
                "Response[success=%s, type=%s, message=%s, agentId=%s]",
                success, type, message, agentId
        );
    }

    /**
     * Convert to map for serialization
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("request_id", requestId);
        map.put("agent_id", agentId);
        map.put("success", success);
        map.put("message", message);
        map.put("type", type != null ? type.toString() : null);
        map.put("priority", priority);
        map.put("data", data);
        map.put("details", details);
        map.put("affected_devices", affectedDevices);
        map.put("error", error);
        map.put("warning", warning);
        map.put("timestamp", timestamp);
        map.put("response_time", responseTime);
        return map;
    }

    // ============== Response Builder ==============

    public static class ResponseBuilder {
        private Response response;

        public ResponseBuilder() {
            response = new Response();
        }

        public ResponseBuilder id(String id) {
            response.id = id;
            return this;
        }

        public ResponseBuilder requestId(String requestId) {
            response.requestId = requestId;
            return this;
        }

        public ResponseBuilder agentId(String agentId) {
            response.agentId = agentId;
            return this;
        }

        public ResponseBuilder success(boolean success) {
            response.success = success;
            return this;
        }

        public ResponseBuilder message(String message) {
            response.message = message;
            return this;
        }

        public ResponseBuilder type(ResponseType type) {
            response.type = type;
            return this;
        }

        public ResponseBuilder priority(String priority) {
            response.priority = priority;
            return this;
        }

        public ResponseBuilder data(Map<String, Object> data) {
            response.data = data;
            return this;
        }

        public ResponseBuilder error(String error) {
            response.error = error;
            response.success = false;
            return this;
        }

        public ResponseBuilder warning(String warning) {
            response.warning = warning;
            return this;
        }

        public ResponseBuilder affectedDevices(List<String> devices) {
            response.affectedDevices = devices;
            return this;
        }

        public Response build() {
            if (response.success && response.type == null) {
                response.type = ResponseType.SUCCESS;
            } else if (!response.success && response.type == null) {
                response.type = ResponseType.FAILED;
            }
            return response;
        }
    }

    // ============== Getters and Setters ==============

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Map<String, Object> getData() {
        if (data == null) {
            data = new HashMap<>();
        }
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getDetails() {
        if (details == null) {
            details = new HashMap<>();
        }
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public List<String> getAffectedDevices() {
        if (affectedDevices == null) {
            affectedDevices = new ArrayList<>();
        }
        return affectedDevices;
    }

    public void setAffectedDevices(List<String> affectedDevices) {
        this.affectedDevices = affectedDevices;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public List<Action> getSuggestedActions() {
        if (suggestedActions == null) {
            suggestedActions = new ArrayList<>();
        }
        return suggestedActions;
    }

    public void setSuggestedActions(List<Action> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }
}
package org.SmartAI_Agent.models;

import java.util.*;

/**
 * Command - Represents a command to be executed by an agent
 * Contains the action to perform and all necessary parameters
 */
public class Command {

    private String id;                      // Command identifier
    private String requestId;               // Original request ID
    private String action;                  // Action to perform
    private String target;                  // Target agent or device
    private Map<String, Object> parameters; // Command parameters
    private Map<String, Object> context;    // Command context
    private String priority;                // Command priority
    private CommandType type;               // Type of command
    private CommandStatus status;           // Current status
    private Date timestamp;                 // When command was created
    private Long timeout;                   // Timeout in milliseconds
    private Integer maxRetries;             // Maximum retry attempts
    private Integer currentRetry;           // Current retry count
    private boolean requiresConfirmation;   // Needs confirmation
    private boolean async;                  // Execute asynchronously
    private String source;                  // Source of command
    private List<String> dependencies;      // Dependent commands
    private Map<String, String> metadata;   // Command metadata

    /**
     * Command types
     */
    public enum CommandType {
        CONTROL,        // Device control command
        QUERY,          // Information query
        CONFIGURATION,  // Configuration change
        AUTOMATION,     // Automation command
        SCENE,          // Scene execution
        EMERGENCY,      // Emergency command
        SYSTEM,         // System command
        TEST           // Test command
    }

    /**
     * Command status
     */
    public enum CommandStatus {
        PENDING,        // Not yet executed
        QUEUED,         // In execution queue
        EXECUTING,      // Currently executing
        COMPLETED,      // Successfully completed
        FAILED,         // Execution failed
        CANCELLED,      // Command cancelled
        TIMEOUT,        // Command timed out
        RETRY           // Retrying execution
    }

    /**
     * Command priority levels
     */
    public static class Priority {
        public static final String LOW = "LOW";
        public static final String NORMAL = "NORMAL";
        public static final String HIGH = "HIGH";
        public static final String CRITICAL = "CRITICAL";
    }

    /**
     * Default constructor
     */
    public Command() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.parameters = new HashMap<>();
        this.context = new HashMap<>();
        this.metadata = new HashMap<>();
        this.dependencies = new ArrayList<>();
        this.type = CommandType.CONTROL;
        this.status = CommandStatus.PENDING;
        this.priority = Priority.NORMAL;
        this.timeout = 10000L; // 10 seconds default
        this.maxRetries = 3;
        this.currentRetry = 0;
        this.requiresConfirmation = false;
        this.async = false;
    }

    /**
     * Constructor with action and target
     */
    public Command(String action, String target) {
        this();
        this.action = action;
        this.target = target;
    }

    /**
     * Constructor with all basic fields
     */
    public Command(String action, String target, Map<String, Object> parameters) {
        this();
        this.action = action;
        this.target = target;
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }

    /**
     * Builder pattern
     */
    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    /**
     * Add a parameter
     */
    public void addParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(key, value);
    }

    /**
     * Add context
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
     * Add dependency
     */
    public void addDependency(String commandId) {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        dependencies.add(commandId);
    }

    /**
     * Get parameter value
     */
    public Object getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }

    /**
     * Get context value
     */
    public Object getContextValue(String key) {
        return context != null ? context.get(key) : null;
    }

    /**
     * Check if command is high priority
     */
    public boolean isHighPriority() {
        return Priority.HIGH.equals(priority) || Priority.CRITICAL.equals(priority);
    }

    /**
     * Check if command is critical
     */
    public boolean isCritical() {
        return Priority.CRITICAL.equals(priority) || type == CommandType.EMERGENCY;
    }

    /**
     * Check if command has timed out
     */
    public boolean hasTimedOut() {
        if (timeout == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - timestamp.getTime();
        return elapsed > timeout;
    }

    /**
     * Check if can retry
     */
    public boolean canRetry() {
        return currentRetry < maxRetries;
    }

    /**
     * Increment retry count
     */
    public void incrementRetry() {
        currentRetry++;
        status = CommandStatus.RETRY;
        addMetadata("retry_count", String.valueOf(currentRetry));
    }

    /**
     * Mark as executing
     */
    public void markExecuting() {
        status = CommandStatus.EXECUTING;
        addMetadata("execution_started", new Date().toString());
    }

    /**
     * Mark as completed
     */
    public void markCompleted() {
        status = CommandStatus.COMPLETED;
        addMetadata("completed_at", new Date().toString());
    }

    /**
     * Mark as failed
     */
    public void markFailed(String reason) {
        status = CommandStatus.FAILED;
        addMetadata("failed_at", new Date().toString());
        addMetadata("failure_reason", reason);
    }

    /**
     * Mark as cancelled
     */
    public void markCancelled() {
        status = CommandStatus.CANCELLED;
        addMetadata("cancelled_at", new Date().toString());
    }

    /**
     * Check if command is executable
     */
    public boolean isExecutable() {
        return status == CommandStatus.PENDING || status == CommandStatus.QUEUED;
    }

    /**
     * Check if command is completed
     */
    public boolean isCompleted() {
        return status == CommandStatus.COMPLETED;
    }

    /**
     * Check if command failed
     */
    public boolean isFailed() {
        return status == CommandStatus.FAILED || status == CommandStatus.TIMEOUT;
    }

    /**
     * Create a control command
     */
    public static Command control(String action, String target) {
        Command cmd = new Command(action, target);
        cmd.setType(CommandType.CONTROL);
        return cmd;
    }

    /**
     * Create a query command
     */
    public static Command query(String action, String target) {
        Command cmd = new Command(action, target);
        cmd.setType(CommandType.QUERY);
        return cmd;
    }

    /**
     * Create an emergency command
     */
    public static Command emergency(String action, String target) {
        Command cmd = new Command(action, target);
        cmd.setType(CommandType.EMERGENCY);
        cmd.setPriority(Priority.CRITICAL);
        cmd.setTimeout(5000L); // 5 second timeout
        cmd.setMaxRetries(1); // Only one retry for emergency
        return cmd;
    }

    /**
     * Create a scene command
     */
    public static Command scene(String sceneName) {
        Command cmd = new Command("execute_scene", "orchestrator");
        cmd.setType(CommandType.SCENE);
        cmd.addParameter("scene", sceneName);
        return cmd;
    }

    /**
     * Clone this command
     */
    public Command clone() {
        Command cloned = new Command();
        cloned.action = this.action;
        cloned.target = this.target;
        cloned.parameters = new HashMap<>(this.parameters);
        cloned.context = new HashMap<>(this.context);
        cloned.priority = this.priority;
        cloned.type = this.type;
        cloned.timeout = this.timeout;
        cloned.maxRetries = this.maxRetries;
        cloned.requiresConfirmation = this.requiresConfirmation;
        cloned.async = this.async;
        return cloned;
    }

    /**
     * Convert to string representation
     */
    @Override
    public String toString() {
        return String.format(
                "Command[id=%s, action=%s, target=%s, type=%s, status=%s, priority=%s]",
                id, action, target, type, status, priority
        );
    }

    /**
     * Convert to map for serialization
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("request_id", requestId);
        map.put("action", action);
        map.put("target", target);
        map.put("parameters", parameters);
        map.put("context", context);
        map.put("type", type != null ? type.toString() : null);
        map.put("status", status != null ? status.toString() : null);
        map.put("priority", priority);
        map.put("timestamp", timestamp);
        map.put("timeout", timeout);
        map.put("metadata", metadata);
        return map;
    }

    // ============== Command Builder ==============

    public static class CommandBuilder {
        private Command command;

        public CommandBuilder() {
            command = new Command();
        }

        public CommandBuilder id(String id) {
            command.id = id;
            return this;
        }

        public CommandBuilder requestId(String requestId) {
            command.requestId = requestId;
            return this;
        }

        public CommandBuilder action(String action) {
            command.action = action;
            return this;
        }

        public CommandBuilder target(String target) {
            command.target = target;
            return this;
        }

        public CommandBuilder parameters(Map<String, Object> parameters) {
            command.parameters = parameters;
            return this;
        }

        public CommandBuilder parameter(String key, Object value) {
            command.addParameter(key, value);
            return this;
        }

        public CommandBuilder context(Map<String, Object> context) {
            command.context = context;
            return this;
        }

        public CommandBuilder priority(String priority) {
            command.priority = priority;
            return this;
        }

        public CommandBuilder type(CommandType type) {
            command.type = type;
            return this;
        }

        public CommandBuilder timeout(Long timeout) {
            command.timeout = timeout;
            return this;
        }

        public CommandBuilder requiresConfirmation(boolean requires) {
            command.requiresConfirmation = requires;
            return this;
        }

        public CommandBuilder async(boolean async) {
            command.async = async;
            return this;
        }

        public Command build() {
            return command;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
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

    public Map<String, Object> getContext() {
        if (context == null) {
            context = new HashMap<>();
        }
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public CommandStatus getStatus() {
        return status;
    }

    public void setStatus(CommandStatus status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getCurrentRetry() {
        return currentRetry;
    }

    public void setCurrentRetry(Integer currentRetry) {
        this.currentRetry = currentRetry;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getDependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
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
}
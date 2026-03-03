// Path: D:\AI-Course\My_Agent\src\main\java\org\SmartAgent\models\AgentCapability.java
package org.SmartAI_Agent.models;

import java.util.*;

/**
 * AgentCapability - Represents what an agent can do
 * Defines the capabilities, actions, and constraints of each agent
 */
public class AgentCapability {

    private String id;                              // Capability identifier
    private String name;                            // Capability name
    private String description;                     // Human-readable description
    private String agentId;                         // Agent that has this capability
    private CapabilityType type;                    // Type of capability
    private List<String> supportedActions;          // Actions this capability supports
    private Map<String, ParameterDefinition> parameters; // Required/optional parameters
    private List<String> requiredPermissions;       // Permissions needed
    private List<Constraint> constraints;           // Operational constraints
    private boolean enabled;                        // Is capability enabled
    private boolean requiresConfirmation;           // Needs user confirmation
    private boolean critical;                       // Is this a critical capability
    private String category;                        // Category/group
    private Map<String, Object> metadata;           // Additional metadata
    private Date lastUsed;                          // Last time used
    private Integer usageCount;                     // Usage statistics

    /**
     * Capability types
     */
    public enum CapabilityType {
        CONTROL,        // Direct device control
        QUERY,          // Information query
        MONITORING,     // Monitoring capability
        AUTOMATION,     // Automation capability
        CONFIGURATION,  // Configuration capability
        EMERGENCY,      // Emergency response
        DIAGNOSTIC,     // Diagnostic capability
        MAINTENANCE     // Maintenance capability
    }

    /**
     * Parameter definition for capability
     */
    public static class ParameterDefinition {
        private String name;
        private String type;           // String, Integer, Boolean, etc.
        private boolean required;
        private Object defaultValue;
        private Object minValue;
        private Object maxValue;
        private List<Object> allowedValues;
        private String description;
        private String unit;           // Unit of measurement
        private String validation;     // Validation rule

        public ParameterDefinition() {
            this.required = false;
        }

        public ParameterDefinition(String name, String type, boolean required) {
            this.name = name;
            this.type = type;
            this.required = required;
        }

        // Builder pattern for ParameterDefinition
        public static ParameterBuilder builder() {
            return new ParameterBuilder();
        }

        public static class ParameterBuilder {
            private ParameterDefinition param = new ParameterDefinition();

            public ParameterBuilder name(String name) {
                param.name = name;
                return this;
            }

            public ParameterBuilder type(String type) {
                param.type = type;
                return this;
            }

            public ParameterBuilder required(boolean required) {
                param.required = required;
                return this;
            }

            public ParameterBuilder defaultValue(Object defaultValue) {
                param.defaultValue = defaultValue;
                return this;
            }

            public ParameterBuilder range(Object min, Object max) {
                param.minValue = min;
                param.maxValue = max;
                return this;
            }

            public ParameterBuilder allowedValues(List<Object> values) {
                param.allowedValues = values;
                return this;
            }

            public ParameterBuilder unit(String unit) {
                param.unit = unit;
                return this;
            }

            public ParameterDefinition build() {
                return param;
            }
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public Object getDefaultValue() { return defaultValue; }
        public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
        public Object getMinValue() { return minValue; }
        public void setMinValue(Object minValue) { this.minValue = minValue; }
        public Object getMaxValue() { return maxValue; }
        public void setMaxValue(Object maxValue) { this.maxValue = maxValue; }
        public List<Object> getAllowedValues() { return allowedValues; }
        public void setAllowedValues(List<Object> allowedValues) { this.allowedValues = allowedValues; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getValidation() { return validation; }
        public void setValidation(String validation) { this.validation = validation; }
    }

    /**
     * Constraint for capability
     */
    public static class Constraint {
        private String type;
        private String condition;
        private String value;
        private String message;

        public Constraint() {}

        public Constraint(String type, String condition, String value, String message) {
            this.type = type;
            this.condition = condition;
            this.value = value;
            this.message = message;
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Default constructor
     */
    public AgentCapability() {
        this.id = UUID.randomUUID().toString();
        this.supportedActions = new ArrayList<>();
        this.parameters = new HashMap<>();
        this.requiredPermissions = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.enabled = true;
        this.requiresConfirmation = false;
        this.critical = false;
        this.usageCount = 0;
    }

    /**
     * Constructor with basic fields
     */
    public AgentCapability(String name, CapabilityType type, String agentId) {
        this();
        this.name = name;
        this.type = type;
        this.agentId = agentId;
    }

    /**
     * Add supported action
     */
    public void addSupportedAction(String action) {
        if (supportedActions == null) {
            supportedActions = new ArrayList<>();
        }
        supportedActions.add(action);
    }

    /**
     * Add parameter definition
     */
    public void addParameter(String name, ParameterDefinition definition) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(name, definition);
    }

    /**
     * Add required permission
     */
    public void addRequiredPermission(String permission) {
        if (requiredPermissions == null) {
            requiredPermissions = new ArrayList<>();
        }
        requiredPermissions.add(permission);
    }

    /**
     * Add constraint
     */
    public void addConstraint(Constraint constraint) {
        if (constraints == null) {
            constraints = new ArrayList<>();
        }
        constraints.add(constraint);
    }

    /**
     * Check if capability supports an action
     */
    public boolean supportsAction(String action) {
        return supportedActions != null && supportedActions.contains(action);
    }

    /**
     * Check if capability has required parameters
     */
    public boolean hasRequiredParameters() {
        if (parameters == null || parameters.isEmpty()) {
            return false;
        }
        return parameters.values().stream().anyMatch(ParameterDefinition::isRequired);
    }

    /**
     * Get required parameters
     */
    public List<ParameterDefinition> getRequiredParameters() {
        List<ParameterDefinition> required = new ArrayList<>();
        if (parameters != null) {
            parameters.values().stream()
                    .filter(ParameterDefinition::isRequired)
                    .forEach(required::add);
        }
        return required;
    }

    /**
     * Validate parameters against definitions
     */
    public boolean validateParameters(Map<String, Object> providedParams) {
        if (parameters == null || parameters.isEmpty()) {
            return true; // No parameters required
        }

        // Check all required parameters are present
        for (Map.Entry<String, ParameterDefinition> entry : parameters.entrySet()) {
            ParameterDefinition def = entry.getValue();

            if (def.isRequired() && !providedParams.containsKey(entry.getKey())) {
                return false; // Required parameter missing
            }

            // Validate parameter value if present
            if (providedParams.containsKey(entry.getKey())) {
                Object value = providedParams.get(entry.getKey());

                // Check allowed values
                if (def.getAllowedValues() != null && !def.getAllowedValues().contains(value)) {
                    return false;
                }

                // Check min/max for numeric values
                if (value instanceof Number) {
                    double numValue = ((Number) value).doubleValue();
                    if (def.getMinValue() != null) {
                        double min = ((Number) def.getMinValue()).doubleValue();
                        if (numValue < min) return false;
                    }
                    if (def.getMaxValue() != null) {
                        double max = ((Number) def.getMaxValue()).doubleValue();
                        if (numValue > max) return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Check if capability is available
     */
    public boolean isAvailable() {
        return enabled && !critical; // Critical capabilities need special handling
    }

    /**
     * Increment usage count
     */
    public void incrementUsage() {
        usageCount++;
        lastUsed = new Date();
    }

    /**
     * Create a control capability
     */
    public static AgentCapability control(String name, String agentId, List<String> actions) {
        AgentCapability cap = new AgentCapability(name, CapabilityType.CONTROL, agentId);
        cap.setSupportedActions(actions);
        return cap;
    }

    /**
     * Create a query capability
     */
    public static AgentCapability query(String name, String agentId, List<String> actions) {
        AgentCapability cap = new AgentCapability(name, CapabilityType.QUERY, agentId);
        cap.setSupportedActions(actions);
        return cap;
    }

    /**
     * Create an emergency capability
     */
    public static AgentCapability emergency(String name, String agentId, List<String> actions) {
        AgentCapability cap = new AgentCapability(name, CapabilityType.EMERGENCY, agentId);
        cap.setSupportedActions(actions);
        cap.setCritical(true);
        cap.setRequiresConfirmation(false); // Emergency actions don't wait for confirmation
        return cap;
    }

    /**
     * Convert to string representation
     */
    @Override
    public String toString() {
        return String.format(
                "AgentCapability[name=%s, type=%s, agent=%s, enabled=%s, actions=%d]",
                name, type, agentId, enabled, supportedActions.size()
        );
    }

    /**
     * Convert to map for serialization
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("agent_id", agentId);
        map.put("type", type != null ? type.toString() : null);
        map.put("supported_actions", supportedActions);
        map.put("parameters", parameters);
        map.put("enabled", enabled);
        map.put("critical", critical);
        map.put("category", category);
        map.put("usage_count", usageCount);
        return map;
    }

    // ============== Getters and Setters ==============

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public CapabilityType getType() {
        return type;
    }

    public void setType(CapabilityType type) {
        this.type = type;
    }

    public List<String> getSupportedActions() {
        if (supportedActions == null) {
            supportedActions = new ArrayList<>();
        }
        return supportedActions;
    }

    public void setSupportedActions(List<String> supportedActions) {
        this.supportedActions = supportedActions;
    }

    public Map<String, ParameterDefinition> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        return parameters;
    }

    public void setParameters(Map<String, ParameterDefinition> parameters) {
        this.parameters = parameters;
    }

    public List<String> getRequiredPermissions() {
        if (requiredPermissions == null) {
            requiredPermissions = new ArrayList<>();
        }
        return requiredPermissions;
    }

    public void setRequiredPermissions(List<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    public List<Constraint> getConstraints() {
        if (constraints == null) {
            constraints = new ArrayList<>();
        }
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String, Object> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
}
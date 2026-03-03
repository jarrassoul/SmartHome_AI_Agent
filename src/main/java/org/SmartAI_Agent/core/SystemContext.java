package org.SmartAI_Agent.core;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.SmartAI_Agent.models.Intent;
import org.SmartAI_Agent.models.Response;

/**
 * SystemContext - Manages the overall system state and context
 * Tracks user preferences, device states, and system history
 */
public class SystemContext {

    // User contexts - stores information about each user
    private final Map<String, UserContext> userContexts = new ConcurrentHashMap<>();

    // Device states - current state of all devices
    private final Map<String, DeviceState> deviceStates = new ConcurrentHashMap<>();

    // System state - overall system status
    private final SystemState systemState = new SystemState();

    // Command history - recent commands
    private final List<CommandHistory> commandHistory = Collections.synchronizedList(new ArrayList<>());

    // Active scenarios
    private final Map<String, ScenarioState> activeScenarios = new ConcurrentHashMap<>();

    /**
     * Update context after processing a user request
     */
    public void updateContext(String userId, Intent intent, Response response) {
        System.out.println("Updating context for user: " + userId);

        // Get or create user context
        UserContext userContext = getUserContext(userId);

        // Update user activity
        userContext.updateLastActivity();
        userContext.incrementInteractionCount();

        // Record command in history
        addCommandToHistory(userId, intent, response);

        // Update user preferences based on usage
        updateUserPreferences(userId, intent);

        // Update system statistics
        systemState.updateStatistics(response.isSuccess());

        System.out.println("Context updated successfully");
    }

    /**
     * Get conversation history for a user
     */
    public List<CommandHistory> getConversationHistory(String userId, int limit) {
        UserContext userContext = getUserContext(userId);
        List<CommandHistory> userHistory = new ArrayList<>();
        
        // Filter command history for this specific user
        synchronized (commandHistory) {
            for (CommandHistory history : commandHistory) {
                if (userId.equals(history.getUserId())) {
                    userHistory.add(history);
                }
            }
        }
        
        // Return the most recent entries up to the limit
        int start = Math.max(0, userHistory.size() - limit);
        return userHistory.subList(start, userHistory.size());
    }

    /**
     * Get recent conversation context for a user
     */
    public List<Map<String, Object>> getRecentConversationContext(String userId, int limit) {
        UserContext userContext = getUserContext(userId);
        Map<String, Object> contextMap = userContext.toMap();
        Map<String, Object> conversationContext = (Map<String, Object>) contextMap.get("conversation_context");
        
        if (conversationContext != null) {
            List<String> recentRequests = (List<String>) conversationContext.get("recent_requests");
            List<String> recentResponses = (List<String>) conversationContext.get("recent_responses");
            
            List<Map<String, Object>> conversationPairs = new ArrayList<>();
            
            if (recentRequests != null && recentResponses != null) {
                int size = Math.min(recentRequests.size(), recentResponses.size());
                int start = Math.max(0, size - limit);
                
                for (int i = start; i < size; i++) {
                    Map<String, Object> pair = new HashMap<>();
                    pair.put("request", recentRequests.get(i));
                    pair.put("response", recentResponses.get(i));
                    conversationPairs.add(pair);
                }
            }
            
            return conversationPairs;
        }
        
        return new ArrayList<>();
    }

    /**
     * Get or create user context
     */
    public UserContext getUserContext(String userId) {
        return userContexts.computeIfAbsent(userId, k -> new UserContext(userId));
    }

    /**
     * Get current system context as a map
     */
    public Map<String, Object> getCurrentContext(String userId) {
        Map<String, Object> context = new HashMap<>();

        // Add user context
        UserContext userContext = getUserContext(userId);
        context.put("user", userContext.toMap());

        // Add temporal context
        context.put("temporal", getTemporalContext());

        // Add system state
        context.put("system", systemState.toMap());

        // Add device states summary
        context.put("devices", getDeviceStatesSummary());

        // Add active scenarios
        context.put("active_scenarios", activeScenarios.keySet());

        return context;
    }

    /**
     * Get temporal context (time-based information)
     */
    private Map<String, Object> getTemporalContext() {
        Map<String, Object> temporal = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();

        temporal.put("current_time", now.toString());
        temporal.put("hour", now.getHour());
        temporal.put("day_of_week", now.getDayOfWeek().toString());
        temporal.put("period_of_day", getPeriodOfDay(time));
        temporal.put("is_weekend", isWeekend(now));
        temporal.put("season", getSeason(now));

        return temporal;
    }

    /**
     * Determine period of day
     */
    private String getPeriodOfDay(LocalTime time) {
        int hour = time.getHour();

        if (hour >= 5 && hour < 12) {
            return "MORNING";
        } else if (hour >= 12 && hour < 17) {
            return "AFTERNOON";
        } else if (hour >= 17 && hour < 21) {
            return "EVENING";
        } else {
            return "NIGHT";
        }
    }

    /**
     * Check if it's weekend
     */
    private boolean isWeekend(LocalDateTime date) {
        String day = date.getDayOfWeek().toString();
        return "SATURDAY".equals(day) || "SUNDAY".equals(day);
    }

    /**
     * Get current season
     */
    private String getSeason(LocalDateTime date) {
        int month = date.getMonthValue();

        if (month >= 3 && month <= 5) {
            return "SPRING";
        } else if (month >= 6 && month <= 8) {
            return "SUMMER";
        } else if (month >= 9 && month <= 11) {
            return "AUTUMN";
        } else {
            return "WINTER";
        }
    }

    /**
     * Update device state
     */
    public void updateDeviceState(String deviceId, String status, Map<String, Object> attributes) {
        DeviceState state = deviceStates.computeIfAbsent(deviceId, k -> new DeviceState(deviceId));
        state.setStatus(status);
        state.setLastUpdate(LocalDateTime.now());

        if (attributes != null) {
            state.getAttributes().putAll(attributes);
        }

        System.out.println("Updated device state for: " + deviceId);
    }

    /**
     * Get device state
     */
    public DeviceState getDeviceState(String deviceId) {
        return deviceStates.get(deviceId);
    }

    /**
     * Get all device states summary
     */
    private Map<String, Object> getDeviceStatesSummary() {
        Map<String, Object> summary = new HashMap<>();

        int totalDevices = deviceStates.size();
        long activeDevices = deviceStates.values().stream()
                .filter(d -> "ACTIVE".equals(d.getStatus()) || "ON".equals(d.getStatus()))
                .count();

        summary.put("total", totalDevices);
        summary.put("active", activeDevices);
        summary.put("inactive", totalDevices - activeDevices);

        return summary;
    }

    /**
     * Add command to history
     */
    private void addCommandToHistory(String userId, Intent intent, Response response) {
        CommandHistory history = new CommandHistory();
        history.setUserId(userId);
        history.setIntent(intent.getAction());
        history.setDomain(intent.getDomain());
        history.setSuccess(response.isSuccess());
        history.setTimestamp(LocalDateTime.now());

        commandHistory.add(history);

        // Keep only last 100 commands
        if (commandHistory.size() > 100) {
            commandHistory.remove(0);
        }
    }

    /**
     * Get command history
     */
    public List<CommandHistory> getRecentCommands(int limit) {
        int start = Math.max(0, commandHistory.size() - limit);
        return new ArrayList<>(commandHistory.subList(start, commandHistory.size()));
    }

    /**
     * Update user preferences based on usage patterns
     */
    private void updateUserPreferences(String userId, Intent intent) {
        UserContext userContext = getUserContext(userId);

        // Track frequently used actions
        userContext.incrementActionCount(intent.getAction());

        // Track preferred times for certain actions
        String period = getPeriodOfDay(LocalTime.now());
        userContext.addTimePreference(intent.getAction(), period);

        // Update domain preferences
        userContext.incrementDomainUsage(intent.getDomain());
    }

    /**
     * Start a scenario
     */
    public void startScenario(String scenarioName, String userId) {
        ScenarioState scenario = new ScenarioState();
        scenario.setName(scenarioName);
        scenario.setUserId(userId);
        scenario.setStartTime(LocalDateTime.now());
        scenario.setActive(true);

        activeScenarios.put(scenarioName, scenario);
        System.out.println("Started scenario: " + scenarioName + " for user: " + userId);
    }

    /**
     * End a scenario
     */
    public void endScenario(String scenarioName) {
        ScenarioState scenario = activeScenarios.get(scenarioName);
        if (scenario != null) {
            scenario.setActive(false);
            scenario.setEndTime(LocalDateTime.now());
            activeScenarios.remove(scenarioName);
            System.out.println("Ended scenario: " + scenarioName);
        }
    }

    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_users", userContexts.size());
        stats.put("active_scenarios", activeScenarios.size());
        stats.put("total_devices", deviceStates.size());
        stats.put("commands_processed", systemState.getTotalCommands());
        stats.put("success_rate", systemState.getSuccessRate());
        stats.put("uptime_minutes", systemState.getUptimeMinutes());

        return stats;
    }

    /**
     * Reset user context
     */
    public void resetUserContext(String userId) {
        userContexts.remove(userId);
        System.out.println("Reset context for user: " + userId);
    }

    /**
     * Clear all contexts (for testing/reset)
     */
    public void clearAllContexts() {
        userContexts.clear();
        deviceStates.clear();
        commandHistory.clear();
        activeScenarios.clear();
        systemState.reset();
        System.out.println("All contexts cleared!");
    }

    // ============== Inner Classes ==============

    /**
     * User context information
     */
    public static class UserContext {
        private final String userId;
        private LocalDateTime lastActivity;
        private int interactionCount;
        private final Map<String, Integer> actionCounts = new HashMap<>();
        private final Map<String, String> preferences = new HashMap<>();
        private final Map<String, Integer> domainUsage = new HashMap<>();
        private final Map<String, List<String>> timePreferences = new HashMap<>();
        private final List<String> recentRequests = new ArrayList<>();
        private final List<String> recentResponses = new ArrayList<>();
        
        // Fields for confirmation mechanism
        private org.SmartAI_Agent.models.Command pendingCommand;
        private org.SmartAI_Agent.models.Intent pendingIntent;
        private List<String> remainingCompoundCommands;

        public UserContext(String userId) {
            this.userId = userId;
            this.lastActivity = LocalDateTime.now();
            this.interactionCount = 0;
            this.remainingCompoundCommands = new ArrayList<>();
        }

        public void updateLastActivity() {
            this.lastActivity = LocalDateTime.now();
        }

        public void incrementInteractionCount() {
            this.interactionCount++;
        }

        public void incrementActionCount(String action) {
            actionCounts.merge(action, 1, Integer::sum);
        }

        public void incrementDomainUsage(String domain) {
            domainUsage.merge(domain, 1, Integer::sum);
        }

        public void addTimePreference(String action, String timeOfDay) {
            timePreferences.computeIfAbsent(action, k -> new ArrayList<>()).add(timeOfDay);
        }

        public void addRecentRequest(String request) {
            recentRequests.add(request);
            if (recentRequests.size() > 10) {
                recentRequests.remove(0);
            }
        }

        public void addRecentResponse(String response) {
            recentResponses.add(response);
            if (recentResponses.size() > 10) {
                recentResponses.remove(0);
            }
        }

        public Map<String, Object> getConversationContext() {
            Map<String, Object> context = new HashMap<>();
            context.put("recent_requests", new ArrayList<>(recentRequests));
            context.put("recent_responses", new ArrayList<>(recentResponses));
            return context;
        }
        
        public void setPendingCommand(org.SmartAI_Agent.models.Command command) {
            this.pendingCommand = command;
        }

        public org.SmartAI_Agent.models.Command getPendingCommand() {
            return this.pendingCommand;
        }

        public void setPendingIntent(org.SmartAI_Agent.models.Intent intent) {
            this.pendingIntent = intent;
        }

        public org.SmartAI_Agent.models.Intent getPendingIntent() {
            return this.pendingIntent;
        }
        
        public void setRemainingCompoundCommands(List<String> commands) {
            this.remainingCompoundCommands = commands != null ? new ArrayList<>(commands) : new ArrayList<>();
        }
        
        public List<String> getRemainingCompoundCommands() {
            return this.remainingCompoundCommands;
        }
        
        public boolean hasRemainingCompoundCommands() {
            return !this.remainingCompoundCommands.isEmpty();
        }
        
        public String pollNextRemainingCommand() {
            if (this.remainingCompoundCommands.isEmpty()) {
                return null;
            }
            return this.remainingCompoundCommands.remove(0);
        }

        public void clearPendingCommand() {
            this.pendingCommand = null;
            this.pendingIntent = null;
            this.remainingCompoundCommands.clear();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("last_activity", lastActivity.toString());
            map.put("interaction_count", interactionCount);
            map.put("favorite_actions", getMostUsedActions());
            map.put("preferred_domain", getMostUsedDomain());
            map.put("conversation_context", getConversationContext());
            return map;
        }

        private List<String> getMostUsedActions() {
            return actionCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(ArrayList::new, (list, entry) -> list.add(entry), ArrayList::addAll);
        }

        private String getMostUsedDomain() {
            return domainUsage.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("general");
        }
    }

    /**
     * Device state information
     */
    public static class DeviceState {
        private final String deviceId;
        private String status;
        private LocalDateTime lastUpdate;
        private final Map<String, Object> attributes = new HashMap<>();

        public DeviceState(String deviceId) {
            this.deviceId = deviceId;
            this.status = "UNKNOWN";
            this.lastUpdate = LocalDateTime.now();
        }

        public String getDeviceId() { return deviceId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
        public Map<String, Object> getAttributes() { return attributes; }
    }

    /**
     * System state information
     */
    private static class SystemState {
        private LocalDateTime startTime;
        private long totalCommands;
        private long successfulCommands;
        private long failedCommands;

        public SystemState() {
            this.startTime = LocalDateTime.now();
            this.totalCommands = 0;
            this.successfulCommands = 0;
            this.failedCommands = 0;
        }

        public void updateStatistics(boolean success) {
            totalCommands++;
            if (success) {
                successfulCommands++;
            } else {
                failedCommands++;
            }
        }

        public long getTotalCommands() { return totalCommands; }

        public double getSuccessRate() {
            if (totalCommands == 0) return 0;
            return (double) successfulCommands / totalCommands * 100;
        }

        public long getUptimeMinutes() {
            return java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("uptime_minutes", getUptimeMinutes());
            map.put("total_commands", totalCommands);
            map.put("successful_commands", successfulCommands);
            map.put("failed_commands", failedCommands);
            map.put("success_rate", String.format("%.2f%%", getSuccessRate()));
            return map;
        }

        public void reset() {
            this.startTime = LocalDateTime.now();
            this.totalCommands = 0;
            this.successfulCommands = 0;
            this.failedCommands = 0;
        }
    }

    /**
     * Command history entry
     */
    public static class CommandHistory {
        private String userId;
        private String intent;
        private String domain;
        private boolean success;
        private LocalDateTime timestamp;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Scenario state information
     */
    private static class ScenarioState {
        private String name;
        private String userId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean active;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}
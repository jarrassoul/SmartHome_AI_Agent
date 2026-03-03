package org.SmartAI_Agent.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.SmartAI_Agent.models.Command;
import org.SmartAI_Agent.models.Device;
import org.SmartAI_Agent.models.Response;

/**
 * BaseAgent - Abstract base class for all specialized agents
 * Provides common functionality and interface for agent implementations
 */
public abstract class BaseAgent {

    protected String agentId;
    protected String status;
    protected Map<String, Object> agentContext;
    protected List<Device> managedDevices;

    /**
     * Default constructor
     */
    public BaseAgent(String agentId) {
        this.agentId = agentId;
        this.status = "INITIALIZING";
        this.agentContext = new HashMap<>();
        this.managedDevices = new ArrayList<>();
        initializeDevices();
    }

    /**
     * Initialize devices specific to this agent type
     */
    protected abstract void initializeDevices();

    /**
     * Process a command - to be implemented by subclasses
     */
    public abstract Response processCommand(Command command);

    /**
     * Get agent status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set agent status
     */
    protected void setStatus(String status) {
        this.status = status;
    }

    /**
     * Update agent context
     */
    protected void updateContext(String key, Object value) {
        agentContext.put(key, value);
    }

    /**
     * Get context value
     */
    protected Object getContextValue(String key) {
        return agentContext.get(key);
    }

    /**
     * Get list of managed devices
     */
    public List<Device> getManagedDevices() {
        return managedDevices;
    }

    /**
     * Add a device to the managed devices list
     */
    protected void addManagedDevice(Device device) {
        if (!managedDevices.contains(device)) {
            managedDevices.add(device);
        }
    }

    /**
     * Find a device by name
     */
    protected Device findDeviceByName(String name) {
        for (Device device : managedDevices) {
            if (device.getName().equalsIgnoreCase(name) || device.getId().equalsIgnoreCase(name)) {
                return device;
            }
        }
        return null;
    }

    /**
     * Check if this agent manages a specific device
     */
    public boolean managesDevice(String deviceId) {
        for (Device device : managedDevices) {
            if (device.getId().equals(deviceId) || device.getName().equalsIgnoreCase(deviceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get device status
     */
    public String getDeviceStatus(String deviceId) {
        Device device = findDeviceByName(deviceId);
        if (device != null) {
            return device.getStatus();
        }
        return "unknown";
    }

    /**
     * Build a success response
     */
    protected Response buildSuccessResponse(String message) {
        Response response = new Response();
        response.setSuccess(true);
        response.setMessage(message);
        response.setAgentId(agentId);
        return response;
    }

    /**
     * Build an error response
     */
    protected Response buildErrorResponse(String message) {
        Response response = new Response();
        response.setSuccess(false);
        response.setMessage(message);
        response.setAgentId(agentId);
        return response;
    }

    /**
     * Build a response with data
     */
    protected Response buildDataResponse(String message, Map<String, Object> data) {
        Response response = new Response();
        response.setSuccess(true);
        response.setMessage(message);
        response.setAgentId(agentId);
        response.setData(data);
        return response;
    }
}
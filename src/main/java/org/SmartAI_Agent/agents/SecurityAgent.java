package org.SmartAI_Agent.agents;

import java.util.HashMap;
import java.util.Map;

import org.SmartAI_Agent.models.Command;
import org.SmartAI_Agent.models.Device;
import org.SmartAI_Agent.models.Response;

/**
 * SecurityAgent - Handles all security-related commands
 * Controls locks, cameras, alarms, and security systems
 */
public class SecurityAgent extends BaseAgent {

    /**
     * Constructor
     */
    public SecurityAgent() {
        super("security");
        setStatus("ONLINE");
        updateContext("system_mode", "disarmed");
        updateContext("doors_locked", false);
        updateContext("cameras_active", false);
    }

    /**
     * Initialize devices specific to the security agent
     */
    @Override
    protected void initializeDevices() {
        // Add common security devices
        addManagedDevice(new Device("door_lock_001", "Front Door Lock", "door_lock", "access_control", "front_door"));
        addManagedDevice(new Device("door_lock_002", "Back Door Lock", "door_lock", "access_control", "back_door"));
        addManagedDevice(new Device("door_lock_003", "Garage Door Lock", "door_lock", "access_control", "garage"));
        addManagedDevice(new Device("camera_001", "Front Door Camera", "camera", "surveillance", "front_door"));
        addManagedDevice(new Device("camera_002", "Backyard Camera", "camera", "surveillance", "backyard"));
        addManagedDevice(new Device("camera_003", "Garage Camera", "camera", "surveillance", "garage"));
        addManagedDevice(new Device("motion_sensor_001", "Living Room Motion Sensor", "motion_sensor", "intrusion_detection", "living_room"));
        addManagedDevice(new Device("motion_sensor_002", "Kitchen Motion Sensor", "motion_sensor", "intrusion_detection", "kitchen"));
        addManagedDevice(new Device("alarm_001", "Main Alarm System", "alarm", "security_system", "basement"));
        addManagedDevice(new Device("window_sensor_001", "Living Room Window Sensor", "window_sensor", "intrusion_detection", "living_room"));
        addManagedDevice(new Device("window_sensor_002", "Bedroom Window Sensor", "window_sensor", "intrusion_detection", "bedroom"));
        
        // Add capabilities to devices
        for (Device device : managedDevices) {
            if (device.getType().equals("door_lock")) {
                device.addCapability("lock");
                device.addCapability("unlock");
                device.addCapability("get_status");
            } else if (device.getType().equals("camera")) {
                device.addCapability("start_recording");
                device.addCapability("stop_recording");
                device.addCapability("get_feed");
                device.addCapability("turn_on");
                device.addCapability("turn_off");
            } else if (device.getType().equals("motion_sensor")) {
                device.addCapability("enable");
                device.addCapability("disable");
                device.addCapability("get_status");
            } else if (device.getType().equals("alarm")) {
                device.addCapability("arm");
                device.addCapability("disarm");
                device.addCapability("trigger");
                device.addCapability("silence");
            } else if (device.getType().equals("window_sensor")) {
                device.addCapability("enable");
                device.addCapability("disable");
                device.addCapability("get_status");
            }
        }
    }

    /**
     * Process security commands
     */
    @Override
    public Response processCommand(Command command) {
        String action = command.getAction();
        Map<String, Object> params = command.getParameters();

        System.out.println("SecurityAgent processing command: " + action);

        switch (action.toLowerCase()) {
            case "lock_doors":
            case "lock":
                return handleLockDoors(params);
                
            case "unlock_doors":
            case "unlock":
                return handleUnlockDoors(params);
                
            case "arm_security":
                return handleArmSecurity(params);
                
            case "disarm_security":
                return handleDisarmSecurity(params);
                
            case "enable_night_mode":
            case "disable_night_mode":
                return handleNightMode(action, params);
                
            case "emergency":
                return handleEmergency(params);
                
            case "guest_mode":
                return handleGuestMode(params);
                
            case "stay_mode":
                return handleStayMode(params);
                
            case "show_camera":
                return handleShowCamera(params);
                
            case "all_off":
                return handleAllOff(params);
                
            case "emergency_stop":
                return handleEmergencyStop(params);
                
            case "status":
                return handleStatusQuery(params);
                
            case "query":
                return handleSecurityQuery(params);
                
            case "lock_front_door":
                return handleLockSpecificDoor("front_door", params);
                
            case "unlock_front_door":
                return handleUnlockSpecificDoor("front_door", params);
                
            case "lock_back_door":
                return handleLockSpecificDoor("back_door", params);
                
            case "unlock_back_door":
                return handleUnlockSpecificDoor("back_door", params);
                
            default:
                return buildErrorResponse("Unknown security command: " + action + ". I can help with locking/unlocking doors, arming/disarming security, camera controls, and security status.");
        }
    }

    /**
     * Handle lock specific door command
     */
    private Response handleLockSpecificDoor(String door, Map<String, Object> params) {
        updateContext(door + "_locked", true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("door", door);
        data.put("status", "locked");
        data.put("timestamp", System.currentTimeMillis());
        
        return buildDataResponse(door.replace("_", " ") + " has been locked.", data);
    }

    /**
     * Handle unlock specific door command
     */
    private Response handleUnlockSpecificDoor(String door, Map<String, Object> params) {
        updateContext(door + "_locked", false);
        
        Map<String, Object> data = new HashMap<>();
        data.put("door", door);
        data.put("status", "unlocked");
        data.put("timestamp", System.currentTimeMillis());
        
        return buildDataResponse(door.replace("_", " ") + " has been unlocked.", data);
    }

    /**
     * Handle lock doors command
     */
    private Response handleLockDoors(Map<String, Object> params) {
        updateContext("doors_locked", true);
        updateContext("system_mode", "armed");
        
        Map<String, Object> data = new HashMap<>();
        data.put("doors", "locked");
        data.put("timestamp", System.currentTimeMillis());
        
        return buildDataResponse("All doors have been locked. Security system is now armed.", data);
    }

    /**
     * Handle unlock doors command
     */
    private Response handleUnlockDoors(Map<String, Object> params) {
        updateContext("doors_locked", false);
        updateContext("system_mode", "disarmed");
        
        Map<String, Object> data = new HashMap<>();
        data.put("doors", "unlocked");
        data.put("timestamp", System.currentTimeMillis());
        
        return buildDataResponse("All doors have been unlocked. Security system is now disarmed.", data);
    }

    /**
     * Handle arm security command
     */
    private Response handleArmSecurity(Map<String, Object> params) {
        String mode = (String) params.getOrDefault("mode", "home");
        updateContext("system_mode", "armed_" + mode);
        updateContext("cameras_active", true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("mode", mode);
        data.put("status", "armed");
        data.put("cameras", "active");
        
        return buildDataResponse("Security system armed in " + mode + " mode. Cameras are now active.", data);
    }

    /**
     * Handle disarm security command
     */
    private Response handleDisarmSecurity(Map<String, Object> params) {
        updateContext("system_mode", "disarmed");
        updateContext("cameras_active", false);
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", "disarmed");
        data.put("cameras", "inactive");
        
        return buildDataResponse("Security system has been disarmed. Cameras are now inactive.", data);
    }

    /**
     * Handle night mode commands
     */
    private Response handleNightMode(String action, Map<String, Object> params) {
        boolean enable = action.contains("enable");
        updateContext("night_mode", enable);
        
        Map<String, Object> data = new HashMap<>();
        data.put("night_mode", enable);
        
        if (enable) {
            return buildDataResponse("Night mode has been enabled. Security system adjusted for night operation.", data);
        } else {
            return buildDataResponse("Night mode has been disabled.", data);
        }
    }

    /**
     * Handle emergency command
     */
    private Response handleEmergency(Map<String, Object> params) {
        updateContext("emergency_active", true);
        updateContext("doors_locked", false); // Unlock for emergency exit
        updateContext("system_mode", "emergency");
        
        Map<String, Object> data = new HashMap<>();
        data.put("emergency", "activated");
        data.put("doors", "unlocked");
        data.put("alarm", "sounding");
        
        return buildDataResponse("EMERGENCY PROTOCOL ACTIVATED! All doors unlocked, alarm sounding.", data);
    }

    /**
     * Handle guest mode command
     */
    private Response handleGuestMode(Map<String, Object> params) {
        updateContext("guest_mode", true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("guest_mode", true);
        
        return buildDataResponse("Guest mode activated. Security system adjusted for guests.", data);
    }

    /**
     * Handle stay mode command
     */
    private Response handleStayMode(Map<String, Object> params) {
        updateContext("system_mode", "stay");
        
        Map<String, Object> data = new HashMap<>();
        data.put("mode", "stay");
        
        return buildDataResponse("Stay mode activated. Security system adjusted for occupancy.", data);
    }

    /**
     * Handle show camera command
     */
    private Response handleShowCamera(Map<String, Object> params) {
        updateContext("cameras_active", true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("cameras", "active");
        data.put("feed", "available");
        
        return buildDataResponse("Camera feed is now available.", data);
    }

    /**
     * Handle all off command
     */
    private Response handleAllOff(Map<String, Object> params) {
        updateContext("system_mode", "minimal");
        updateContext("cameras_active", false);
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", "minimal");
        data.put("cameras", "inactive");
        
        return buildDataResponse("Security system set to minimal mode.", data);
    }

    /**
     * Handle emergency stop command
     */
    private Response handleEmergencyStop(Map<String, Object> params) {
        updateContext("emergency_active", false);
        updateContext("system_mode", "disarmed");
        
        Map<String, Object> data = new HashMap<>();
        data.put("emergency", "stopped");
        data.put("system", "disarmed");
        
        return buildDataResponse("Emergency stop executed. Security system disarmed.", data);
    }

    /**
     * Handle status query
     */
    private Response handleStatusQuery(Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("system_mode", getContextValue("system_mode"));
        data.put("doors_locked", getContextValue("doors_locked"));
        data.put("cameras_active", getContextValue("cameras_active"));
        data.put("night_mode", getContextValue("night_mode"));
        data.put("guest_mode", getContextValue("guest_mode"));
        
        StringBuilder message = new StringBuilder();
        message.append("Security System Status:\n");
        message.append("- System Mode: ").append(getContextValue("system_mode")).append("\n");
        message.append("- Doors Locked: ").append(getContextValue("doors_locked")).append("\n");
        message.append("- Cameras Active: ").append(getContextValue("cameras_active")).append("\n");
        message.append("- Night Mode: ").append(getContextValue("night_mode")).append("\n");
        message.append("- Guest Mode: ").append(getContextValue("guest_mode"));
        
        return buildDataResponse(message.toString(), data);
    }

    /**
     * Handle general security queries
     */
    private Response handleSecurityQuery(Map<String, Object> params) {
        // Provide general information about security capabilities
        return buildDataResponse("I can help you with security tasks such as:\n" +
                "- Locking/unlocking doors\n" +
                "- Arming/disarming the security system\n" +
                "- Activating night mode or guest mode\n" +
                "- Checking camera feeds\n" +
                "- Emergency protocols\n" +
                "What would you like me to help you with?", new HashMap<>());
    }
}
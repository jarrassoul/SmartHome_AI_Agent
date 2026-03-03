package org.SmartAI_Agent.agents;

import java.util.HashMap;
import java.util.Map;

import org.SmartAI_Agent.models.Command;
import org.SmartAI_Agent.models.Device;
import org.SmartAI_Agent.models.Response;

/**
 * ApplianceAgent - Handles all appliance-related commands
 * Controls lights, kitchen appliances, laundry equipment, and entertainment devices
 * Manages 13 devices across 4 categories: lighting, kitchen, laundry, and entertainment
 */
public class ApplianceAgent extends BaseAgent {

    // Context key constants for better maintainability
    private static final String CTX_LIGHTS_ON = "lights_on";
    private static final String CTX_COFFEE_BREWING = "coffee_brewing";
    private static final String CTX_KITCHEN_APPLIANCES = "kitchen_appliances";
    private static final String CTX_WASHING_MACHINE_STATUS = "washing_machine_status";
    private static final String CTX_DRYER_STATUS = "dryer_status";
    private static final String CTX_DISHWASHER_STATUS = "dishwasher_status";
    private static final String CTX_FRIDGE_STATUS = "fridge_status";
    private static final String CTX_MICROWAVE_STATUS = "microwave_status";
    private static final String CTX_OVEN_STATUS = "oven_status";
    private static final String CTX_TV_STATUS = "tv_status";
    private static final String CTX_TV_LOCATION_PREFIX = "tv_status_";

    /**
     * Constructor - Initializes the appliance agent with proper context state
     */
    public ApplianceAgent() {
        super("appliance");
        setStatus("ONLINE");
        initializeContextState();
    }

    /**
     * Initialize all context state values for proper state tracking
     */
    private void initializeContextState() {
        // Lighting state
        updateContext(CTX_LIGHTS_ON, false);
        
        // Coffee maker state
        updateContext(CTX_COFFEE_BREWING, false);
        
        // Kitchen appliances state map
        updateContext(CTX_KITCHEN_APPLIANCES, new HashMap<String, Object>());
        
        // Individual appliance statuses
        updateContext(CTX_WASHING_MACHINE_STATUS, "off");
        updateContext(CTX_DRYER_STATUS, "off");
        updateContext(CTX_DISHWASHER_STATUS, "off");
        updateContext(CTX_FRIDGE_STATUS, "on");  // Fridge is typically always on
        updateContext(CTX_MICROWAVE_STATUS, "off");
        updateContext(CTX_OVEN_STATUS, "off");
        
        // TV statuses by location
        updateContext(CTX_TV_STATUS, "off");
        updateContext(CTX_TV_LOCATION_PREFIX + "living_room", "off");
        updateContext(CTX_TV_LOCATION_PREFIX + "bedroom", "off");
    }

    /**
     * Initialize devices specific to the appliance agent
     */
    @Override
    protected void initializeDevices() {
        // Add common appliance devices
        addManagedDevice(new Device("light_001", "Living Room Lights", "light", "lighting", "living_room"));
        addManagedDevice(new Device("light_002", "Kitchen Lights", "light", "lighting", "kitchen"));
        addManagedDevice(new Device("light_003", "Bedroom Lights", "light", "lighting", "bedroom"));
        addManagedDevice(new Device("light_004", "Bathroom Lights", "light", "lighting", "bathroom"));
        addManagedDevice(new Device("coffee_maker_001", "Coffee Maker", "coffee_maker", "kitchen", "kitchen"));
        addManagedDevice(new Device("fridge_001", "Refrigerator", "fridge", "kitchen", "kitchen"));
        addManagedDevice(new Device("washing_machine_001", "Washing Machine", "washing_machine", "laundry", "laundry_room"));
        addManagedDevice(new Device("dryer_001", "Clothes Dryer", "dryer", "laundry", "laundry_room"));
        addManagedDevice(new Device("dishwasher_001", "Dishwasher", "dishwasher", "kitchen", "kitchen"));
        addManagedDevice(new Device("microwave_001", "Microwave", "microwave", "kitchen", "kitchen"));
        addManagedDevice(new Device("oven_001", "Oven", "oven", "kitchen", "kitchen"));
        addManagedDevice(new Device("tv_001", "Living Room TV", "tv", "entertainment", "living_room"));
        addManagedDevice(new Device("tv_002", "Bedroom TV", "tv", "entertainment", "bedroom"));
        
        // Add capabilities to devices
        for (Device device : managedDevices) {
            if (device.getType().equals("light")) {
                device.addCapability("turn_on");
                device.addCapability("turn_off");
                device.addCapability("dim");
            } else if (device.getType().equals("coffee_maker")) {
                device.addCapability("brew");
                device.addCapability("turn_on");
                device.addCapability("turn_off");
            } else if (device.getType().equals("fridge")) {
                device.addCapability("set_temperature");
                device.addCapability("turn_on");
                device.addCapability("turn_off");
            } else if (device.getType().equals("washing_machine")) {
                device.addCapability("start");
                device.addCapability("stop");
                device.addCapability("set_mode");
                device.addCapability("set_duration");
            } else if (device.getType().equals("dryer")) {
                device.addCapability("start");
                device.addCapability("stop");
                device.addCapability("set_mode");
                device.addCapability("set_duration");
            } else if (device.getType().equals("dishwasher")) {
                device.addCapability("start");
                device.addCapability("stop");
            } else if (device.getType().equals("microwave")) {
                device.addCapability("turn_on");
                device.addCapability("turn_off");
            } else if (device.getType().equals("oven")) {
                device.addCapability("turn_on");
                device.addCapability("turn_off");
            } else if (device.getType().equals("tv")) {
                device.addCapability("turn_on");
                device.addCapability("turn_off");
                device.addCapability("play_music");
            }
        }
    }

    /**
     * Process appliance commands
     */
    @Override
    public Response processCommand(Command command) {
        String action = command.getAction();
        Map<String, Object> params = command.getParameters();

        System.out.println("ApplianceAgent processing command: " + action);

        switch (action.toLowerCase()) {
            case "turn_on_lights":
            case "turn_on":
                return handleTurnOn(params);
                
            case "turn_off_lights":
            case "turn_off":
                return handleTurnOff(params);
                
            case "turn_on_tv":
                return handleTVControl("turn_on");
                
            case "turn_off_tv":
                return handleTVControl("turn_off");
                
            case "control_tv":
                return handleTVControl("control");
                
            case "brew_coffee":
                return handleBrewCoffee(params);
                
            case "dim_lights":
                return handleDimLights(params);
                
            case "welcome_lights":
                return handleWelcomeLights(params);
                
            case "party_lights":
                return handlePartyLights(params);
                
            case "all_lights_on":
                return handleAllLightsOn(params);
                
            case "all_off":
                return handleAllOff(params);
                
            case "control_appliance":
                return handleControlAppliance(params);
                
            case "status":
                return handleStatusQuery(params);
                
            case "query":
                return handleApplianceQuery(params);
                
            case "emergency_stop":
                return handleEmergencyStop(params);
                
            default:
                return buildErrorResponse("Unknown appliance command: " + action + ". I can help with lights, coffee maker, washing machine, and other household appliances.");
        }
    }

    /**
     * Handle turn on command
     */
    private Response handleTurnOn(Map<String, Object> params) {
        updateContext(CTX_LIGHTS_ON, true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("lights", "on");
        data.put("timestamp", System.currentTimeMillis());
        data.put("affected_devices", getDevicesByCategory("lighting"));
        
        return buildDataResponse("Lights have been turned on.", data);
    }

    /**
     * Handle turn off command
     */
    private Response handleTurnOff(Map<String, Object> params) {
        updateContext(CTX_LIGHTS_ON, false);
        updateContext(CTX_COFFEE_BREWING, false);
        
        Map<String, Object> data = new HashMap<>();
        data.put("lights", "off");
        data.put("appliances", "off");
        data.put("timestamp", System.currentTimeMillis());
        data.put("affected_devices", getDevicesByCategory("lighting"));
        
        return buildDataResponse("All lights and appliances have been turned off.", data);
    }

    /**
     * Get device names by category
     */
    private java.util.List<String> getDevicesByCategory(String category) {
        java.util.List<String> devices = new java.util.ArrayList<>();
        for (Device device : getManagedDevices()) {
            if (device.getCategory().equals(category)) {
                devices.add(device.getName());
            }
        }
        return devices;
    }

    /**
     * Handle brew coffee command
     */
    private Response handleBrewCoffee(Map<String, Object> params) {
        String strength = (String) params.getOrDefault("strength", "medium");
        updateContext(CTX_COFFEE_BREWING, true);
        
        // Update kitchen appliances map
        @SuppressWarnings("unchecked")
        Map<String, Object> kitchenAppliances = (Map<String, Object>) getContextValue(CTX_KITCHEN_APPLIANCES);
        if (kitchenAppliances != null) {
            kitchenAppliances.put("coffee_maker", "brewing_" + strength);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("coffee", "brewing");
        data.put("strength", strength);
        data.put("status", "in_progress");
        data.put("device", "coffee_maker_001");
        
        return buildDataResponse("Coffee brewing started with " + strength + " strength.", data);
    }

    /**
     * Handle dim lights command
     */
    private Response handleDimLights(Map<String, Object> params) {
        // Parse the level parameter correctly with validation
        Object levelObj = params.getOrDefault("level", 50);
        int level;
        
        if (levelObj instanceof String) {
            try {
                level = Integer.parseInt((String) levelObj);
            } catch (NumberFormatException e) {
                level = 50; // Default value if parsing fails
            }
        } else if (levelObj instanceof Integer) {
            level = (Integer) levelObj;
        } else if (levelObj instanceof Number) {
            level = ((Number) levelObj).intValue();
        } else {
            level = 50; // Default value for any other type
        }
        
        // Validate level range (0-100)
        level = Math.max(0, Math.min(100, level));
        
        updateContext(CTX_LIGHTS_ON, level > 0);
        
        Map<String, Object> data = new HashMap<>();
        data.put("lights", "dimmed");
        data.put("level", level);
        data.put("brightness_percent", level);
        data.put("affected_devices", getDevicesByCategory("lighting"));
        
        return buildDataResponse("Lights have been dimmed to " + level + "%.", data);
    }

    /**
     * Handle welcome lights command
     */
    private Response handleWelcomeLights(Map<String, Object> params) {
        updateContext(CTX_LIGHTS_ON, true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("lights", "welcome_sequence");
        data.put("status", "active");
        data.put("mode", "welcome");
        data.put("affected_devices", getDevicesByCategory("lighting"));
        
        return buildDataResponse("Welcome light sequence activated.", data);
    }

    /**
     * Handle party lights command
     */
    private Response handlePartyLights(Map<String, Object> params) {
        updateContext(CTX_LIGHTS_ON, true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("lights", "party_mode");
        data.put("status", "active");
        data.put("mode", "party");
        data.put("affected_devices", getDevicesByCategory("lighting"));
        
        return buildDataResponse("Party lights activated! Let's get the party started.", data);
    }

    /**
     * Handle all lights on command
     */
    private Response handleAllLightsOn(Map<String, Object> params) {
        updateContext(CTX_LIGHTS_ON, true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("lights", "all_on");
        data.put("status", "active");
        data.put("affected_devices", getDevicesByCategory("lighting"));
        
        return buildDataResponse("All lights have been turned on.", data);
    }

    /**
     * Handle all off command
     */
    private Response handleAllOff(Map<String, Object> params) {
        // Reset all appliance states
        updateContext(CTX_LIGHTS_ON, false);
        updateContext(CTX_COFFEE_BREWING, false);
        updateContext(CTX_WASHING_MACHINE_STATUS, "off");
        updateContext(CTX_DRYER_STATUS, "off");
        updateContext(CTX_DISHWASHER_STATUS, "off");
        updateContext(CTX_MICROWAVE_STATUS, "off");
        updateContext(CTX_OVEN_STATUS, "off");
        updateContext(CTX_TV_STATUS, "off");
        updateContext(CTX_TV_LOCATION_PREFIX + "living_room", "off");
        updateContext(CTX_TV_LOCATION_PREFIX + "bedroom", "off");
        // Note: Fridge stays on as it's typically always on
        
        Map<String, Object> data = new HashMap<>();
        data.put("lights", "off");
        data.put("appliances", "off");
        data.put("timestamp", System.currentTimeMillis());
        data.put("fridge_note", "Refrigerator remains on for food safety");
        
        return buildDataResponse("All appliances and lights have been turned off. (Refrigerator remains on for food safety)", data);
    }

    /**
     * Handle control appliance command (like washing machine, fridge, coffee maker, etc.)
     */
    private Response handleControlAppliance(Map<String, Object> params) {
        String appliance = (String) params.getOrDefault("appliance", "unknown");
        String action = (String) params.getOrDefault("action", "control");
        String mode = (String) params.get("mode");
        Object durationObj = params.get("duration");
        Object tempObj = params.get("temperature");
        
        switch (appliance) {
            case "washing_machine":
                return handleWashingMachineControl(action, mode, durationObj);
            case "fridge":
                return handleFridgeControl(action, tempObj);
            case "coffee_maker":
            case "coffee_machine":
                return handleCoffeeMakerControl(action);
            case "tv":
            case "television":
                return handleTVControl(action, params); // Use the new method with params
            case "microwave":
                return handleMicrowaveControl(action);
            case "oven":
                return handleOvenControl(action);
            case "dishwasher":
                return handleDishwasherControl(action);
            case "dryer":
                return handleDryerControl(action, mode, durationObj);
            case "lights":
                if ("turn_on".equals(action)) {
                    return handleTurnOn(new HashMap<>());
                } else if ("turn_off".equals(action)) {
                    return handleTurnOff(new HashMap<>());
                }
                break;
            default:
                // Handle generic appliance control
                return handleGenericApplianceControl(appliance, action);
        }
        
        return buildErrorResponse("Unsupported appliance: " + appliance);
    }

    /**
     * Handle washing machine control
     */
    private Response handleWashingMachineControl(String action, String mode, Object durationObj) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "washing_machine");
        data.put("action", action);
        data.put("device_id", "washing_machine_001");
        
        StringBuilder message = new StringBuilder();
        
        if ("turn_on".equals(action) || "start".equals(action)) {
            message.append("Washing machine turned on");
            
            if (mode != null) {
                message.append(" in ").append(mode).append(" mode");
                data.put("mode", mode);
            }
            
            if (durationObj != null) {
                int duration = parseDuration(durationObj, 60);
                message.append(" for ").append(duration).append(" minutes");
                data.put("duration", duration);
            }
            
            message.append(".");
            updateContext(CTX_WASHING_MACHINE_STATUS, "running");
        } else if ("turn_off".equals(action) || "stop".equals(action)) {
            message.append("Washing machine turned off.");
            updateContext(CTX_WASHING_MACHINE_STATUS, "off");
        } else {
            // Handle the case where action is not explicitly set
            if (mode != null || durationObj != null) {
                message.append("Washing machine configured");
                
                if (mode != null) {
                    message.append(" for ").append(mode).append(" wash");
                    data.put("mode", mode);
                }
                
                if (durationObj != null) {
                    int duration = parseDuration(durationObj, 60);
                    message.append(" for ").append(duration).append(" minutes");
                    data.put("duration", duration);
                }
                
                message.append(".");
                updateContext(CTX_WASHING_MACHINE_STATUS, "configured");
            } else {
                message.append("Washing machine control command executed.");
                updateContext(CTX_WASHING_MACHINE_STATUS, "controlled");
            }
        }
        
        data.put("status", getContextValue(CTX_WASHING_MACHINE_STATUS));
        
        return buildDataResponse(message.toString(), data);
    }

    /**
     * Parse duration from various object types
     */
    private int parseDuration(Object durationObj, int defaultValue) {
        if (durationObj instanceof String) {
            try {
                return Integer.parseInt((String) durationObj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else if (durationObj instanceof Integer) {
            return (Integer) durationObj;
        } else if (durationObj instanceof Number) {
            return ((Number) durationObj).intValue();
        }
        return defaultValue;
    }

    /**
     * Handle fridge control
     */
    private Response handleFridgeControl(String action, Object tempObj) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "fridge");
        data.put("action", action);
        data.put("device_id", "fridge_001");
        
        StringBuilder message = new StringBuilder();
        
        if ("turn_on".equals(action)) {
            message.append("Fridge turned on");
            
            if (tempObj != null) {
                int temperature = parseTemperature(tempObj, 37); // Default 37°F
                message.append(" and set to ").append(temperature).append(" degrees F");
                data.put("temperature", temperature);
            }
            
            message.append(".");
            updateContext(CTX_FRIDGE_STATUS, "on");
        } else if ("turn_off".equals(action)) {
            message.append("Fridge turned off.");
            updateContext(CTX_FRIDGE_STATUS, "off");
        } else {
            message.append("Fridge control command executed.");
            updateContext(CTX_FRIDGE_STATUS, "controlled");
        }
        
        data.put("status", getContextValue(CTX_FRIDGE_STATUS));
        
        return buildDataResponse(message.toString(), data);
    }

    /**
     * Parse temperature from various object types
     */
    private int parseTemperature(Object tempObj, int defaultValue) {
        if (tempObj instanceof String) {
            try {
                return Integer.parseInt((String) tempObj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else if (tempObj instanceof Integer) {
            return (Integer) tempObj;
        } else if (tempObj instanceof Number) {
            return ((Number) tempObj).intValue();
        }
        return defaultValue;
    }

    /**
     * Handle coffee maker control
     */
    private Response handleCoffeeMakerControl(String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "coffee_maker");
        data.put("action", action);
        data.put("device_id", "coffee_maker_001");
        
        if ("turn_on".equals(action)) {
            updateContext(CTX_COFFEE_BREWING, true);
            data.put("status", "brewing");
            return buildDataResponse("Coffee maker turned on. Brewing coffee now.", data);
        } else if ("turn_off".equals(action)) {
            updateContext(CTX_COFFEE_BREWING, false);
            data.put("status", "off");
            return buildDataResponse("Coffee maker turned off.", data);
        }
        
        data.put("status", getContextValue(CTX_COFFEE_BREWING));
        return buildDataResponse("Coffee maker control command executed.", data);
    }

    /**
     * Handle TV control
     */
    private Response handleTVControl(String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "tv");
        data.put("action", action);
        data.put("device_id", "tv_001"); // Default to living room TV
        
        if ("turn_on".equals(action)) {
            updateContext(CTX_TV_STATUS, "on");
            updateContext(CTX_TV_LOCATION_PREFIX + "living_room", "on");
            data.put("status", "on");
            data.put("location", "living_room");
            return buildDataResponse("TV turned on.", data);
        } else if ("turn_off".equals(action)) {
            updateContext(CTX_TV_STATUS, "off");
            updateContext(CTX_TV_LOCATION_PREFIX + "living_room", "off");
            data.put("status", "off");
            data.put("location", "living_room");
            return buildDataResponse("TV turned off.", data);
        }
        
        data.put("status", getContextValue(CTX_TV_STATUS));
        return buildDataResponse("TV control command executed.", data);
    }
    
    /**
     * Handle TV control with music parameters and location awareness
     */
    private Response handleTVControl(String action, Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "tv");
        data.put("action", action);
        
        // Determine which TV to control based on location parameter
        String location = (String) params.getOrDefault("location", "living_room");
        String deviceId = "living_room".equals(location) ? "tv_001" : "tv_002";
        data.put("device_id", deviceId);
        data.put("location", location);
        
        if ("turn_on".equals(action)) {
            updateContext(CTX_TV_STATUS, "on");
            updateContext(CTX_TV_LOCATION_PREFIX + location, "on");
            data.put("status", "on");
            
            // Check if this is a music request
            if (params.containsKey("music_request")) {
                String artist = (String) params.get("artist");
                if (artist != null && !artist.isEmpty()) {
                    return buildDataResponse("TV turned on in " + location + ". Playing music by " + artist + ".", data);
                } else {
                    return buildDataResponse("TV turned on in " + location + ". Ready to play music.", data);
                }
            } else {
                return buildDataResponse("TV turned on in " + location + ".", data);
            }
        } else if ("turn_off".equals(action)) {
            updateContext(CTX_TV_STATUS, "off");
            updateContext(CTX_TV_LOCATION_PREFIX + location, "off");
            data.put("status", "off");
            return buildDataResponse("TV turned off in " + location + ".", data);
        }
        
        // Handle music-specific requests
        if (params.containsKey("music_request")) {
            String artist = (String) params.get("artist");
            if (artist != null && !artist.isEmpty()) {
                return buildDataResponse("Playing music by " + artist + " on TV in " + location + ".", data);
            } else {
                return buildDataResponse("Playing music on TV in " + location + ".", data);
            }
        }
        
        data.put("status", getContextValue(CTX_TV_LOCATION_PREFIX + location));
        return buildDataResponse("TV control command executed in " + location + ".", data);
    }

    /**
     * Handle microwave control
     */
    private Response handleMicrowaveControl(String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "microwave");
        data.put("action", action);
        data.put("device_id", "microwave_001");
        
        if ("turn_on".equals(action)) {
            updateContext(CTX_MICROWAVE_STATUS, "on");
            data.put("status", "on");
            return buildDataResponse("Microwave turned on.", data);
        } else if ("turn_off".equals(action)) {
            updateContext(CTX_MICROWAVE_STATUS, "off");
            data.put("status", "off");
            return buildDataResponse("Microwave turned off.", data);
        }
        
        data.put("status", getContextValue(CTX_MICROWAVE_STATUS));
        return buildDataResponse("Microwave control command executed.", data);
    }

    /**
     * Handle oven control
     */
    private Response handleOvenControl(String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "oven");
        data.put("action", action);
        data.put("device_id", "oven_001");
        
        if ("turn_on".equals(action)) {
            updateContext(CTX_OVEN_STATUS, "on");
            data.put("status", "on");
            return buildDataResponse("Oven turned on.", data);
        } else if ("turn_off".equals(action)) {
            updateContext(CTX_OVEN_STATUS, "off");
            data.put("status", "off");
            return buildDataResponse("Oven turned off.", data);
        }
        
        data.put("status", getContextValue(CTX_OVEN_STATUS));
        return buildDataResponse("Oven control command executed.", data);
    }

    /**
     * Handle dishwasher control
     */
    private Response handleDishwasherControl(String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "dishwasher");
        data.put("action", action);
        data.put("device_id", "dishwasher_001");
        
        if ("turn_on".equals(action) || "start".equals(action)) {
            updateContext(CTX_DISHWASHER_STATUS, "running");
            data.put("status", "running");
            return buildDataResponse("Dishwasher started.", data);
        } else if ("turn_off".equals(action) || "stop".equals(action)) {
            updateContext(CTX_DISHWASHER_STATUS, "off");
            data.put("status", "off");
            return buildDataResponse("Dishwasher turned off.", data);
        }
        
        data.put("status", getContextValue(CTX_DISHWASHER_STATUS));
        return buildDataResponse("Dishwasher control command executed.", data);
    }

    /**
     * Handle dryer control
     */
    private Response handleDryerControl(String action, String mode, Object durationObj) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", "dryer");
        data.put("action", action);
        data.put("device_id", "dryer_001");
        
        StringBuilder message = new StringBuilder();
        
        if ("turn_on".equals(action) || "start".equals(action)) {
            message.append("Dryer turned on");
            
            if (mode != null) {
                message.append(" in ").append(mode).append(" mode");
                data.put("mode", mode);
            }
            
            if (durationObj != null) {
                int duration = parseDuration(durationObj, 60);
                message.append(" for ").append(duration).append(" minutes");
                data.put("duration", duration);
            }
            
            message.append(".");
            updateContext(CTX_DRYER_STATUS, "running");
        } else if ("turn_off".equals(action) || "stop".equals(action)) {
            message.append("Dryer turned off.");
            updateContext(CTX_DRYER_STATUS, "off");
        } else {
            message.append("Dryer control command executed.");
            updateContext(CTX_DRYER_STATUS, "controlled");
        }
        
        data.put("status", getContextValue(CTX_DRYER_STATUS));
        
        return buildDataResponse(message.toString(), data);
    }

    /**
     * Handle generic appliance control
     */
    private Response handleGenericApplianceControl(String appliance, String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("appliance", appliance);
        data.put("action", action);
        
        String statusKey = appliance + "_status";
        
        if ("turn_on".equals(action)) {
            updateContext(statusKey, "on");
            data.put("status", "on");
            return buildDataResponse(appliance.substring(0, 1).toUpperCase() + appliance.substring(1) + " turned on.", data);
        } else if ("turn_off".equals(action)) {
            updateContext(statusKey, "off");
            data.put("status", "off");
            return buildDataResponse(appliance.substring(0, 1).toUpperCase() + appliance.substring(1) + " turned off.", data);
        }
        
        return buildDataResponse(appliance.substring(0, 1).toUpperCase() + appliance.substring(1) + " control command executed.", data);
    }

    /**
     * Handle status query
     */
    private Response handleStatusQuery(Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("lights_on", getContextValue("lights_on"));
        data.put("coffee_brewing", getContextValue("coffee_brewing"));
        data.put("washing_machine_status", getContextValue("washing_machine_status"));
        
        StringBuilder message = new StringBuilder();
        message.append("Appliance Status:\n");
        message.append("- Lights: ").append(getContextValue("lights_on")).append("\n");
        message.append("- Coffee Maker: ").append(getContextValue("coffee_brewing") != null && (Boolean) getContextValue("coffee_brewing") ? "brewing" : "idle").append("\n");
        message.append("- Washing Machine: ").append(getContextValue("washing_machine_status"));
        
        return buildDataResponse(message.toString(), data);
    }

    /**
     * Handle general appliance queries
     */
    private Response handleApplianceQuery(Map<String, Object> params) {
        // Provide general information about appliance capabilities
        return buildDataResponse("I can help you with various appliances such as:\n" +
                "- Light controls (turn on/off, dim)\n" +
                "- Coffee maker\n" +
                "- Washing machine\n" +
                "- Party lighting modes\n" +
                "What appliance would you like to control?", new HashMap<>());
    }

    /**
     * Handle emergency stop command
     */
    private Response handleEmergencyStop(Map<String, Object> params) {
        // Store previous state for potential recovery
        Map<String, Object> previousState = new HashMap<>();
        previousState.put(CTX_LIGHTS_ON, getContextValue(CTX_LIGHTS_ON));
        previousState.put(CTX_COFFEE_BREWING, getContextValue(CTX_COFFEE_BREWING));
        previousState.put(CTX_WASHING_MACHINE_STATUS, getContextValue(CTX_WASHING_MACHINE_STATUS));
        previousState.put(CTX_DRYER_STATUS, getContextValue(CTX_DRYER_STATUS));
        previousState.put(CTX_OVEN_STATUS, getContextValue(CTX_OVEN_STATUS));
        previousState.put(CTX_MICROWAVE_STATUS, getContextValue(CTX_MICROWAVE_STATUS));
        previousState.put(CTX_DISHWASHER_STATUS, getContextValue(CTX_DISHWASHER_STATUS));
        
        // Emergency shutdown of all appliances
        updateContext(CTX_LIGHTS_ON, false);
        updateContext(CTX_COFFEE_BREWING, false);
        updateContext(CTX_WASHING_MACHINE_STATUS, "emergency_stop");
        updateContext(CTX_DRYER_STATUS, "emergency_stop");
        updateContext(CTX_DISHWASHER_STATUS, "emergency_stop");
        updateContext(CTX_OVEN_STATUS, "emergency_stop");
        updateContext(CTX_MICROWAVE_STATUS, "emergency_stop");
        updateContext(CTX_TV_STATUS, "off");
        updateContext(CTX_TV_LOCATION_PREFIX + "living_room", "off");
        updateContext(CTX_TV_LOCATION_PREFIX + "bedroom", "off");
        // Fridge remains on for food safety
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", "emergency_stop");
        data.put("all_systems", "halted");
        data.put("previous_state", previousState);
        data.put("timestamp", System.currentTimeMillis());
        data.put("fridge_note", "Refrigerator remains on for food safety");
        
        return buildDataResponse("Emergency stop executed. All appliance systems halted. (Refrigerator remains on for food safety)", data);
    }
}
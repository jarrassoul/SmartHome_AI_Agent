package org.SmartAI_Agent.agents;

import java.util.HashMap;
import java.util.Map;

import org.SmartAI_Agent.models.Command;
import org.SmartAI_Agent.models.Device;
import org.SmartAI_Agent.models.Response;

/**
 * ClimateAgent - Handles all climate-related commands
 * Controls temperature, humidity, and HVAC systems
 */
public class ClimateAgent extends BaseAgent {

    /**
     * Constructor
     */
    public ClimateAgent() {
        super("climate");
        setStatus("ONLINE");
        updateContext("current_temperature", 72); // 22°C = 72°F
        updateContext("target_temperature", 72);
        updateContext("hvac_mode", "auto");
        updateContext("fan_speed", "medium");
        updateContext("temperature_unit", "F"); // Use Fahrenheit
    }

    /**
     * Initialize devices specific to the climate agent
     */
    @Override
    protected void initializeDevices() {
        // Add common climate devices
        addManagedDevice(new Device("thermostat_001", "Main Thermostat", "thermostat", "climate_control", "living_room"));
        addManagedDevice(new Device("hvac_001", "Central HVAC System", "hvac", "climate_control", "basement"));
        addManagedDevice(new Device("fan_001", "Living Room Fan", "fan", "climate_control", "living_room"));
        addManagedDevice(new Device("fan_002", "Bedroom Fan", "fan", "climate_control", "bedroom"));
        addManagedDevice(new Device("humidifier_001", "Whole House Humidifier", "humidifier", "climate_control", "basement"));
        addManagedDevice(new Device("dehumidifier_001", "Basement Dehumidifier", "dehumidifier", "climate_control", "basement"));
        
        // Add capabilities to devices
        for (Device device : managedDevices) {
            if (device.getType().equals("thermostat")) {
                device.addCapability("set_temperature");
                device.addCapability("get_temperature");
                device.addCapability("set_mode");
            } else if (device.getType().equals("hvac")) {
                device.addCapability("turn_on");
                device.addCapability("turn_off");
                device.addCapability("set_mode");
                device.addCapability("set_temperature");
            } else if (device.getType().equals("fan")) {
                device.addCapability("turn_on");
                device.addCapability("turn_off");
                device.addCapability("set_speed");
            } else if (device.getType().equals("humidifier")) {
                device.addCapability("turn_on");
                device.addCapability("turn_off");
                device.addCapability("set_humidity");
            } else if (device.getType().equals("dehumidifier")) {
                device.addCapability("turn_on");
                device.addCapability("turn_off");
                device.addCapability("set_humidity");
            }
        }
    }

    /**
     * Process climate commands
     */
    @Override
    public Response processCommand(Command command) {
        String action = command.getAction();
        Map<String, Object> params = command.getParameters();

        System.out.println("ClimateAgent processing command: " + action);

        switch (action.toLowerCase()) {
            case "set_temperature":
                return handleSetTemperature(params);
                
            case "increase_temperature":
            case "raise_temperature":
                return handleIncreaseTemperature(params);
                
            case "decrease_temperature":
            case "lower_temperature":
                return handleDecreaseTemperature(params);
                
            case "turn_on_ac":
            case "turn_on_air_conditioner":
                return handleTurnOnAC(params);
                
            case "turn_off_ac":
            case "turn_off_air_conditioner":
                return handleTurnOffAC(params);
                
            case "control_appliance":
                return handleControlAppliance(params);
                
            case "eco_mode":
                return handleEcoMode(params);
                
            case "comfort_mode":
                return handleComfortMode(params);
                
            case "max_ventilation":
                return handleMaxVentilation(params);
                
            case "all_off":
                return handleAllOff(params);
                
            case "emergency_stop":
                return handleEmergencyStop(params);
                
            case "status":
                return handleStatusQuery(params);
                
            case "query":
                return handleClimateQuery(params);
                
            case "set_fan_speed":
                return handleSetFanSpeed(params);
                
            case "set_humidity":
                return handleSetHumidity(params);
                
            default:
                return buildErrorResponse("Unknown climate command: " + action + ". I can help with temperature control, HVAC modes, and climate system status.");
        }
    }

    /**
     * Handle temperature setting with unit display
     */
    private Response handleSetTemperature(Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        
        if (params != null && params.containsKey("temperature")) {
            int tempFahrenheit = ((Number) params.get("temperature")).intValue();
            data.put("temperature", tempFahrenheit);
            
            // Display in original unit if available
            if (params.containsKey("display_temperature") && params.containsKey("display_unit")) {
                int displayTemp = ((Number) params.get("display_temperature")).intValue();
                String displayUnit = (String) params.get("display_unit");
                data.put("display_temperature", displayTemp);
                data.put("display_unit", displayUnit);
                return buildDataResponse("Temperature set to " + displayTemp + "°" + displayUnit + " (" + tempFahrenheit + "°F)", data);
            } else {
                return buildDataResponse("Temperature set to " + tempFahrenheit + "°F", data);
            }
        }
        
        return buildDataResponse("Temperature setting updated.", data);
    }

    /**
     * Handle turn off AC command
     */
    private Response handleTurnOffAC(Map<String, Object> params) {
        updateContext("ac_status", "off");
        updateContext("hvac_mode", "off");
        
        Map<String, Object> data = new HashMap<>();
        data.put("ac_status", "off");
        data.put("mode", "off");
        
        return buildDataResponse("Air conditioner turned off.", data);
    }
    
    /**
     * Handle control appliance command
     */
    private Response handleControlAppliance(Map<String, Object> params) {
        String appliance = (String) params.getOrDefault("appliance", "");
        
        if ("air_conditioner".equals(appliance)) {
            // Check if there's an action specified
            String action = (String) params.getOrDefault("action", "turn_on");
            
            if ("turn_on".equals(action) || "on".equals(action)) {
                return handleTurnOnAC(params);
            } else if ("turn_off".equals(action) || "off".equals(action)) {
                return handleTurnOffAC(params);
            } else {
                // Default to turn on
                return handleTurnOnAC(params);
            }
        }
        
        return buildErrorResponse("Unknown appliance: " + appliance + ". I can only control air conditioners.");
    }

    /**
     * Handle set fan speed command
     */
    private Response handleSetFanSpeed(Map<String, Object> params) {
        String speed = (String) params.getOrDefault("speed", "medium");
        updateContext("fan_speed", speed);
        
        Map<String, Object> data = new HashMap<>();
        data.put("fan_speed", speed);
        
        return buildDataResponse("Fan speed set to " + speed + ".", data);
    }

    /**
     * Handle set humidity command
     */
    private Response handleSetHumidity(Map<String, Object> params) {
        // Fix the type casting issue - parse the humidity parameter correctly
        Object humidityObj = params.getOrDefault("humidity", 45);
        int humidity;
        
        if (humidityObj instanceof String) {
            try {
                humidity = Integer.parseInt((String) humidityObj);
            } catch (NumberFormatException e) {
                humidity = 45; // Default value if parsing fails
            }
        } else if (humidityObj instanceof Integer) {
            humidity = (Integer) humidityObj;
        } else {
            humidity = 45; // Default value for any other type
        }
        
        updateContext("target_humidity", humidity);
        
        Map<String, Object> data = new HashMap<>();
        data.put("target_humidity", humidity);
        
        return buildDataResponse("Humidity set to " + humidity + "%.", data);
    }

    /**
     * Handle increase temperature command
     */
    private Response handleIncreaseTemperature(Map<String, Object> params) {
        // Fix the type casting issue
        Object tempObj = getContextValue("current_temperature");
        int currentTemp;
        
        if (tempObj instanceof String) {
            try {
                currentTemp = Integer.parseInt((String) tempObj);
            } catch (NumberFormatException e) {
                currentTemp = 72; // Default value if parsing fails
            }
        } else if (tempObj instanceof Integer) {
            currentTemp = (Integer) tempObj;
        } else {
            currentTemp = 72; // Default value for any other type
        }
        
        int newTemp = currentTemp + 2;
        
        updateContext("target_temperature", newTemp);
        updateContext("current_temperature", newTemp);
        
        Map<String, Object> data = new HashMap<>();
        data.put("previous_temperature", currentTemp);
        data.put("new_temperature", newTemp);
        data.put("change", "+2");
        data.put("unit", "Fahrenheit");
        
        return buildDataResponse("Temperature increased to " + newTemp + " degrees F.", data);
    }

    /**
     * Handle decrease temperature command
     */
    private Response handleDecreaseTemperature(Map<String, Object> params) {
        // Fix the type casting issue
        Object tempObj = getContextValue("current_temperature");
        int currentTemp;
        
        if (tempObj instanceof String) {
            try {
                currentTemp = Integer.parseInt((String) tempObj);
            } catch (NumberFormatException e) {
                currentTemp = 72; // Default value if parsing fails
            }
        } else if (tempObj instanceof Integer) {
            currentTemp = (Integer) tempObj;
        } else {
            currentTemp = 72; // Default value for any other type
        }
        
        int newTemp = currentTemp - 2;
        
        updateContext("target_temperature", newTemp);
        updateContext("current_temperature", newTemp);
        
        Map<String, Object> data = new HashMap<>();
        data.put("previous_temperature", currentTemp);
        data.put("new_temperature", newTemp);
        data.put("change", "-2");
        data.put("unit", "Fahrenheit");
        
        return buildDataResponse("Temperature decreased to " + newTemp + " degrees F.", data);
    }

    /**
     * Handle turn on AC command
     */
    private Response handleTurnOnAC(Map<String, Object> params) {
        updateContext("ac_status", "on");
        updateContext("hvac_mode", "cool");
        
        Map<String, Object> data = new HashMap<>();
        data.put("ac_status", "on");
        data.put("mode", "cool");
        
        return buildDataResponse("Air conditioner turned on in cooling mode.", data);
    }
    
    /**
     * Handle eco mode command
     */
    private Response handleEcoMode(Map<String, Object> params) {
        updateContext("hvac_mode", "eco");
        updateContext("fan_speed", "low");
        
        // Fix the type casting issue
        Object tempObj = getContextValue("current_temperature");
        int currentTemp;
        
        if (tempObj instanceof String) {
            try {
                currentTemp = Integer.parseInt((String) tempObj);
            } catch (NumberFormatException e) {
                currentTemp = 72; // Default value if parsing fails
            }
        } else if (tempObj instanceof Integer) {
            currentTemp = (Integer) tempObj;
        } else {
            currentTemp = 72; // Default value for any other type
        }
        
        int ecoTemp = Math.max(currentTemp - 5, 65); // Don't go below 65°F (instead of 18°C)
        updateContext("target_temperature", ecoTemp);
        
        Map<String, Object> data = new HashMap<>();
        data.put("mode", "eco");
        data.put("fan_speed", "low");
        data.put("target_temperature", ecoTemp);
        data.put("unit", "Fahrenheit");
        
        return buildDataResponse("Eco mode activated. Temperature set to " + ecoTemp + " degrees F to save energy.", data);
    }

    /**
     * Handle comfort mode command
     */
    private Response handleComfortMode(Map<String, Object> params) {
        updateContext("hvac_mode", "comfort");
        updateContext("fan_speed", "medium");
        
        int comfortTemp = 72; // 22°C = 72°F
        updateContext("target_temperature", comfortTemp);
        
        Map<String, Object> data = new HashMap<>();
        data.put("mode", "comfort");
        data.put("fan_speed", "medium");
        data.put("target_temperature", comfortTemp);
        data.put("unit", "Fahrenheit");
        
        return buildDataResponse("Comfort mode activated. Temperature set to " + comfortTemp + " degrees F.", data);
    }

    /**
     * Handle max ventilation command
     */
    private Response handleMaxVentilation(Map<String, Object> params) {
        updateContext("hvac_mode", "ventilation");
        updateContext("fan_speed", "high");
        
        Map<String, Object> data = new HashMap<>();
        data.put("mode", "ventilation");
        data.put("fan_speed", "high");
        data.put("status", "maximum_airflow");
        
        return buildDataResponse("Maximum ventilation activated. Airflow set to high.", data);
    }

    /**
     * Handle all off command
     */
    private Response handleAllOff(Map<String, Object> params) {
        updateContext("hvac_mode", "off");
        updateContext("fan_speed", "off");
        
        Map<String, Object> data = new HashMap<>();
        data.put("hvac", "off");
        data.put("fan_speed", "off");
        
        return buildDataResponse("Climate control system turned off.", data);
    }

    /**
     * Handle emergency stop command
     */
    private Response handleEmergencyStop(Map<String, Object> params) {
        updateContext("hvac_mode", "emergency");
        updateContext("fan_speed", "off");
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", "emergency_stop");
        data.put("hvac", "emergency_halt");
        
        return buildDataResponse("Emergency stop executed. Climate systems halted.", data);
    }

    /**
     * Handle status query
     */
    private Response handleStatusQuery(Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("current_temperature", getContextValue("current_temperature"));
        data.put("target_temperature", getContextValue("target_temperature"));
        data.put("hvac_mode", getContextValue("hvac_mode"));
        data.put("fan_speed", getContextValue("fan_speed"));
        
        StringBuilder message = new StringBuilder();
        message.append("Climate System Status:\n");
        message.append("- Current Temperature: ").append(getContextValue("current_temperature")).append(" degrees F\n");
        message.append("- Target Temperature: ").append(getContextValue("target_temperature")).append(" degrees F\n");
        message.append("- HVAC Mode: ").append(getContextValue("hvac_mode")).append("\n");
        message.append("- Fan Speed: ").append(getContextValue("fan_speed"));
        
        return buildDataResponse(message.toString(), data);
    }

    /**
     * Handle general climate queries
     */
    private Response handleClimateQuery(Map<String, Object> params) {
        // Provide general information about climate capabilities
        return buildDataResponse("I can help you with climate control such as:\n" +
                "- Setting temperature\n" +
                "- Increasing/decreasing temperature\n" +
                "- Eco and comfort modes\n" +
                "- Maximum ventilation\n" +
                "- Climate system status\n" +
                "What would you like to adjust with the climate system?", new HashMap<>());
    }
}
package org.SmartAI_Agent.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Device - Represents a smart home device that can be controlled by the AI agents
 */
public class Device {
    private String id;
    private String name;
    private String type;
    private String category;
    private String location;
    private List<String> capabilities;
    private boolean isConnected;
    private String status;
    
    public Device() {
        this.capabilities = new ArrayList<>();
    }
    
    public Device(String id, String name, String type, String category, String location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.location = location;
        this.capabilities = new ArrayList<>();
        this.isConnected = true;
        this.status = "online";
    }
    
    // Getters and setters
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public List<String> getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }
    
    public void addCapability(String capability) {
        if (!this.capabilities.contains(capability)) {
            this.capabilities.add(capability);
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public void setConnected(boolean connected) {
        isConnected = connected;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", location='" + location + '\'' +
                ", capabilities=" + capabilities +
                ", isConnected=" + isConnected +
                ", status='" + status + '\'' +
                '}';
    }
}
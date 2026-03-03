package org.SmartAI_Agent;

import org.SmartAI_Agent.Config.AppConfig;
import org.SmartAI_Agent.core.MainOrchestrator;
import org.SmartAI_Agent.models.Response;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for the Smart Home AI Agent system
 */
public class SmartHomeApplicationTest {
    
    private AppConfig appConfig;
    private MainOrchestrator orchestrator;
    
    @BeforeEach
    public void setUp() {
        appConfig = new AppConfig();
        appConfig.initialize();
        orchestrator = appConfig.getMainOrchestrator();
    }
    
    @Test
    public void testProcessSimpleCommand() {
        Response response = orchestrator.processUserRequest("Turn on the lights", "testUser");
        assertNotNull(response);
        assertNotNull(response.getMessage());
        System.out.println("Response: " + response.getMessage());
    }
    
    @Test
    public void testProcessTemperatureCommand() {
        Response response = orchestrator.processUserRequest("Set temperature to 22 degrees", "testUser");
        assertNotNull(response);
        assertNotNull(response.getMessage());
        // Command may require confirmation, so we don't assert success
        System.out.println("Response: " + response.getMessage());
    }
    
    @Test
    public void testProcessSecurityCommand() {
        Response response = orchestrator.processUserRequest("Lock all doors", "testUser");
        assertNotNull(response);
        assertNotNull(response.getMessage());
        System.out.println("Response: " + response.getMessage());
    }
    
    @Test
    public void testProcessApplianceCommand() {
        Response response = orchestrator.processUserRequest("Brew coffee", "testUser");
        assertNotNull(response);
        assertNotNull(response.getMessage());
        // Command may require confirmation, so we don't assert success
        System.out.println("Response: " + response.getMessage());
    }
    
    @Test
    public void testProcessScenario() {
        Response response = orchestrator.processUserRequest("Good morning", "testUser");
        assertNotNull(response);
        assertNotNull(response.getMessage());
        System.out.println("Response: " + response.getMessage());
    }
    
    @Test
    public void testGetSystemStatus() {
        var status = orchestrator.getSystemStatus();
        assertNotNull(status);
        assertFalse(status.isEmpty());
        System.out.println("System Status: " + status);
    }
}
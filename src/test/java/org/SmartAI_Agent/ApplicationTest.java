package org.SmartAI_Agent;

import org.SmartAI_Agent.Config.AppConfig;
import org.SmartAI_Agent.core.MainOrchestrator;
import org.SmartAI_Agent.models.Response;

public class ApplicationTest {
    public static void main(String[] args) {
        System.out.println("Testing SmartHome AI Agent Application...");
        
        try {
            // Initialize the application components
            AppConfig appConfig = new AppConfig();
            appConfig.initialize();
            
            MainOrchestrator orchestrator = appConfig.getMainOrchestrator();
            
            // Test a simple command
            String userId = "testUser";
            String input = "Turn on the lights";
            
            System.out.println("Testing command: " + input);
            Response response = orchestrator.processUserRequest(input, userId);
            System.out.println("Response: " + response.getMessage());
            
            // Test another command
            input = "Set temperature to 70 degrees";
            System.out.println("\nTesting command: " + input);
            response = orchestrator.processUserRequest(input, userId);
            System.out.println("Response: " + response.getMessage());
            
            System.out.println("\nApplication test completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            System.err.println("Error details: " + e.toString());
        }
    }
}
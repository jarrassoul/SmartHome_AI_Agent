package org.SmartAI_Agent;

import java.util.Scanner;

import org.SmartAI_Agent.Config.AppConfig;
import org.SmartAI_Agent.core.MainOrchestrator;
import org.SmartAI_Agent.models.Response;

/**
 * SmartHomeApplication - Main entry point for the Smart Home AI Agent system
 */
public class SmartHomeApplication {
    
    private static AppConfig appConfig;
    private static MainOrchestrator orchestrator;
    
    public static void main(String[] args) {
        System.out.println("Starting Smart Home AI Agent...");
        
        try {
            // Initialize the application components
            AppConfig appConfig = new AppConfig();
            appConfig.initialize();
            
            MainOrchestrator orchestrator = appConfig.getMainOrchestrator();
            
            // Start interactive console
            runInteractiveConsole(orchestrator);
            
        } catch (Exception e) {
            System.err.println("Failed to start Smart Home AI Agent: " + e.getMessage());
            System.err.println("Error details: " + e.toString());
        }
    }
    
    /**
     * Run the interactive console
     */
    private static void runInteractiveConsole(MainOrchestrator orchestrator) {
        String userId = "defaultUser"; // In a real application, this would be dynamic
        
        System.out.println("Welcome to Smart Home AI Agent!");
        System.out.println("Type 'help' for available commands or 'quit' to exit.");
        System.out.println("----------------------------------------");
        
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nYou: ");
                
                // Force flush the output to ensure the prompt is displayed
                System.out.flush();
                
                // Try to read input - handle cases where input might not be available
                String input = null;
                try {
                    input = scanner.nextLine();
                } catch (java.util.NoSuchElementException e) {
                    // This happens when running through Gradle or in non-interactive environments
                    System.out.println("\nNo input available. Exiting interactive mode.");
                    break;
                }
                
                if (input == null) {
                    break; // EOF reached
                }
                
                input = input.trim();
                
                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    System.out.println("Thank you for using Smart Home AI Agent. Goodbye!");
                    break;
                }
                
                if (input.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }
                
                // Only process non-empty input
                if (!input.isEmpty()) {
                    // Check if this is a confirmation response FIRST
                    if (isConfirmationResponse(input)) {
                        try {
                            Response response;
                            if (isPositiveConfirmation(input)) {
                                // Process the confirmed command
                                response = orchestrator.processConfirmedCommand(userId);
                            } else {
                                // Cancel the command
                                response = orchestrator.cancelPendingCommand(userId);
                            }
                            System.out.println("Assistant: " + formatResponse(response));
                        } catch (Exception e) {
                            System.err.println("Assistant: Sorry, I encountered an error processing your confirmation: " + e.getMessage());
                            System.err.println("Error details: " + e.toString());
                        }
                    } else {
                        // Process as regular command
                        try {
                            Response response = orchestrator.processUserRequest(input, userId);
                            System.out.println("Assistant: " + formatResponse(response));
                        } catch (Exception e) {
                            System.err.println("Assistant: Sorry, I encountered an error processing your request: " + e.getMessage());
                            System.err.println("Error details: " + e.toString());
                        }
                }
                }
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                System.out.println("Console interrupted, shutting down...");
                Thread.currentThread().interrupt(); // Restore interrupted status
            } else {
                System.err.println("Error in interactive console: " + e.getMessage());
                System.err.println("Error details: " + e.toString());
            }
        }
    }
    
    /**
     * Format response for display
     */
    private static String formatResponse(Response response) {
        StringBuilder formatted = new StringBuilder();
        
        if (response.getMessage() != null) {
            formatted.append(response.getMessage());
        }
        
        return formatted.toString();
    }
    
    /**
     * Check if input is a confirmation response
     */
    private static boolean isConfirmationResponse(String input) {
        String lowerInput = input.toLowerCase().trim();
        return lowerInput.equals("yes") || lowerInput.equals("no") || 
               lowerInput.equals("1") || lowerInput.equals("2") ||
               lowerInput.contains("confirm") || lowerInput.contains("cancel");
    }
    
    /**
     * Check if confirmation is positive
     */
    private static boolean isPositiveConfirmation(String input) {
        String lowerInput = input.toLowerCase().trim();
        return lowerInput.equals("yes") || lowerInput.equals("1") || 
               lowerInput.contains("confirm");
    }
    
    /**
     * Print help information
     */
    private static void printHelp() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("SMART HOME AI AGENT - AVAILABLE COMMANDS");
        System.out.println("=".repeat(50));
        System.out.println("Basic Commands:");
        System.out.println("  help          - Show this help message");
        System.out.println("  quit/exit     - Exit the application");
        System.out.println("");
        System.out.println("Device Control:");
        System.out.println("  Turn on the lights");
        System.out.println("  Turn off the lights");
        System.out.println("  Set temperature to 22 degrees");
        System.out.println("  Lock all doors");
        System.out.println("  Unlock all doors");
        System.out.println("  Brew coffee");
        System.out.println("  Dim lights to 50 percent");
        System.out.println("");
        System.out.println("Scenarios:");
        System.out.println("  Activate morning routine");
        System.out.println("  Activate night routine");
        System.out.println("  Activate party mode");
        System.out.println("  Activate movie mode");
        System.out.println("  I'm leaving home");
        System.out.println("  I'm arriving home");
        System.out.println("  Emergency");
        System.out.println("=".repeat(50));
    }
}
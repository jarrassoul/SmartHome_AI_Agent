package org.SmartAI_Agent.Config;

import org.SmartAI_Agent.LLM.ClaudeAI;
import org.SmartAI_Agent.LLM.DeepSeekService;
import org.SmartAI_Agent.LLM.OpenAIService;
import org.SmartAI_Agent.agents.ApplianceAgent;
import org.SmartAI_Agent.agents.ClimateAgent;
import org.SmartAI_Agent.agents.SecurityAgent;
import org.SmartAI_Agent.core.IntentAnalyzer;
import org.SmartAI_Agent.core.MainOrchestrator;
import org.SmartAI_Agent.core.ResponseBuilder;
import org.SmartAI_Agent.core.SystemContext;

/**
 * AppConfig - Central configuration class for the SmartHome AI Agent system
 * Initializes all components and manages their dependencies
 */
public class AppConfig {
    
    private IntentAnalyzer intentAnalyzer;
    private ResponseBuilder responseBuilder;
    private SystemContext systemContext;
    private OpenAIService openAIService;
    private ClaudeAI claudeAI;
    private DeepSeekService deepSeekService;
    private SecurityAgent securityAgent;
    private ApplianceAgent applianceAgent;
    private ClimateAgent climateAgent;
    private MainOrchestrator mainOrchestrator;
    
    /**
     * Initialize all system components
     */
    public void initialize() {
        // Initialize core services
        responseBuilder = new ResponseBuilder();
        systemContext = new SystemContext();
        
        // Initialize LLM services
        openAIService = new OpenAIService();
        claudeAI = new ClaudeAI();
        deepSeekService = new DeepSeekService();
        
        // Initialize intent analyzer with AI providers
        intentAnalyzer = new IntentAnalyzer();
        
        // Initialize agents
        securityAgent = new SecurityAgent();
        applianceAgent = new ApplianceAgent();
        climateAgent = new ClimateAgent();
        
        // Initialize main orchestrator
        mainOrchestrator = new MainOrchestrator(
            securityAgent,
            applianceAgent,
            climateAgent,
            intentAnalyzer,
            responseBuilder,
            systemContext
        );
    }
    
    // Getters for all components
    
    public IntentAnalyzer getIntentAnalyzer() {
        return intentAnalyzer;
    }
    
    public ResponseBuilder getResponseBuilder() {
        return responseBuilder;
    }
    
    public SystemContext getSystemContext() {
        return systemContext;
    }
    
    public OpenAIService getOpenAIService() {
        return openAIService;
    }
    
    public ClaudeAI getClaudeAI() {
        return claudeAI;
    }

    public DeepSeekService getDeepSeekService() {
        return deepSeekService;
    }

    public SecurityAgent getSecurityAgent() {
        return securityAgent;
    }
    
    public ApplianceAgent getApplianceAgent() {
        return applianceAgent;
    }
    
    public ClimateAgent getClimateAgent() {
        return climateAgent;
    }
    
    public MainOrchestrator getMainOrchestrator() {
        return mainOrchestrator;
    }
}
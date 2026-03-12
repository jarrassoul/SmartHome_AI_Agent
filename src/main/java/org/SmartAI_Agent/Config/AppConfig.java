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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AppConfig - Central configuration class for the SmartHome AI Agent system
 * Initializes all components and manages their dependencies
 */
@Configuration
public class AppConfig {

    /**
     * Initialize all system components
     */
    @Bean
    public ResponseBuilder responseBuilder() {
        return new ResponseBuilder();
    }

    @Bean
    public SystemContext systemContext() {
        return new SystemContext();
    }

    @Bean
    public OpenAIService openAIService() {
        return new OpenAIService();
    }

    @Bean
    public ClaudeAI claudeAI() {
        return new ClaudeAI();
    }

    @Bean
    public DeepSeekService deepSeekService() {
        return new DeepSeekService();
    }

    @Bean
    public IntentAnalyzer intentAnalyzer() {
        return new IntentAnalyzer();
    }

    @Bean
    public SecurityAgent securityAgent() {
        return new SecurityAgent();
    }

    @Bean
    public ApplianceAgent applianceAgent() {
        return new ApplianceAgent();
    }

    @Bean
    public ClimateAgent climateAgent() {
        return new ClimateAgent();
    }

    @Bean
    public MainOrchestrator mainOrchestrator(
            SecurityAgent securityAgent,
            ApplianceAgent applianceAgent,
            ClimateAgent climateAgent,
            IntentAnalyzer intentAnalyzer,
            ResponseBuilder responseBuilder,
            SystemContext systemContext) {
        return new MainOrchestrator(
            securityAgent,
            applianceAgent,
            climateAgent,
            intentAnalyzer,
            responseBuilder,
            systemContext
        );
    }
}
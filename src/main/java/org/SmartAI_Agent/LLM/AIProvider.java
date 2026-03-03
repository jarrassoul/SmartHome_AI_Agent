package org.SmartAI_Agent.LLM;

/**
 * AIProvider - Abstract base class for AI service providers
 * Defines the interface for integrating with different AI services
 */
public abstract class AIProvider {
    
    protected String providerName;
    protected boolean available;
    protected String apiKey;
    
    /**
     * Constructor
     */
    public AIProvider(String providerName) {
        this.providerName = providerName;
        this.available = false;
    }
    
    /**
     * Analyze text using the AI provider
     */
    public abstract String analyze(String prompt);
    
    /**
     * Check if the AI provider is available/configured
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Set availability status
     */
    protected void setAvailable(boolean available) {
        this.available = available;
    }
    
    /**
     * Get provider name
     */
    public String getProviderName() {
        return providerName;
    }
    
    /**
     * Set API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
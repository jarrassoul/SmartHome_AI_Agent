package com.smarthome.agent.api;

import com.smarthome.agent.model.ChatRequest;
import com.smarthome.agent.model.ChatResponse;
import com.smarthome.agent.model.ConfirmRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    
    @POST("api/chat")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
    
    @POST("api/confirm")
    Call<ChatResponse> confirmAction(@Body ConfirmRequest request);
    
    @GET("api/status")
    Call<Map<String, Object>> getStatus();
}

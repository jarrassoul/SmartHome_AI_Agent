package com.smarthome.agent;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smarthome.agent.adapter.ChatAdapter;
import com.smarthome.agent.api.RetrofitClient;
import com.smarthome.agent.model.ChatRequest;
import com.smarthome.agent.model.ChatResponse;
import com.smarthome.agent.model.ConfirmRequest;
import com.smarthome.agent.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ChatAdapter.OnConfirmationListener {

    private RecyclerView recyclerChat;
    private EditText editMessage;
    private MaterialButton buttonSend;
    private ProgressBar progressBar;
    
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private String userId;
    private boolean waitingForConfirmation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Generate unique user ID for this session
        userId = "android_" + UUID.randomUUID().toString().substring(0, 8);

        initViews();
        setupRecyclerView();
        setupListeners();
        
        // Add welcome message
        addBotMessage("Hello! I'm your SmartHome AI Agent. How can I help you today?");
    }

    private void initViews() {
        recyclerChat = findViewById(R.id.recycler_chat);
        editMessage = findViewById(R.id.edit_message);
        buttonSend = findViewById(R.id.button_send);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Add user message to chat
        addUserMessage(messageText);
        editMessage.setText("");
        
        // Show loading
        showLoading(true);

        // Send to API
        ChatRequest request = new ChatRequest(messageText, userId);
        RetrofitClient.getApiService().sendMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    waitingForConfirmation = chatResponse.isRequiresConfirmation();
                    addBotMessage(chatResponse.getMessage(), chatResponse.isRequiresConfirmation());
                } else {
                    addBotMessage("Sorry, I couldn't process your request. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                showLoading(false);
                addBotMessage("Network error: " + t.getMessage());
            }
        });
    }

    private void addUserMessage(String message) {
        Message userMessage = new Message(message, Message.TYPE_USER);
        chatAdapter.addMessage(userMessage);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        addBotMessage(message, false);
    }

    private void addBotMessage(String message, boolean requiresConfirmation) {
        Message botMessage = new Message(message, Message.TYPE_BOT, requiresConfirmation);
        chatAdapter.addMessage(botMessage);
        scrollToBottom();
    }

    private void scrollToBottom() {
        recyclerChat.post(() -> {
            if (chatAdapter.getItemCount() > 0) {
                recyclerChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSend.setEnabled(!show);
    }

    @Override
    public void onConfirm() {
        if (!waitingForConfirmation) return;
        
        showLoading(true);
        ConfirmRequest request = new ConfirmRequest("confirm", userId);
        RetrofitClient.getApiService().confirmAction(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                showLoading(false);
                waitingForConfirmation = false;
                if (response.isSuccessful() && response.body() != null) {
                    addBotMessage(response.body().getMessage());
                } else {
                    addBotMessage("Failed to confirm action.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                showLoading(false);
                addBotMessage("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onCancel() {
        if (!waitingForConfirmation) return;
        
        showLoading(true);
        ConfirmRequest request = new ConfirmRequest("cancel", userId);
        RetrofitClient.getApiService().confirmAction(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                showLoading(false);
                waitingForConfirmation = false;
                if (response.isSuccessful() && response.body() != null) {
                    addBotMessage(response.body().getMessage());
                } else {
                    addBotMessage("Action cancelled.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                showLoading(false);
                addBotMessage("Network error: " + t.getMessage());
            }
        });
    }
}

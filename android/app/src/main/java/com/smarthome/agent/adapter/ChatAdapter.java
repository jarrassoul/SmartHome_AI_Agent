package com.smarthome.agent.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarthome.agent.R;
import com.smarthome.agent.model.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private OnConfirmationListener confirmationListener;

    public interface OnConfirmationListener {
        void onConfirm();
        void onCancel();
    }

    public ChatAdapter(List<Message> messages, OnConfirmationListener listener) {
        this.messages = messages;
        this.confirmationListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_bot, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getItemViewType() == Message.TYPE_USER) {
            ((UserMessageViewHolder) holder).bind(message);
        } else {
            ((BotMessageViewHolder) holder).bind(message, confirmationListener);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void updateLastMessage(Message message) {
        if (!messages.isEmpty()) {
            messages.set(messages.size() - 1, message);
            notifyItemChanged(messages.size() - 1);
        }
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
        }

        void bind(Message message) {
            messageText.setText(message.getContent());
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        View confirmButton;
        View cancelButton;
        View confirmationLayout;

        BotMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
            confirmButton = itemView.findViewById(R.id.button_confirm);
            cancelButton = itemView.findViewById(R.id.button_cancel);
            confirmationLayout = itemView.findViewById(R.id.layout_confirmation);
        }

        void bind(Message message, OnConfirmationListener listener) {
            messageText.setText(message.getContent());
            
            if (message.isRequiresConfirmation() && confirmationLayout != null) {
                confirmationLayout.setVisibility(View.VISIBLE);
                confirmButton.setOnClickListener(v -> {
                    if (listener != null) listener.onConfirm();
                    confirmationLayout.setVisibility(View.GONE);
                });
                cancelButton.setOnClickListener(v -> {
                    if (listener != null) listener.onCancel();
                    confirmationLayout.setVisibility(View.GONE);
                });
            } else if (confirmationLayout != null) {
                confirmationLayout.setVisibility(View.GONE);
            }
        }
    }
}

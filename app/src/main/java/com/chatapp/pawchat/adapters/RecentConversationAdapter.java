package com.chatapp.pawchat.adapters;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatapp.pawchat.databinding.ItemContainerRecentConversationBinding;
import com.chatapp.pawchat.listeners.ConversionListener;
import com.chatapp.pawchat.models.ChatMessage;
import com.chatapp.pawchat.models.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener){
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(ItemContainerRecentConversationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversationAdapter.ConversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversationBinding binding;
        ConversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConverstionBinding){
            super(itemContainerRecentConverstionBinding.getRoot());
            binding = itemContainerRecentConverstionBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecent.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListener.onConversionClicked(user);
            });
        }
    }
    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

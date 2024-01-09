package com.chatapp.pawchat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.chatapp.pawchat.adapters.ChatAdapter;
import com.chatapp.pawchat.databinding.ActivityChatBinding;
import com.chatapp.pawchat.models.ChatMessage;
import com.chatapp.pawchat.models.User;
import com.chatapp.pawchat.ultilities.Constants;
import com.chatapp.pawchat.ultilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String convertionId = null;
    private Boolean isReceiverAvailabel = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListListener();
        loadReceiverDetails();
        init();
        listenMessages();
    }
    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(getBitmapFromEncodedString(receiverUser.image), preferenceManager.getString(Constants.KEY_USER_ID), chatMessages);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_STAMPTIME, new Date());

        // Add the message to the chat collection
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        // Check if there is an existing conversation
        if (convertionId != null) {
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            // Create a new conversation
            HashMap<String, Object> convertion = new HashMap<>();
            convertion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            convertion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            convertion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            convertion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            convertion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            convertion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            convertion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            convertion.put(Constants.KEY_STAMPTIME, new Date());
            addConversation(convertion);
            // Add the conversation to the conversations collection
        }
        binding.inputMessage.setText(null);
    }
    private void listenAvailavilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects. requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                    isReceiverAvailabel = availability == 1;
                }
            }
            if (isReceiverAvailabel){
                binding.textAvailability.setVisibility(View.VISIBLE);
            }
            else {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
    }
    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id).addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id).whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null){
            return;
        } if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_STAMPTIME));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_STAMPTIME);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progessBar.setVisibility(View.GONE);
        if (convertionId == null){
            checkForConvertion();
        }
    });
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        assert receiverUser != null;
        binding.textName.setText(receiverUser.name);
    }
    private void setListListener(){
        binding.imgSend.setOnClickListener(v -> sendMessage());
        binding.imgBack.setOnClickListener(v -> onBackPressed());
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void addConversation(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).add(conversion).addOnSuccessListener(documentReference -> convertionId = documentReference.getId());
    }
    private void updateConversion(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(convertionId);

        // Use a HashMap to provide alternating field names and values
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_LAST_MESSAGE, message);
        updates.put(Constants.KEY_STAMPTIME, new Date());

        // Update the document with the HashMap
        documentReference.update(updates);
    }

    private void checkForConvertion(){
        if (chatMessages.size() != 0){
            checkForConvertionRemotelyRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receiverUser.id);
        };
        checkForConvertionRemotelyRemotely(receiverUser.id, preferenceManager.getString(Constants.KEY_USER_ID));
    }
    private void checkForConvertionRemotelyRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(Constants.KEY_SENDER_ID, senderId).whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId).get().addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            convertionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailavilityOfReceiver();
    }
}
package com.chatapp.pawchat.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Firebase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token){
        super.onNewToken(token);



//        Log.d("FCM","Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);

//        //Checking state of msg sent or not
//        Log.d("FCM","Message: " + remoteMessage.getNotification().getBody());
    }
}

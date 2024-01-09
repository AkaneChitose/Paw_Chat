package com.chatapp.pawchat.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.chatapp.pawchat.R;
import com.chatapp.pawchat.activities.ChatActivity;
import com.chatapp.pawchat.models.User;
import com.chatapp.pawchat.ultilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;


public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        User user = new User();

        user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
        user.name = remoteMessage.getData().get(Constants.KEY_NAME);
        user.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

        int notificationId = new Random().nextInt();
        String channelId = "Chat_message";

        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder Builder = new NotificationCompat.Builder(this, channelId);
        Builder.setSmallIcon(R.drawable.ic_notification);
        Builder.setContentTitle(user.name);
        Builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
        Builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(Constants.KEY_MESSAGE)));
        Builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Builder.setContentIntent(pendingIntent);
        Builder.setAutoCancel(true);

        CharSequence channelName = "Chat Message";
        String channelDescription = "abcacbacbcabc";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelDescription);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        notificationManagerCompat.notify(notificationId, Builder.build());
    }
}

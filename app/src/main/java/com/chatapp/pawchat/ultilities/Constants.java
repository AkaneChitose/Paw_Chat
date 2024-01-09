package com.chatapp.pawchat.ultilities;

import java.util.HashMap;

public class Constants {

    //THIS ONE USE IN LOGIN - SIGNIN - AUTH SESSION
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID= "userId";
    public static final String KEY_IMAGE= "image";

    //THIS ONE FOR LOGOUT
    public static final String KEY_FCM_TOKEN= "fcmToken";

    //THIS ONE FOR USER HANDLE DETAILS FUNTION
    public static final String KEY_USER = "user";

    //THIS ONE FOR HANDLE CHAT FUNCTION
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_STAMPTIME = "timestamp";

    //THIS ONE FOR RECENT CONVERSATION LIST HANDLE
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";

    //FOR HANDLE STATUS ONLINE IS AVAILABLE OR NOT
    public static final String KEY_AVAILABILITY = "availability";

    //FOR HANDLE NOTIFICATION
    public static final String REMOTE_MSG_AUTH = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static  HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if (remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTH, "key=AAAAxZsq1GE:APA91bEIL1ZfkyXG1wja_XxtW_wLGImL70BSb8VQL4rK6ztMGVoe8Tq9k_R4LbGSZhsFLtKeP--_ewebSXJQhqBzNoOhWt459PuL0lHXHmJvBYQsXidvH6gj6-ODJdlRCLKIad-4WvaI");
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE, "application/json");
        }
        return remoteMsgHeaders;
    }

}

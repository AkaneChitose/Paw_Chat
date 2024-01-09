package com.chatapp.pawchat.models;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    public String senderId, receiverId, message, dateTime;
    public Date dateObject;
    public String conversionId, conversionName, conversionImage;
}

package com.chatapp.pawchat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chatapp.pawchat.adapters.ChatAdapter;
import com.chatapp.pawchat.databinding.ActivityChatBinding;
import com.chatapp.pawchat.models.ChatMessage;
import com.chatapp.pawchat.models.User;
import com.chatapp.pawchat.network.ApiClient;
import com.chatapp.pawchat.network.ApiService;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Khởi tạo và thiết lập giao diện người dùng
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập nghe sự kiện cho danh sách và load thông tin người nhận
        setListListener();
        loadReceiverDetails();

        // Khởi tạo các thành phần cần thiết
        init();

        // Lắng nghe tin nhắn
        listenMessages();
    }

    // Phương thức khởi tạo các thành phần
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID),
                chatMessages
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        // Tạo một HashMap để lưu trữ chi tiết của tin nhắn
        // Phương thức này tạo một HashMap để lưu trữ chi tiết của tin nhắn, bao gồm ID của người gửi, ID của người nhận, nội dung tin nhắn và thời điểm.
        HashMap<String, Object> message = new HashMap<>();

        // Thêm ID của người gửi và người nhận, nội dung tin nhắn và timestamp vào HashMap tin nhắn
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_STAMPTIME, new Date());

        // Thêm tin nhắn vào file "chat" trong cơ sở dữ liệu
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        // Kiểm tra xem có cuộc trò chuyện tồn tại không
        if (convertionId != null) {
            // Nếu có cuộc trò chuyện, cập nhật nó với tin nhắn mới
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            // Nếu không có cuộc trò chuyện tồn tại, tạo một HashMap cuộc trò chuyện mới
            HashMap<String, Object> convertion = new HashMap<>();
            convertion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            convertion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            convertion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            convertion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            convertion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            convertion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            convertion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            convertion.put(Constants.KEY_STAMPTIME, new Date());

            // Thêm cuộc trò chuyện mới vào cuộc trò chuyện trong cơ sở dữ liệu
            addConversation(convertion);
        }

        // Kiểm tra xem người nhận có Online
        if (!isReceiverAvailabel){
            try {
                // Tạo mảng và đối tượng JSON cho thông báo FCM
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                // Tạo một đối tượng JSON cho nội dung thông báo
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                // Gửi thông báo FCM
                sendNotification(body.toString());
            } catch (Exception exception){
                // Xử lý các ngoại lệ
                showToast(exception.getMessage());
            }
        }

        // Xóa trường nhập sau khi gửi tin nhắn
        binding.inputMessage.setText(null);
    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        // Tạo một yêu cầu Retrofit thông qua API service
        ApiClient.getClient().create(ApiService.class)
                .sendMessage(Constants.getRemoteMsgHeaders(), messageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        // Xử lý phản hồi khi yêu cầu thành công
                        if (response.isSuccessful()) {
                            try {
                                // Kiểm tra xem phản hồi có dữ liệu không
                                if (response.body() != null) {
                                    // Parse JSON từ dữ liệu phản hồi
                                    JSONObject responseJson = new JSONObject(response.body());
                                    JSONArray results = responseJson.getJSONArray("results");

                                    // Kiểm tra nếu có lỗi trong phản hồi
                                    if (responseJson.getInt("failure") == 1) {
                                        JSONObject error = (JSONObject) results.get(0);
                                        showToast(error.getString("error"));
                                        return;
                                    }
                                }
                            } catch (JSONException e) {
                                // Xử lý ngoại lệ khi có lỗi parse JSON
                                e.printStackTrace();
                            }
                            // Hiển thị thông báo khi thông báo được gửi thành công
                            showToast("Notification sent successfully");
                        } else {
                            // Hiển thị thông báo lỗi khi yêu cầu không thành công
                            showToast("Error:" + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        // Xử lý khi yêu cầu thất bại
                        showToast(t.getMessage());
                    }
                });
    }

    private void listenAvailavilityOfReceiver(){
        // Lắng nghe sự kiện thay đổi của tài khoản người nhận trong bảng Users
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    // Kiểm tra lỗi
                    if (error != null){
                        return;
                    }

                    // Kiểm tra giá trị trả về từ cơ sở dữ liệu
                    if (value != null){
                        // Lấy thông tin về sự khả dụng từ giá trị trả về
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isReceiverAvailabel = availability == 1;
                        }

                        // Cập nhật token và hình ảnh của người nhận nếu chưa được cập nhật
                        receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                        if (receiverUser.image == null){
                            receiverUser.image = value.getString(Constants.KEY_IMAGE);
                            // Cập nhật hình ảnh người nhận trong Adapter
                            chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                            chatAdapter.notifyItemRangeInserted(0, chatMessages.size());
                        }
                    }

                    // Hiển thị trạng thái khả dụng của người nhận trên giao diện người dùng
                    if (isReceiverAvailabel){
                        binding.textAvailability.setVisibility(View.VISIBLE);
                    } else {
                        binding.textAvailability.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Chuyển đổi một chuỗi ảnh đã mã hóa sang đối tượng Bitmap.
     *
     * @param encodedImage Chuỗi ảnh đã mã hóa
     * @return Đối tượng Bitmap được tạo từ chuỗi ảnh đã mã hóa
     */
    private Bitmap getBitmapFromEncodedString(String encodedImage){
        // Kiểm tra xem chuỗi ảnh đã mã hóa có giá trị không
        if (encodedImage != null){
            // Giải mã chuỗi ảnh và chuyển đổi thành mảng byte
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            // Chuyển đổi mảng byte thành đối tượng Bitmap
            return BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
        } else {
            // Trả về null nếu chuỗi ảnh đã mã hóa là null
            return null;
        }
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
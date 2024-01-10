package com.chatapp.pawchat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chatapp.pawchat.ultilities.Constants;
import com.chatapp.pawchat.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo PreferenceManager để quản lý dữ liệu người dùng
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

        // Lấy đối tượng Firestore để tương tác với cơ sở dữ liệu Firebase
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Xây dựng đối tượng DocumentReference để tham chiếu đến tài liệu người dùng cụ thể trong cơ sở dữ liệu
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Khi Activity tạm dừng, cập nhật trạng thái "không khả dụng" (0) cho người dùng trong cơ sở dữ liệu
        documentReference.update(Constants.KEY_AVAILABILITY, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Khi Activity được khôi phục, cập nhật trạng thái "khả dụng" (1) cho người dùng trong cơ sở dữ liệu
        documentReference.update(Constants.KEY_AVAILABILITY, 1);
    }
}

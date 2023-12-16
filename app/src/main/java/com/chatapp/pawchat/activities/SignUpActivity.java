package com.chatapp.pawchat.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.chatapp.pawchat.R;
import com.chatapp.pawchat.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners(){
        binding.txtSignIn.setOnClickListener(v -> onBackPressed());
    }
}
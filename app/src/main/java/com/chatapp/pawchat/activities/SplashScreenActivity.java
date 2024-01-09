package com.chatapp.pawchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatapp.pawchat.R;

public class SplashScreenActivity extends AppCompatActivity {
    private TextView name, slogan;
    private ImageView logo;
    private View topView1, topView2, topView3, bottomView1, bottomView2, bottomView3;
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);

        name = findViewById(R.id.appName);
        slogan = findViewById(R.id.slogan);
        logo = findViewById(R.id.appLogo);

        topView1 = findViewById(R.id.topView1);
        topView2 = findViewById(R.id.topView2);
        topView3 = findViewById(R.id.topView3);

        bottomView1 = findViewById(R.id.bottomView1);
        bottomView2 = findViewById(R.id.bottomView2);
        bottomView3 = findViewById(R.id.bottomView3);

        // Store a reference to the context
        final AppCompatActivity context = this;


        Animation topView1Animation = AnimationUtils.loadAnimation(context, R.anim.top_view_anim);
        Animation topView2Animation = AnimationUtils.loadAnimation(context, R.anim.top_view_anim);
        Animation topView3Animation = AnimationUtils.loadAnimation(context, R.anim.top_view_anim);

        Animation bottomView1Animation = AnimationUtils.loadAnimation(context, R.anim.bottom_view_anim);
        Animation bottomView2Animation = AnimationUtils.loadAnimation(context, R.anim.bottom_view_anim);
        Animation bottomView3Animation = AnimationUtils.loadAnimation(context, R.anim.bottom_view_anim);

        topView1.startAnimation(topView1Animation);
        bottomView1.startAnimation(bottomView1Animation);

        topView1Animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                topView2.setVisibility(View.VISIBLE);
                bottomView2.setVisibility(View.VISIBLE);

                topView2.startAnimation(topView2Animation);
                bottomView2.startAnimation(bottomView2Animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        topView2Animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                topView3.setVisibility(View.VISIBLE);
                bottomView3.setVisibility(View.VISIBLE);

                topView3.startAnimation(topView3Animation);
                bottomView3.startAnimation(bottomView3Animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        topView3Animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Ensure the logo animation is only triggered once
                if (logo.getVisibility() != View.VISIBLE) {
                    logo.setVisibility(View.VISIBLE);
                    logo.startAnimation(AnimationUtils.loadAnimation(context, R.anim.zoom));
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });


        Animation logoAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom);
        Animation nameAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom);
        Animation sloganAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom);

        logo.setVisibility(View.VISIBLE);
        logo.startAnimation(logoAnimation);

        logoAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Logo animation finished, start appName animation
                name.setVisibility(View.VISIBLE);
                name.startAnimation(nameAnimation);

                nameAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // appName animation finished, start slogan animation
                        slogan.setVisibility(View.VISIBLE);
                        final String animateTxt = slogan.getText().toString();
                        slogan.setText("");
                        count = 0;
                        new CountDownTimer(animateTxt.length() * 100, 100) {

                            @Override
                            public void onTick(long millisUntilFinished) {
                                slogan.setText(slogan.getText().toString() + animateTxt.charAt(count));
                                count++;
                            }

                            @Override
                            public void onFinish() {

                            }
                        }.start();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Delay the start of MainActivity by 3000 milliseconds (3 seconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start MainActivity
                Intent intent = new Intent(context, SignInActivity.class);
                startActivity(intent);
                finish(); // Close WelcomeActivity
            }
        }, 4000);
    }
}
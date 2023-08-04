package com.example.mysignupapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/*SplashScreenActivity is used to play an animated intro of our app
Code: When the activity is created the intro
will be displayed for 4 seconds (line 29 shows time in milliseconds)
 */
public class SplashScreenActivity extends AppCompatActivity
{

    private FirebaseAuth mAuth; //Reference to the Firebase connected to the project
    private FirebaseUser currentUser; //the running Firebase user of the app

    Animation right_animation;
    Animation left_animation;

    ImageView e_letter;
    ImageView mart_text;
    ImageView change_text;
    ImageView arket_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        right_animation = AnimationUtils.loadAnimation(this, R.anim.animate_right);
        left_animation = AnimationUtils.loadAnimation(this, R.anim.animate_left);

        e_letter = (ImageView) findViewById(R.id.e_letter);
        mart_text = (ImageView) findViewById(R.id.mart_text);
        change_text = (ImageView) findViewById(R.id.change_text);
        arket_text = (ImageView) findViewById(R.id.arket_text);

        e_letter.setAnimation(right_animation);
        mart_text.setAnimation(left_animation);
        change_text.setAnimation(left_animation);
        arket_text.setAnimation(left_animation);


        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mAuth = FirebaseAuth.getInstance();
                currentUser = mAuth.getCurrentUser();

                if(currentUser != null)
                {
                    Intent login_intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                    startActivity(login_intent);
                    finish();
                }
                else
                {
                    // We create and start a new Intent from splash activity to MainActivity
                    // MainActivity : Sign in/ Login screen
                    Intent login_intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(login_intent);
                    finish();
                }

            }
        },5000);

    }

    
}
package com.example.mimanhwa.Activities.LoginActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mimanhwa.R;

public class IntroActivity extends AppCompatActivity {

    //Intent
    private Intent intent;

    //view item
    private Button inButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //init view item
        inButton = findViewById(R.id.getInBtn);

        //click listener in button
        inButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroActivity.this,LoginActivity.class));
                finish();
            }
        });
    }
}
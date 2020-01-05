package com.example.emailapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DisplayMail extends AppCompatActivity {

    TextView tv_sentby;
    TextView tv_sub;
    TextView tv_message;
    TextView tv_createdAt;
    Button bt_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_mail);

        setTitle("Display Mail");

        tv_sentby = findViewById(R.id.tv_sentby);
        tv_sub = findViewById(R.id.tv_sub);
        tv_message = findViewById(R.id.tv_message);
        tv_createdAt = findViewById(R.id.tv_createdAt);
        bt_close = findViewById(R.id.bt_close);

        if(getIntent().getExtras()!=null){
            Chat chat = (Chat)getIntent().getExtras().getSerializable(RecyclerChatAdapter.CHAT_KEY);
            tv_sentby.setText(chat.sender_fname);
            tv_sub.setText(chat.subject);
            tv_message.setText(chat.message);
            tv_createdAt.setText(chat.created_at);
        }

        bt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

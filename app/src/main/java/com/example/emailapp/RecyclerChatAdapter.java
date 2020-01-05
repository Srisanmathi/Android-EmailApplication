package com.example.emailapp;

import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerChatAdapter extends RecyclerView.Adapter<RecyclerChatAdapter.ViewHolder>{

    ArrayList<Chat> sData;
    static String CHAT_KEY = "chat";

    public RecyclerChatAdapter(ArrayList<Chat> sData) {
        this.sData = sData;
    }

    @NonNull
    @Override
    public RecyclerChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_recycler_chat_adapter, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerChatAdapter.ViewHolder holder, int position) {
        Chat s = sData.get(position);
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//        try {
//            Date date = format.parse(s.updated_time);
//            String required_format  = (String) DateFormat.format("MM-dd-yyyy", date);
//            s.updated_time = required_format;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }Toast.makeText(getClass().this, "", Toast.LENGTH_SHORT).show();
        Log.d("fruit",holder.tv_subject.getLineCount()+"");
        holder. tv_subject.setText(s.getSubject());
        holder.tv_date.setText(s.created_at);

        holder.chat = s;


    }

    @Override
    public int getItemCount() {
        return sData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_subject;
        TextView tv_date;
        Chat chat;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tv_subject = itemView.findViewById(R.id.tv_subject);
            tv_date = itemView.findViewById(R.id.tv_date);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(itemView.getContext(), DisplayMail.class);
                    i.putExtra(CHAT_KEY,chat);
                    v.getContext().startActivity(i);


                }
            });
        }
    }
}


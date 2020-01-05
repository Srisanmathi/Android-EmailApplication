package com.example.emailapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Inbox extends AppCompatActivity {

    ImageView iv_newemail;
    ImageView iv_logout;
    TextView tv_name;
    RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    String firstname;
    String lastname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        setTitle("Inbox");

        iv_newemail = findViewById(R.id.iv_newemail);
        iv_logout = findViewById(R.id.iv_logout);
        tv_name =findViewById(R.id.tv_name);
        recyclerView = findViewById(R.id.recyclerView);

        SharedPreferences m = getSharedPreferences("user",MODE_PRIVATE);
        firstname = m.getString("firstname","0");
        lastname = m.getString("lastname","0");
        tv_name.setText(firstname+ " "+lastname);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(isConnected()){
            new GetChatAsync().execute("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/inbox");
        }else{
            Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
        }

        iv_newemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Inbox.this,NewEmail.class);
                startActivity(i);
            }
        });
        iv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Inbox.this,MainActivity.class);
                startActivity(i);
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    private class GetChatAsync extends AsyncTask<String, Void, String> {
        private final OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences m = getSharedPreferences("user",MODE_PRIVATE);
            String token = m.getString("token",null);
            if(token == null){
                Log.d("report","token is not found");
            }

            try {

                Request request = new Request.Builder()
                        .url(params[0])
                        .addHeader("Authorization", "BEARER " + token)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    return response.body().string();

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                ArrayList<Chat> r = new ArrayList<>();
                try{
                    JSONObject root = new JSONObject(result);
                    JSONArray messages =   root.getJSONArray("messages");

                    for(int i=0; i<messages.length(); i++ ){
                        JSONObject message = messages.getJSONObject(i);

                        Chat u = new Chat();
                        u.sender_fname = message.getString("sender_fname");
                        u.sender_lname = message.getString("sender_lname");
                        u.id = message.getString("id");
                        u.sender_id = message.getString("sender_id");
                        u.receiver_id = message.getString("receiver_id");
                        u.message = message.getString("message");
                        u.subject = message.getString("subject");
                        u.created_at = message.getString("created_at");
                        u.updated_at = message.getString("updated_at");
                        r.add(u);


                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                mAdapter = new RecyclerChatAdapter(r);
                recyclerView.setAdapter(mAdapter);

            } else {
                Toast.makeText(Inbox.this, "No messages to show or there is a connection problem", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

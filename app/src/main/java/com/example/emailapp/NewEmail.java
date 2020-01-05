package com.example.emailapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewEmail extends AppCompatActivity {

    TextView et_subject;
    Spinner spinner;
    EditText et_message;
    Button bt_send;
    Button bt_cancel1;
    String subject;
    String message;
    String receiver_id;
    ArrayList<String> res = new ArrayList<>();
    ArrayList<String> res_ids = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_email);
        setTitle("Create New Email");

        spinner = findViewById(R.id.spinner);
        et_subject = findViewById(R.id.et_subject);
        et_message = findViewById(R.id.et_message);
        bt_send = findViewById(R.id.bt_send);
        bt_cancel1 = findViewById(R.id.bt_cancel1);

        if(isConnected()){
            new GetUsersAsync().execute("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/users");

        }
        else{
            Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                receiver_id = res_ids.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    subject = et_subject.getText().toString();
                    message = et_message.getText().toString();
                    new SendEmailAsync().execute("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/inbox/add");

                }
                else{
                    Toast.makeText(NewEmail.this, "Not connected to internet", Toast.LENGTH_SHORT).show();
                }
                //new SendEmailAsync().execute("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/inbox/add");
            }
        });

        bt_cancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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


//To load spinner
    private class GetUsersAsync extends AsyncTask<String, Void, String> {
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

                try{
                JSONObject root = new JSONObject(result);
                JSONArray users =   root.getJSONArray("users");
                for(int i=0; i<users.length(); i++ ){
                    JSONObject user = users.getJSONObject(i);
                    Users u = new Users();
                    u.fname = user.getString("fname");
                    u.lname = user.getString("lname");
                    u.id = user.getString("id");
                    res.add(u.fname + " " + u.lname);
                    res_ids.add(u.id);

                }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(NewEmail.this,
                            android.R.layout.simple_spinner_item, res);
                    spinner.setAdapter(adapter);
                    }
                catch (JSONException e){
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(NewEmail.this, "No users available ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //To send email
    private class SendEmailAsync extends AsyncTask<String, Void, String> {
        private final OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences m = getSharedPreferences("user",MODE_PRIVATE);
            String token = m.getString("token",null);
            if(token == null){
                Log.d("report","token is not found");
            }

            try {

                RequestBody formBody = new FormBody.Builder()
                        .add("receiver_id", receiver_id)
                        .add("subject", subject)
                        .add("message", message)
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .addHeader("Authorization", "BEARER " + token)
                        .post(formBody)
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
            Toast.makeText(NewEmail.this, "Message has been sent!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

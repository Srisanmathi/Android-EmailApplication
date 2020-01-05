package com.example.emailapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText et_email;
    EditText et_password;
    Button bt_login;
    Button bt_signup;
    String email;
    String password;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Mailer");

        et_email = findViewById(R.id.EmailMain);
        et_password = findViewById(R.id.et_password);
        bt_login = findViewById(R.id.bt_login);
        bt_signup = findViewById(R.id.btsignMain);

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnected()){
                    Toast.makeText(MainActivity.this, "Not Connected to internet", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(et_email.equals("")){
                        et_email.setError("Enter your email");
                        Toast.makeText(MainActivity.this, "enter email", Toast.LENGTH_SHORT).show();
                    }
                    else if(et_password.equals("")){
                        et_password.setError("Enter password");
                        Toast.makeText(MainActivity.this, "enter password", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        email = et_email.getText().toString();
                        password = et_password.getText().toString();
                        new GetLoginAsync().execute("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/login");
                    }

                }
            }
        });

        bt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SignUp.class);
                startActivity(i);
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

    private class GetLoginAsync extends AsyncTask<String, Void, String> {
        private final OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {

            try {
                RequestBody formBody = new FormBody.Builder()
                        .add("email", email)
                        .add("password", password)
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(formBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    handler.post( new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "hello" , Toast.LENGTH_LONG).show();
                        }
                    });
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

                Gson g = new Gson();
                //Converting json string to a UserToken object
                UserToken user = g.fromJson(result, UserToken.class);

                SharedPreferences m = getSharedPreferences("user",MODE_PRIVATE);
                SharedPreferences.Editor mEditor = m.edit();
                mEditor.putString("token",user.token);
                mEditor.putString("firstname",user.user_fname);
                mEditor.putString("lastname",user.user_lname);
                mEditor.apply();

                //Toast.makeText(MainActivity.this,  " Successfully logged in!", Toast.LENGTH_SHORT).show();
                finish();
                Intent i = new Intent(MainActivity.this, Inbox.class);
                startActivity(i);

            } else {
                Toast.makeText(MainActivity.this, "Error logging in. Try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

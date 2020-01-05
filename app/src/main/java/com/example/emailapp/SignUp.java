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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUp extends AppCompatActivity {

    EditText et_fname;
    EditText et_lname;
    EditText et_email;
    EditText et_choosepass;
    EditText et_confirmpass;
    Button bt_signup;
    Button bt_cancel;
    String email;
    String password;
    String fname;
    String lname;
    Handler handler = new Handler();
    String message ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");

        et_fname = findViewById(R.id.et_fname);
        et_lname = findViewById(R.id.et_lname);
        et_email = findViewById(R.id.Et_Email);
        et_choosepass = findViewById(R.id.et_choosepass);
        et_confirmpass = findViewById(R.id.et_confirmpass);
        bt_signup =findViewById(R.id.Bt_Signup);
        bt_cancel = findViewById(R.id.bt_cancel1);



        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignUp.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        bt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnected()){
                    Toast.makeText(SignUp.this, "Not Connected to internet", Toast.LENGTH_SHORT).show();
                }
                else if(et_fname.equals("")){
                        et_fname.setError("Enter your firstname");
                    }
                else if(et_lname.equals("")){
                        et_lname.setError("Enter your lastname");
                    }
                else if(et_email.equals("") || !android.util.Patterns.EMAIL_ADDRESS.matcher(et_email.getText().toString().trim()).matches()){
                        et_email.setError("Enter a valid email");
                    }
                else if(et_choosepass.equals("")){
                        et_choosepass.setError("Enter a password");
                    }
                else if(et_confirmpass.equals("")){
                        et_confirmpass.setError("Confirm password");
                    }
                else if(!et_choosepass.getText().toString().equals(et_confirmpass.getText().toString())) {
                        et_confirmpass.setError("Password does not match");
                    }
                else{
                        fname = et_fname.getText().toString();
                        lname = et_lname.getText().toString();
                        email = et_email.getText().toString();
                        password = et_choosepass.getText().toString();
                        new GetSignUpAsync().execute("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/signup");
                    }
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

    private class GetSignUpAsync extends AsyncTask<String, Void, String> {

        private final OkHttpClient client = new OkHttpClient();


        @Override
        protected String doInBackground(String... params) {


                RequestBody formBody = new FormBody.Builder()
                        .add("email", email)
                        .add("password", password)
                        .add("fname", fname)
                        .add("lname", lname)
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(formBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        
                        try {
                            String json = response.body().string();
                            JSONObject root = new JSONObject(json);
                            message = root.getString("message");

                            handler.post( new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                                }
                            });
                            //Toast.makeText(getApplicationContext(), ""+message, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        throw new IOException("Unexpected code " + response);
                    }
                    return response.body().string();

                }

            catch (MalformedURLException e) {
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
                UserToken user = g.fromJson(result, UserToken.class);
                SharedPreferences m = getSharedPreferences("user",MODE_PRIVATE);
                SharedPreferences.Editor mEditor = m.edit();
                mEditor.putString("token",user.token);
                mEditor.putString("firstname",user.user_fname);
                mEditor.putString("lastname",user.user_lname);
                mEditor.apply();

                Toast.makeText(SignUp.this, user.user_fname + " is successfully created!", Toast.LENGTH_SHORT).show();

                finish();
                Intent i = new Intent(SignUp.this, Inbox.class);
                startActivity(i);

            } else {
                Toast.makeText(SignUp.this, "Error signing up. Try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

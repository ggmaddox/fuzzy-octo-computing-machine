package com.example.fabflix;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Gets email and password
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void connectToTomcat(final View view) {
        // Happens on click to email_login_button

        // no user is logged in, so we must connect to the server

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // 10.0.2.2 is the host machine when running the android emulator
//        final StringRequest afterLoginRequest = new StringRequest(Request.Method.GET, "https://10.0.2.2:8443/fabflix/api/login",
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//                        Log.d("username.reponse", response);
//                        ((TextView) findViewById(R.id.http_response)).setText(response);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // error
//                        Log.d("username.error", error.toString());
//                    }
//                }
//        );

        final StringRequest loginRequest = new StringRequest(Request.Method.POST, "https://192.168.1.16:8443/fabflix/api/login",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("login.success", response);
                        try
                        {
                            JSONObject json_response = new JSONObject(response);
                            Log.d("JSON response:", json_response.toString());
                            if (json_response.getString("status").compareToIgnoreCase("success") == 0)
                            {
                                goToIndex(view);
                            }
                            else
                            {
                                ((TextView) findViewById(R.id.error_message)).setText(json_response.getString("message"));
                            }
                        }
                        catch (JSONException j_e)
                        {
                            j_e.printStackTrace();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                       // ((TextView) findViewById(R.id.http_response)).setText(response);
                        // Add the request to the RequestQueue.
                        //queue.add(afterLoginRequest);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                        ((TextView) findViewById(R.id.error_message)).setText(error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<String, String>();
                // This should send the email and password to the login servlet
                params.put("username", mEmailView.getText().toString());
                params.put("password", mPasswordView.getText().toString());

                return params;
            }
        };

        // !important: queue.add is where the login request is actually sent
        queue.add(loginRequest);

    }

    public void goToIndex(View view) {
        //String msg = ((EditText) findViewById(R.id.red_2_blue_message)).getText().toString();

        Intent goToIntent = new Intent(this, IndexActivity.class);

        //goToIntent.putExtra("last_activity", "red");
        //goToIntent.putExtra("message", msg);

        startActivity(goToIntent);
    }


}


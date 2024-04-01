package com.moutamid.readnumberplates;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fxn.stash.Stash;
import com.moutamid.readnumberplates.databinding.ActivityLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        binding.login.setOnClickListener(v -> {
            if (valid())
                postFormData();
        });

    }

    private boolean valid() {
        if (binding.username.getEditText().getText().toString().isEmpty()){
            Toast.makeText(this, "Username is empty", Toast.LENGTH_SHORT).show();
            return false;
        } if (binding.password.getEditText().getText().toString().isEmpty()){
            Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private static final String TAG = "LoginActivity";
    private void postFormData() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.login,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        runOnUiThread(progressDialog::dismiss);
                        Log.d(TAG, "onResponse: " + response);
                        String token = "";
                        try {
                            JSONObject object = new JSONObject(response);
                            token = object.getString("JWT");
                            Log.d(TAG, "token: " + token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        UserModel userModel = new UserModel();
                        userModel.name = binding.username.getEditText().getText().toString();
                        userModel.password = binding.password.getEditText().getText().toString();
                        userModel.token = token;
                        Stash.put(Constants.USER, userModel);
                        startActivity(new Intent(LoginActivity.this, OcrActivity.class));
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.d(TAG, "onErrorResponse: " + error.getMessage());
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", binding.username.getEditText().getText().toString());
                params.put("password", binding.password.getEditText().getText().toString());
                params.put("securitycode", "");
                params.put("expire", "");
                params.put("permission", "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("accept", "*/*");
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }
}
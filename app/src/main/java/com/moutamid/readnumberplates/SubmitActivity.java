package com.moutamid.readnumberplates;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.moutamid.readnumberplates.databinding.ActivitySubmitBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SubmitActivity extends AppCompatActivity {
    ActivitySubmitBinding binding;
    String number = "";
    String mimeType = "", fileName = "", fileToken = "";
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubmitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.back.setOnClickListener(v -> finish());

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        number = getIntent().getStringExtra(Constants.Number);

        mimeType = getIntent().getStringExtra(Constants.mimeType);
        fileName = getIntent().getStringExtra(Constants.fileName);
        fileToken = getIntent().getStringExtra(Constants.fileToken);

        Log.d(TAG, "fileToken: " + fileToken);
        Log.d(TAG, "fileName: " + fileName);
        Log.d(TAG, "mimeType: " + mimeType);

        binding.VehicleNo.getEditText().setText(number);

        String datetime = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        binding.OtherMaterials.getEditText().setText(datetime);

        binding.submit.setOnClickListener(v -> {
            postData();
        });

        binding.cashYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.CashValue.setVisibility(View.VISIBLE);
                binding.CashQty.setVisibility(View.VISIBLE);

                binding.CashValue.getEditText().setText("");
                binding.CashQty.getEditText().setText("");
            } else {
                binding.CashValue.setVisibility(View.GONE);
                binding.CashQty.setVisibility(View.GONE);

                binding.CashValue.getEditText().setText("0");
                binding.CashQty.getEditText().setText("0");
            }
        });
        binding.ChequeYES.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.ChequeValue.setVisibility(View.VISIBLE);
                binding.ChequeNo.setVisibility(View.VISIBLE);

                binding.ChequeNo.getEditText().setText("");
                binding.ChequeValue.getEditText().setText("");
            } else {
                binding.ChequeNo.setVisibility(View.GONE);
                binding.ChequeValue.setVisibility(View.GONE);

                binding.ChequeNo.getEditText().setText("0");
                binding.ChequeValue.getEditText().setText("0");
            }
        });
        binding.TV.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.TVValue.setVisibility(View.VISIBLE);
                binding.TVQty.setVisibility(View.VISIBLE);

                binding.TVQty.getEditText().setText("");
                binding.TVValue.getEditText().setText("");
            } else {
                binding.TVQty.setVisibility(View.GONE);
                binding.TVValue.setVisibility(View.GONE);

                binding.TVQty.getEditText().setText("0");
                binding.TVValue.getEditText().setText("0");
            }
        });

        binding.Saree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.SareeQty.setVisibility(View.VISIBLE);
                binding.SareeValue.setVisibility(View.VISIBLE);

                binding.SareeQty.getEditText().setText("");
                binding.SareeValue.getEditText().setText("");
            } else {
                binding.SareeQty.setVisibility(View.GONE);
                binding.SareeValue.setVisibility(View.GONE);

                binding.SareeQty.getEditText().setText("0");
                binding.SareeValue.getEditText().setText("0");
            }
        });
        binding.PressureCooker.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.PressureCookerValue.setVisibility(View.VISIBLE);
                binding.PressureCookerQty.setVisibility(View.VISIBLE);

                binding.PressureCookerValue.getEditText().setText("");
                binding.PressureCookerQty.getEditText().setText("");
            } else {
                binding.PressureCookerValue.setVisibility(View.GONE);
                binding.PressureCookerQty.setVisibility(View.GONE);

                binding.PressureCookerValue.getEditText().setText("0");
                binding.PressureCookerQty.getEditText().setText("0");
            }
        });
        binding.Watches.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.WatchesQty.setVisibility(View.VISIBLE);
                binding.WatchesValue.setVisibility(View.VISIBLE);

                binding.WatchesQty.getEditText().setText("");
                binding.WatchesValue.getEditText().setText("");
            } else {
                binding.WatchesQty.setVisibility(View.GONE);
                binding.WatchesValue.setVisibility(View.GONE);

                binding.WatchesQty.getEditText().setText("0");
                binding.WatchesValue.getEditText().setText("0");
            }
        });
        binding.liqureYES.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.LiquorValue.setVisibility(View.VISIBLE);
                binding.LiquorQty.setVisibility(View.VISIBLE);

                binding.LiquorQty.getEditText().setText("");
                binding.LiquorValue.getEditText().setText("");
            } else {
                binding.LiquorQty.setVisibility(View.GONE);
                binding.LiquorValue.setVisibility(View.GONE);

                binding.LiquorQty.getEditText().setText("0");
                binding.LiquorValue.getEditText().setText("0");
            }
        });
        binding.DrugsYES.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.DrugsValue.setVisibility(View.VISIBLE);
                binding.DrugsQty.setVisibility(View.VISIBLE);

                binding.DrugsValue.getEditText().setText("");
                binding.DrugsQty.getEditText().setText("");
            } else {
                binding.DrugsValue.setVisibility(View.GONE);
                binding.DrugsQty.setVisibility(View.GONE);

                binding.DrugsValue.getEditText().setText("0");
                binding.DrugsQty.getEditText().setText("0");
            }
        });
        binding.GoldYES.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.GoldValue.setVisibility(View.VISIBLE);
                binding.GoldQty.setVisibility(View.VISIBLE);

                binding.GoldQty.getEditText().setText("");
                binding.GoldValue.getEditText().setText("");
            } else {
                binding.GoldQty.setVisibility(View.GONE);
                binding.GoldValue.setVisibility(View.GONE);

                binding.GoldQty.getEditText().setText("0");
                binding.GoldValue.getEditText().setText("0");
            }
        });
        binding.SilverYES.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.SilverQty.setVisibility(View.VISIBLE);
                binding.SilverValue.setVisibility(View.VISIBLE);

                binding.SilverQty.getEditText().setText("");
                binding.SilverValue.getEditText().setText("");
            } else {
                binding.SilverQty.setVisibility(View.GONE);
                binding.SilverValue.setVisibility(View.GONE);

                binding.SilverQty.getEditText().setText("0");
                binding.SilverValue.getEditText().setText("0");
            }
        });
        binding.FreebieYES.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.FreebieQty.setVisibility(View.VISIBLE);
                binding.FreebieValue.setVisibility(View.VISIBLE);

                binding.FreebieQty.getEditText().setText("");
                binding.FreebieValue.getEditText().setText("");
            } else {
                binding.FreebieQty.setVisibility(View.GONE);
                binding.FreebieValue.setVisibility(View.GONE);

                binding.FreebieQty.getEditText().setText("0");
                binding.FreebieValue.getEditText().setText("0");
            }
        });

    }

    private static final String TAG = "SubmitActivity";

    private void postData() {
        JSONObject data = getJson();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        String url = "https://checkpost.vworks.in/api/add/SST";
        Log.d(TAG, "token: " + userModel.token);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        try {
                            JSONObject SST = response.getJSONObject("SST");
                            String SSTUID = SST.getString("SSTUID");
                            String message = "Your form is submitted the SSTUID # is " + SSTUID;
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(SubmitActivity.this, "Form submitted", Toast.LENGTH_SHORT).show();
                                new MaterialAlertDialogBuilder(SubmitActivity.this)
                                        .setTitle("Form Submitted Successfully")
                                        .setMessage(message)
                                        .setPositiveButton("Close", (dialog, which) -> {
                                            dialog.dismiss();
                                            finish();
                                        })
                                        .show();
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(progressDialog::dismiss);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(SubmitActivity.this, "Error : " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            // finish();
                        });
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("accept", "*/*");
                headers.put("X-Authorization", userModel.token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private JSONObject getJson() {
        JSONObject postData = new JSONObject();
        try {

            String cash = binding.cashYes.isChecked() ? "Yes" : "No";
            String Cheque = binding.ChequeYES.isChecked() ? "Yes" : "No";
            String TV = binding.TV.isChecked() ? "Yes" : "No";
            String Saree = binding.Saree.isChecked() ? "Yes" : "No";
            String Pressure_Cooker = binding.PressureCooker.isChecked() ? "Yes" : "No";
            String Watches = binding.Watches.isChecked() ? "Yes" : "No";
            String Liquor = binding.liqureYES.isChecked() ? "Yes" : "No";
            String Drugs = binding.DrugsYES.isChecked() ? "Yes" : "No";
            String Gold = binding.GoldYES.isChecked() ? "Yes" : "No";
            String Silver = binding.SilverYES.isChecked() ? "Yes" : "No";
            String Freebie = binding.FreebieYES.isChecked() ? "Yes" : "No";


//            postData.put("SSTUID", Integer.parseInt(binding.SSTUID.getEditText().getText().toString()));
            postData.put("VehicleNo", binding.VehicleNo.getEditText().getText().toString());

            postData.put("Cash", cash);
            postData.put("Cash_qty", Integer.parseInt(binding.CashQty.getEditText().getText().toString()));
            postData.put("Cash_Value", Integer.parseInt(binding.CashValue.getEditText().getText().toString()));

            postData.put("Cheque", Cheque);
            postData.put("Cheque_No", Integer.parseInt(binding.ChequeNo.getEditText().getText().toString()));
            postData.put("Cheque_Value", Integer.parseInt(binding.ChequeValue.getEditText().getText().toString()));

            postData.put("TV", TV);
            postData.put("TV_Qty", Integer.parseInt(binding.TVQty.getEditText().getText().toString()));
            postData.put("TV_Value", Integer.parseInt(binding.TVValue.getEditText().getText().toString()));

            postData.put("Saree", Saree);
            postData.put("Saree_Qty", Integer.parseInt(binding.SareeQty.getEditText().getText().toString()));
            postData.put("Saree_Value", Integer.parseInt(binding.SareeValue.getEditText().getText().toString()));

            postData.put("Pressure_Cooker", Pressure_Cooker);
            postData.put("Pressure_Cooker_qty", Integer.parseInt(binding.PressureCookerQty.getEditText().getText().toString()));
            postData.put("Pressure_Cooker_value", Integer.parseInt(binding.PressureCookerValue.getEditText().getText().toString()));

            postData.put("Watches", Watches);
            postData.put("Watches_qty", Integer.parseInt(binding.WatchesQty.getEditText().getText().toString()));
            postData.put("Watches_value", Integer.parseInt(binding.WatchesValue.getEditText().getText().toString()));

            postData.put("Other_Materials", "");

            postData.put("Misc", binding.Misc.getEditText().getText().toString());
            postData.put("Misc_qty", Integer.parseInt(binding.MiscQty.getEditText().getText().toString()));
            postData.put("Misc_value", Integer.parseInt(binding.MiscValue.getEditText().getText().toString()));

            postData.put("Liquor", Liquor);
            postData.put("Liquor_qty", Integer.parseInt(binding.LiquorQty.getEditText().getText().toString()));
            postData.put("Liquor_value", Integer.parseInt(binding.LiquorValue.getEditText().getText().toString()));

            postData.put("Drugs", Drugs);
            postData.put("Drugs_qty", Integer.parseInt(binding.DrugsQty.getEditText().getText().toString()));
            postData.put("Drugs_Value", Integer.parseInt(binding.DrugsValue.getEditText().getText().toString()));

            postData.put("Gold", Gold);
            postData.put("Gold_qty", Integer.parseInt(binding.GoldQty.getEditText().getText().toString()));
            postData.put("Gold_Value", Integer.parseInt(binding.GoldValue.getEditText().getText().toString()));

            postData.put("Silver", Silver);
            postData.put("Silver_qty", Integer.parseInt(binding.SilverQty.getEditText().getText().toString()));
            postData.put("Silver_Value", Integer.parseInt(binding.SilverValue.getEditText().getText().toString()));

            postData.put("Freebie", Freebie);
            postData.put("Freebie_qty", Integer.parseInt(binding.FreebieQty.getEditText().getText().toString()));
            postData.put("Freebie_Value", Integer.parseInt(binding.FreebieValue.getEditText().getText().toString()));

            int Total_qty = postData.getInt("Cash_qty") + postData.getInt("Cheque_No") + postData.getInt("TV_Qty") +
                    postData.getInt("Saree_Qty") + postData.getInt("Pressure_Cooker_qty") + postData.getInt("Watches_qty") +
                    postData.getInt("Misc_qty") + postData.getInt("Liquor_qty") + postData.getInt("Drugs_qty") +
                    postData.getInt("Gold_qty") + postData.getInt("Silver_qty") + postData.getInt("Freebie_qty");

            int Total_value = postData.getInt("Cash_Value") + postData.getInt("Cheque_Value") + postData.getInt("TV_Value") +
                    postData.getInt("Saree_Value") + postData.getInt("Pressure_Cooker_value") + postData.getInt("Watches_value") +
                    postData.getInt("Misc_value") + postData.getInt("Liquor_value") + postData.getInt("Drugs_Value") +
                    postData.getInt("Gold_Value") + postData.getInt("Silver_Value") + postData.getInt("Freebie_Value");

            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()); // 2024-03-22 11:25:41
//            String auditid = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); // 2024-03-22

            postData.put("Total_qty", Total_qty);
            postData.put("Total_value", Total_value);
            postData.put("datetime", datetime);
//            postData.put("auditid", auditid);
            postData.put("Hashsign", "");

            UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
            postData.put("staffid", userModel.name);
            postData.put("unit", "CHK App");
            postData.put("Remarks", binding.Remarks.getEditText().getText().toString());

            String file = "https://checkpost.vworks.in/uploads/temp__" + fileToken + "/" + fileName;
            String image = fileToken + "/" + fileName;
            Log.d(TAG, "getJson: " + image);
//            JSONObject image_ref = new JSONObject();
//            image_ref.put("type", mimeType);
//            image_ref.put("file", file);
//            image_ref.put("name", fileName);
//            Log.d(TAG, "image_ref: " + image_ref.toString());
            postData.put("image_ref", file);
            Log.d(TAG, "postData: " + postData.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return postData;
    }
}
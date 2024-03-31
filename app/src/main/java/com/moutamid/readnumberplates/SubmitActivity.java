package com.moutamid.readnumberplates;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.moutamid.readnumberplates.databinding.ActivitySubmitBinding;

public class SubmitActivity extends AppCompatActivity {
    ActivitySubmitBinding binding;
    String number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubmitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.back.setOnClickListener(v -> onBackPressed());

        number = getIntent().getStringExtra(Constants.Number);

        binding.vehicleNo.getEditText().setText(number);

    }
}
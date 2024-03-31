package com.moutamid.readnumberplates;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.moutamid.readnumberplates.databinding.ActivityOcrBinding;

public class OcrActivity extends AppCompatActivity {
    ActivityOcrBinding binding;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2001;
    String cameraPermission[];
    String storagePermission[];
    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOcrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES};
        //storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES};

        binding.pick.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Choose Image Source")
                    .setMessage("Select where you'd like to pick your image from")
                    .setPositiveButton("Camera", (dialog, which) -> {
                        if (!checkCameraPermission()) {
                            requestCameraPermission();
                        } else {
                            pickCamera();
                        }
                    }).setNegativeButton("Gallery", (dialog, which) -> {
                        if (!checkStoragePermission()) {
                            requestStoragePermission();
                        } else {
                            pickGallery();
                        }
                    })
                    .show();
        });

        binding.submit.setOnClickListener(v -> {
            startActivity(new Intent(this, SubmitActivity.class).putExtra(Constants.Number, binding.result.getEditText().getText().toString()));
        });
    }

    private void pickGallery() {
        ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(IMAGE_PICK_GALLERY_CODE);

        //intent to pick image from gallery
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {

        ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(IMAGE_PICK_CAMERA_CODE);
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, "NewPick");
//        values.put(MediaStore.Images.Media.DESCRIPTION, "Image To Text");
//        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
//        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        shouldShowRequestPermissionRationale(storagePermission[0]);
        shouldShowRequestPermissionRationale(storagePermission[1]);
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermission() {
        shouldShowRequestPermissionRationale(cameraPermission[0]);
        shouldShowRequestPermissionRationale(cameraPermission[1]);
        shouldShowRequestPermissionRationale(cameraPermission[2]);
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null){
            image_uri = data.getData(); //get image uri
            //set image to image view
            binding.imageIv.setImageURI(image_uri);
            binding.imagePreviewCard.setVisibility(View.VISIBLE);
            binding.submit.setEnabled(true);
            //get drawable bitmap for text recognition
            BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.imageIv.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();

            TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            if (!recognizer.isOperational()) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            } else {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> items = recognizer.detect(frame);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TextBlock myItem = items.valueAt(i);
                    sb.append(myItem.getValue());
                    sb.append("\n");
                }
                binding.result.getEditText().setText(sb.toString());
            }
        }
    }
}
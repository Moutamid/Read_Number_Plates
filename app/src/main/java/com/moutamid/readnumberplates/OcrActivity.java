package com.moutamid.readnumberplates;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.fxn.stash.Stash;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.ImagePickerActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.moutamid.readnumberplates.databinding.ActivityOcrBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OcrActivity extends AppCompatActivity {
    ActivityOcrBinding binding;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2001;
    String cameraPermission[];
    String storagePermission[];
    Uri image_uri;
    RequestQueue requestQueue;
    private static final String TAG = "OcrActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOcrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        Log.d(TAG, "onCreate: " + userModel.token);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        //storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};

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
            uploadFile();
        });
    }

    private void pickGallery() {
        ImagePicker.with(this)
                .crop(16, 9)
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
                .crop(16, 9)
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
        shouldShowRequestPermissionRationale(storagePermission[2]);
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
        shouldShowRequestPermissionRationale(cameraPermission[3]);
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

    File file;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            image_uri = data.getData(); //get image uri
            //set image to image view
            file = new File(image_uri.getPath());
            Log.d(TAG, "name: " + file.getName());
            Log.d(TAG, "path: " + file.getPath());
            binding.imageIv.setImageURI(image_uri);
            binding.imagePreviewCard.setVisibility(View.VISIBLE);
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
                String result = sb.toString().isEmpty() ? "No text Found" : sb.toString();
                binding.result.getEditText().setText(result);
                binding.submit.setEnabled(!sb.toString().isEmpty());
            }
        }
    }

    public void uploadFile() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();


        OkHttpClient client = new OkHttpClient();
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);

        String fileName = file.getName();
        String mimeType = URLConnection.guessContentTypeFromName(fileName);

        Log.d(TAG, "fileName: " + fileName);
        Log.d(TAG, "mimeType: " + mimeType);
        RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), file);

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBuilder.addFormDataPart("files[]", fileName, fileBody);

        MultipartBody requestBody = multipartBuilder.build();
        Request request = new Request.Builder()
                .url("https://checkpost.vworks.in/api/upload")
                .post(requestBody)
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("X-Authorization", "Bearer " + userModel.token)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(OcrActivity.this, "Image submit failed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    Log.d(TAG, "responseString: " + responseString);
                    runOnUiThread(() -> {
                        binding.imageIv.setImageResource(0);
                        binding.imagePreviewCard.setVisibility(View.GONE);
                        binding.result.getEditText().setText("");
                        binding.submit.setEnabled(false);
                    });
                    // {"success":true,"files":{"files":[{"name":"IMG_20240402_032244356.jpg","success":true}]},"filetoken":"71687312","version":"24.9.0"}
                    String fileToken = "";
                    String fileName = "";

                    try {
                        JSONObject object = new JSONObject(responseString);
                        JSONObject files = object.getJSONObject("files");
                        fileName = files.getJSONArray("files").getJSONObject(0).getString("name");
                        fileToken = object.getString("filetoken");

                        Log.d(TAG, "object: " + object.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    startActivity(new Intent(OcrActivity.this, SubmitActivity.class)
                            .putExtra(Constants.fileName, fileName)
                            .putExtra(Constants.fileToken, fileToken)
                            .putExtra(Constants.mimeType, mimeType)
                            .putExtra(Constants.Number, binding.result.getEditText().getText().toString()));
                } else {
                   runOnUiThread(() -> {
                       Toast.makeText(OcrActivity.this, "Image submit failed : " + response.message(), Toast.LENGTH_SHORT).show();
                   });
                    // Handle unsuccessful response
                    Log.d(TAG, "onResponse: " + response.message());
                }
            }
        });
    }

    // Method to convert file to byte array
    private byte[] getFileDataFromDrawable(File file) {
        FileInputStream fis;
        byte[] byteArray = null;
        try {
            fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

}
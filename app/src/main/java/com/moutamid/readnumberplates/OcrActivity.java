package com.moutamid.readnumberplates;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.moutamid.readnumberplates.databinding.ActivityOcrBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final int IMAGE_PICK_CAMERA_CODE = 2001;
    String cameraPermission[];
    Uri image_uri;
    RequestQueue requestQueue;
    private static final String TAG = "OcrActivity";
    TextRecognizer recognizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOcrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        Log.d(TAG, "onCreate: " + userModel.token);

        binding.pick.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            } else {
                binding.Error.setVisibility(View.GONE);
                pickCamera();
            }
        });

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        binding.submit.setOnClickListener(v -> {
            uploadFile();
        });
    }

    private void pickCamera() {

        ImagePicker.with(this)
                .crop(16, 9)
                .cameraOnly()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(IMAGE_PICK_CAMERA_CODE);
    }


    private boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
            boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == (PackageManager.PERMISSION_GRANTED);
            boolean result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == (PackageManager.PERMISSION_GRANTED);
            return result && result2 && result3;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
            boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == (PackageManager.PERMISSION_GRANTED);
            boolean result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == (PackageManager.PERMISSION_GRANTED);
            boolean result4 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == (PackageManager.PERMISSION_GRANTED);
            return result && result2 && result3 && result4;
        } else {
            boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
            boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
            return result && result1;
        }
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            cameraPermission = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO};
            shouldShowRequestPermissionRationale(cameraPermission[0]);
            shouldShowRequestPermissionRationale(cameraPermission[1]);
            shouldShowRequestPermissionRationale(cameraPermission[2]);
            ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            cameraPermission = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED};
            shouldShowRequestPermissionRationale(cameraPermission[0]);
            shouldShowRequestPermissionRationale(cameraPermission[1]);
            shouldShowRequestPermissionRationale(cameraPermission[2]);
            shouldShowRequestPermissionRationale(cameraPermission[3]);
            ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
        } else {
            cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            shouldShowRequestPermissionRationale(cameraPermission[0]);
            shouldShowRequestPermissionRationale(cameraPermission[1]);
            ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_IMAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_VIDEO = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && READ_IMAGE && READ_VIDEO) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_IMAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_VIDEO = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_MEDIA = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && READ_MEDIA && READ_IMAGE && READ_VIDEO) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
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

            InputImage image;
            try {
                image = InputImage.fromFilePath(OcrActivity.this, image_uri);
                Task<Text> result =
                        recognizer.process(image)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {
                                        // Task completed successfully
                                        String resultText = visionText.getText();
                                        Log.d(TAG, "onSuccess: " + resultText);
                                        binding.result.getEditText().setText(resultText);
                                        binding.submit.setEnabled(true);
                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                                Toast.makeText(OcrActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

//                String re = result.getResult();
            } catch (IOException e) {
                e.printStackTrace();
            }

/*            final String ANDROID_DATA_DIR = "/data/data/" + getPackageName();
            String filePath = "file:///android_asset/runtime_dir/openalpr.conf";
            final String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";
            Log.d(TAG, "onActivityResult: " + openAlprConfFile) ;
            String result = OpenALPR.Factory.create(OcrActivity.this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig("in", "", file.getAbsolutePath(), filePath, 1);
            Log.d(TAG, "onActivityResult: " + result);*/


/*            BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.imageIv.getDrawable();
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
                Log.d(TAG, "sb: " + sb.toString());
                String result = sb.toString().isEmpty() && !isValidVehicleNumberPlate(sb.toString()) ? "No text Found / Not a valid Pattern" : sb.toString();
                result = result.replace(" ", "").replace("-", "");
                if (result.startsWith("IND")){
                    result = result.replaceFirst("IND", "");
                }
                if (!isValidVehicleNumberPlate(result)) {
                    binding.Error.setVisibility(View.VISIBLE);
                    binding.Error.setText("Ensure your image solely features the valid license plate text without any additional text.");
                }
                Log.d(TAG, "result: " + result);
                Log.d(TAG, "isValidVehicleNumberPlate: " + isValidVehicleNumberPlate(result));
                Log.d(TAG, "sb: " + !sb.toString().isEmpty());
                Log.d(TAG, "both: " + (!sb.toString().isEmpty() && isValidVehicleNumberPlate(result)));
                binding.result.getEditText().setText(result);
                binding.submit.setEnabled((!sb.toString().isEmpty() && isValidVehicleNumberPlate(result)));
            }*/
        }
    }

    public static boolean isValidVehicleNumberPlate(String NUMBERPLATE) {
        String regex = "^[A-Z]{2}\\d{1,3}[A-Z]{1,3}\\d{4}$";
        Pattern p = Pattern.compile(regex);
        if (NUMBERPLATE == null) {
            Log.d(TAG, "isValidVehicleNumberPlate: NULL");
            return false;
        }
        Matcher m = p.matcher(NUMBERPLATE);
        return m.find();
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

                    runOnUiThread(() -> {
                        binding.imageIv.setImageResource(0);
                        binding.imagePreviewCard.setVisibility(View.GONE);
                        binding.submit.setEnabled(false);
                        binding.Error.setVisibility(View.GONE);
                    });
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
}
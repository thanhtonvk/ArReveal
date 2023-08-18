package com.thpttranquangkhai.arreveal.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.BitMapUtils;
import com.thpttranquangkhai.arreveal.Utilities.Constants;
import com.thpttranquangkhai.arreveal.Utilities.DrawBoundingBox;
import com.thpttranquangkhai.arreveal.ml.Model;
import com.thpttranquangkhai.arreveal.tflite.Classifier;
import com.thpttranquangkhai.arreveal.tflite.YoloV5Classifier;
import com.yalantis.ucrop.UCrop;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class CameraActivity extends AppCompatActivity {


    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private ImageCapture imageCapture;
    ImageView btnCapture, btnGalery, btnBack;
    ProgressBar progressBar;
    private static final int SELECT_IMAGE = 340;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        }
        initView();
        initModel();
        cameraShow();
        onClick();
        // Create a Canvas object.

    }

    private void onClick() {
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (top == 0 && left == 0 && right == 0 && bottom == 0) {
                    Toast.makeText(CameraActivity.this, "Chưa nhận diện được hình", Toast.LENGTH_SHORT).show();

                } else {
                    String str_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
                    File file = new File(getCacheDir(), str_uri);
                    OutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);
                        Bitmap bitmap = previewView.getBitmap();
                        Bitmap crop = cropImage(bitmap, top, left, right, bottom);
                        crop.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                        fOut.flush(); // Not really required
                        fOut.close(); // do not forget to close the stream
                        MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                        Uri saveUri = Uri.fromFile(file);


                        String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
                        File fileOutput = new File(getCacheDir(), dest_uri);

                        UCrop.Options options = new UCrop.Options();
                        UCrop.of(saveUri, Uri.fromFile(fileOutput))
                                .withOptions(options)
                                .withAspectRatio(0, 0)
                                .useSourceImageAspectRatio().withMaxResultSize(2000, 2000)
                                .start(CameraActivity.this);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        findViewById(R.id.btn_galery).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
                    }
                });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    public Bitmap cropImage(Bitmap originalImage, int top, int left, int right, int bottom) {
        if (top < 0) top = 0;
        if (left < 0) {
            left = 0;
        }
        int widthImage = originalImage.getWidth();
        int heightImage = originalImage.getHeight();
        if (right > widthImage) {
            right = widthImage;
        }
        if (bottom > heightImage) {
            bottom = heightImage;
        }
        int width = right - left;
        int height = bottom - top;
        Bitmap resultBmp = Bitmap.createBitmap(originalImage, left, top, width, height);

        return resultBmp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            Uri resultUri = UCrop.getOutput(data);
            uploadImage(resultUri);
        }
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
                    File fileOutput = new File(getCacheDir(), dest_uri);
                    UCrop.Options options = new UCrop.Options();
                    UCrop.of(data.getData(), Uri.fromFile(fileOutput))
                            .withOptions(options)
                            .withAspectRatio(0, 0)
                            .useSourceImageAspectRatio().withMaxResultSize(2000, 2000)
                            .start(CameraActivity.this);
                    finish();

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(CameraActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void uploadImage(Uri uri) {
        if (uri == null) return;
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            //giảm dung lượng
            Bitmap resize = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.2), (int) (bitmap.getHeight() * 0.2), true);
            Constants.bitmap = resize;
            byte[] anh = BitMapUtils.getBytes(resize);
            Constants.IMAGE = anh;
            startActivity(new Intent(getApplicationContext(), InformationActivity.class));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }



    ImageView img;
    float WIDTH;
    float HEIGHT;

    int top = 0;
    int left = 0;
    int right = 0;
    int bottom = 0;

    private void cameraShow() {
        img = findViewById(R.id.img);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = previewView.getBitmap();
                if (bitmap != null) {
                    WIDTH = bitmap.getWidth();
                    HEIGHT = bitmap.getHeight();
                    Log.d("IMAGESIZE", String.format("run: %s %s", WIDTH, HEIGHT));

                    float sizeWRate = WIDTH / 640f;
                    float sizeHRate = HEIGHT / 640f;
                    List<Classifier.Recognition> recognitions = detector.recognizeImage(bitmap);
                    if (recognitions.size() > 0) {
                        Classifier.Recognition recognition = getMax(recognitions);
                        RectF rectF = recognition.getLocation();
                        left = (int) (rectF.left * sizeWRate);
                        top = (int) (rectF.top * sizeHRate);
                        right = (int) (rectF.right * sizeWRate);
                        bottom = (int) (sizeHRate * rectF.bottom);
                        drawBoundingBox.updateRectangle((int) (rectF.left * sizeWRate), (int) (rectF.top * sizeHRate), (int) (rectF.right * sizeWRate), (int) (sizeHRate * rectF.bottom));
                        Log.d("POSITION", String.format("run: %s %s %s %s", (int) (rectF.left * sizeWRate), (int) (rectF.top * sizeHRate), (int) (rectF.right * sizeWRate), (int) (sizeHRate * rectF.bottom)));
                    } else {
                        top = 0;
                        left = 0;
                        right = 0;
                        bottom = 0;
                        drawBoundingBox.updateRectangle(0, 0, 0, 0);
                    }
                }
                handler.postDelayed(this, 33);
            }
        };
        handler.post(runnable);
    }

    private Classifier.Recognition getMax(List<Classifier.Recognition> recognitions) {
        Classifier.Recognition result = null;
        float max = 0;
        for (Classifier.Recognition rg : recognitions
        ) {
            if (rg.getConfidence() > max) {
                result = rg;
                max = rg.getConfidence();
            }
        }
        return result;

    }

    DrawBoundingBox drawBoundingBox;

    private void initView() {
        previewView = findViewById(R.id.preview);
        btnCapture = findViewById(R.id.btn_capture);
        btnBack = findViewById(R.id.btn_back);
        btnGalery = findViewById(R.id.btn_galery);
        progressBar = findViewById(R.id.progressBar);
        drawBoundingBox = findViewById(R.id.drawbox);



        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());
    }


    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setMaxResolution(new Size(640, 640))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();


        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }




    private Classifier detector;
    public static final int TF_OD_API_INPUT_SIZE = 640;

    private static final boolean TF_OD_API_IS_QUANTIZED = false;

    private static final String TF_OD_API_MODEL_FILE = "model_od.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/customclasses.txt";

    private void initModel() {
        try {
            detector =
                    YoloV5Classifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED,
                            TF_OD_API_INPUT_SIZE);
        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

    }

}

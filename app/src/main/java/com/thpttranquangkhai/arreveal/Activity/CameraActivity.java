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
    Model model;
    TensorBuffer inputFeature0;
    int DIM_BATCH_SIZE = 1;
    int DIM_IMG_SIZE_X = 600;
    int DIM_IMG_SIZE_Y = 600;
    int DIM_PIXEL_SIZE = 3;
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

    }

    private void onClick() {
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (crop != null) {

                    String str_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
                    File file = new File(getCacheDir(), str_uri);
                    OutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(file);
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
                } else {
                    Toast.makeText(CameraActivity.this, "Chưa nhận diện được hình", Toast.LENGTH_SHORT).show();
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
            byte[] anh = BitMapUtils.getBytes(resize);
            Constants.IMAGE = anh;
            startActivity(new Intent(getApplicationContext(), InformationActivity.class));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    float prob = 0.0f;
    Bitmap crop = null;
    ImageView img;
    float WIDTH;
    float HEIGHT;

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

                    float sizeWRate = WIDTH / 640f;
                    float sizeHRate = HEIGHT / 640f;
                    Log.e("TAG", "sizeWRate: " + sizeWRate);
                    Log.e("TAG", "sizeHRate: " + sizeHRate);

                    Log.e("TAG", "run: " + WIDTH);
                    Log.e("TAG", "run: " + HEIGHT);
                    List<Classifier.Recognition> recognitions = detector.recognizeImage(bitmap);
                    if (recognitions.size() > 0) {
                        Classifier.Recognition recognition = getMax(recognitions);
                        RectF rectF = recognition.getLocation();
                        assert (rectF.left < rectF.right && rectF.top < rectF.bottom);
                        crop = Bitmap.createBitmap((int) (rectF.right * sizeWRate - rectF.left * sizeWRate), (int)
                                (rectF.bottom * sizeHRate - rectF.top * sizeHRate), Bitmap.Config.ARGB_8888);
                        new Canvas(crop).drawBitmap(bitmap, -rectF.left * sizeWRate, -rectF.top * sizeHRate, null);
                        img.setImageBitmap(crop);
                        ByteBuffer byteBuffer = convertBitmapToByteBuffer(crop);
                        inputFeature0 = TensorBuffer.createFixedSize(new int[]{DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE}, DataType.FLOAT32);
                        inputFeature0.loadBuffer(byteBuffer);
                        Model.Outputs outputs = model.process(inputFeature0);
                        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                        float[] predict = outputFeature0.getFloatArray();
                        prob = predict[1];
                        if (prob < 0.5) {
                            progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        } else if (prob >= 0.5 && prob < 0.8) {
                            progressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                        } else {
                            progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                        }
                        progressBar.setProgress((int) (prob * 100));
                    }
                }
                handler.postDelayed(this, 500);
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

    private void initView() {
        previewView = findViewById(R.id.preview);
        btnCapture = findViewById(R.id.btn_capture);
        btnBack = findViewById(R.id.btn_back);
        btnGalery = findViewById(R.id.btn_galery);
        progressBar = findViewById(R.id.progressBar);
        try {
            model = Model.newInstance(CameraActivity.this);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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


    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, false);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; i++) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; j++) {
                int input = intValues[pixel++];
                byteBuffer.putFloat((((input >> 16 & 0xFF) - IMAGE_MEAN) / IMAGE_STD));
                byteBuffer.putFloat((((input >> 8 & 0xFF) - IMAGE_MEAN) / IMAGE_STD));
                byteBuffer.putFloat((((input & 0xFF) - IMAGE_MEAN) / IMAGE_STD));
            }
        }
        return byteBuffer;
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

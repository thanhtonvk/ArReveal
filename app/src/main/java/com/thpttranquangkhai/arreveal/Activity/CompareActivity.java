package com.thpttranquangkhai.arreveal.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.thpttranquangkhai.arreveal.Models.Entity;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.BitMapUtils;
import com.thpttranquangkhai.arreveal.Utilities.Constants;
import com.thpttranquangkhai.arreveal.ml.FeatureExtractor;
import com.thpttranquangkhai.arreveal.tflite.Classifier;
import com.thpttranquangkhai.arreveal.tflite.YoloV5Classifier;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CompareActivity extends AppCompatActivity implements View.OnTouchListener {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private ImageCapture imageCapture;
    Entity entity;
    List<Entity> entityList;
    ImageView img_display;

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        initView();
        result = Constants.entity;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        }
        img = findViewById(R.id.img);
        entity = Constants.entity;
        cameraShow();
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        img_display.setOnTouchListener(this);
        setRotationImage();
        videoView = findViewById(R.id.video_play);
        initModel();
    }

    float rotation = 0;

    private void setRotationImage() {
        findViewById(R.id.btn_rotation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotation += 90;
                if (rotation > 360) {
                    rotation = 0;
                }
                img_display.setRotation(rotation);
            }
        });
    }

    private double spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = (float) spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    // ...
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x,
                            event.getY() - start.y);
                } else if (mode == ZOOM) {
                    float newDist = (float) spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        return true; // indicate event was handled
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


    boolean flag = false;
    Entity result;
    float WIDTH;
    float HEIGHT;
    Bitmap crop;

    private void cameraShow() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = previewView.getBitmap();
                if (bitmap != null) {
                    if (!flag) {

                        List<Classifier.Recognition> recognitions = detector.recognizeImage(bitmap);
                        if (recognitions.size() > 0) {
                            search(recognitions, bitmap);
                        }
                    }
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(runnable);
    }

    private void display() {
        if (result.getType() == 1) {
            img_display.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext()) // replace with 'this' if it's in activity
                    .load(entity.getPath_file_online())
                    .into(img_display);
            img_display.setScaleType(ImageView.ScaleType.MATRIX);

        } else if (result.getType() == 0) {
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoPath(entity.getPath_file_online());
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });
            videoView.start();

        }
    }

    private void initView() {
        img_display = findViewById(R.id.img_display);
        previewView = findViewById(R.id.preview);

        try {
            model = FeatureExtractor.newInstance(this);

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

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();


        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    FeatureExtractor model;
    TensorBuffer inputFeature0;
    int DIM_BATCH_SIZE = 1;
    int DIM_IMG_SIZE_X = 224;
    int DIM_IMG_SIZE_Y = 224;
    int DIM_PIXEL_SIZE = 3;
    ImageView img;

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

    private float[] featureExtractor(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        inputFeature0 = TensorBuffer.createFixedSize(new int[]{DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE}, DataType.FLOAT32);
        inputFeature0.loadBuffer(byteBuffer);
        FeatureExtractor.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
        float[] features = outputFeature0.getFloatArray();
        return features;
    }

    private void search(List<Classifier.Recognition> recognitions, Bitmap bitmap) {
        float[] target = featureExtractor(Constants.bitmap);
        for (Classifier.Recognition recognition : recognitions) {
            RectF rectF = recognition.getLocation();
            WIDTH = bitmap.getWidth();
            HEIGHT = bitmap.getHeight();

            float sizeWRate = WIDTH / 640f;
            float sizeHRate = HEIGHT / 640f;
            Log.e("TAG", "sizeWRate: " + sizeWRate);
            Log.e("TAG", "sizeHRate: " + sizeHRate);

            Log.e("TAG", "run: " + WIDTH);
            Log.e("TAG", "run: " + HEIGHT);
            assert (rectF.left < rectF.right && rectF.top < rectF.bottom);
            crop = Bitmap.createBitmap((int) (rectF.right * sizeWRate - rectF.left * sizeWRate), (int)
                    (rectF.bottom * sizeHRate - rectF.top * sizeHRate), Bitmap.Config.ARGB_8888);
            new Canvas(crop).drawBitmap(bitmap, -rectF.left * sizeWRate, -rectF.top * sizeHRate, null);
            img.setImageBitmap(crop);
            float[] feature = featureExtractor(crop);
            double cosine = Constants.cosineSimilarity(feature, target);
            Log.e("Result", String.valueOf(cosine));
            if (cosine > 0.75) {
                flag = true;
                display();

            }
        }
    }


}
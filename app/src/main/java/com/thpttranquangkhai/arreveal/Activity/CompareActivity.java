package com.thpttranquangkhai.arreveal.Activity;

import static com.thpttranquangkhai.arreveal.Utilities.Constants.SUBJECT;


import androidx.annotation.NonNull;
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
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.service.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thpttranquangkhai.arreveal.Models.Entity;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;
import com.thpttranquangkhai.arreveal.Utilities.DrawBoundingBox;
import com.thpttranquangkhai.arreveal.ml.FeatureExtractor;
import com.thpttranquangkhai.arreveal.tflite.Classifier;
import com.thpttranquangkhai.arreveal.tflite.YoloV5Classifier;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CompareActivity extends AppCompatActivity implements View.OnTouchListener {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private ImageCapture imageCapture;
    List<Entity> entityList = new ArrayList<>();
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

    FirebaseDatabase database;
    DatabaseReference reference;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        initView();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        }
        img = findViewById(R.id.img);
        cameraShow();
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        img_display.setOnTouchListener(this);


        initModel();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Entity").child(SUBJECT.getId());
        loadDataList();

    }

    private void loadDataList() {
        ProgressDialog dialog = new ProgressDialog(CompareActivity.this);
        dialog.setTitle("Đang tải dữ liệu");
        dialog.show();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entityList.clear();
                for (DataSnapshot dataSnapshot :
                        snapshot.getChildren()) {
                    Entity entity = dataSnapshot.getValue(Entity.class);
                    entityList.add(entity);
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(CompareActivity.this, "Tải dữ liệu lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });

    }

    float rotation = 0;


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
    float WIDTH;
    float HEIGHT;
    ImageView img;

    int top = 0;
    int left = 0;
    int right = 0;
    int bottom = 0;
    DrawBoundingBox drawBoundingBox;

    private void cameraShow() {
        img = findViewById(R.id.img);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!flag) {
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
                            Bitmap cropImage = cropImage(bitmap, top, left, right, bottom);
                            Entity result = search(cropImage);
                            if (result != null) {
                                flag = true;
                                display(result);
                            }
                            Log.d("POSITION", String.format("run: %s %s %s %s", (int) (rectF.left * sizeWRate), (int) (rectF.top * sizeHRate), (int) (rectF.right * sizeWRate), (int) (sizeHRate * rectF.bottom)));
                        } else {
                            top = 0;
                            left = 0;
                            right = 0;
                            bottom = 0;
                            drawBoundingBox.updateRectangle(0, 0, 0, 0);
                        }
                    }
                }

                handler.postDelayed(this, 33);
            }
        };
        handler.post(runnable);
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

    private void display(Entity entity) {
        if (entity.getType() == 1) {
            img_display.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext()) // replace with 'this' if it's in activity
                    .load(entity.getPath_file_online())
                    .into(img_display);
            img_display.setScaleType(ImageView.ScaleType.MATRIX);

        } else if (entity.getType() == 0) {
            Constants.idYoutube = entity.getPath_file_online();
            startActivity(new Intent(CompareActivity.this, PlayVideoActivity.class));

        } else if (entity.getType() == 2) {
            Constants.idYoutube = entity.getPath_file_online();
            startActivity(new Intent(CompareActivity.this, PlayYoutubeActivity.class));
        }
    }

    private void initView() {
        img_display = findViewById(R.id.img_display);
        previewView = findViewById(R.id.preview);
        drawBoundingBox = findViewById(R.id.drawbox);
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

    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 255.0f;

    FeatureExtractor model;
    TensorBuffer inputFeature0;
    int DIM_BATCH_SIZE = 1;
    int DIM_IMG_SIZE_X = 32;
    int DIM_IMG_SIZE_Y = 32;
    int DIM_PIXEL_SIZE = 3;

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

    private Entity search(Bitmap bitmap) {
        double max = 0;
        Entity result = null;
        float[] target = featureExtractor(bitmap);
        for (Entity entity : entityList
        ) {
            double cosine = Constants.cosineSimilarity(entity.convertListToArray(), target);
            Log.d("COSINE", "search: " + cosine);
            if (cosine > Constants.Threshold) {
                if (cosine > max) {
                    Toast.makeText(this, "score " + cosine, Toast.LENGTH_SHORT).show();
                    result = entity;
                    max = cosine;

                }
            }
        }
        return result;
    }


}
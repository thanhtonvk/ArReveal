package com.thpttranquangkhai.arreveal.Activity;

import static com.thpttranquangkhai.arreveal.MainActivity.fileUtils;
import static com.thpttranquangkhai.arreveal.Utilities.Constants.SUBJECT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.thpttranquangkhai.arreveal.Models.Entity;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.BitMapUtils;
import com.thpttranquangkhai.arreveal.Utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thpttranquangkhai.arreveal.ml.FeatureExtractor;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InformationActivity extends AppCompatActivity {

    TextView edtFile;
    EditText edtName;
    RadioButton rbVideo, rbGif, rbYoutube;
    ImageView imageView;
    Entity entity;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;
    Random random;
    CardView cvUpload, cvYoutube;
    EditText edtLinkYoutube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        initView();
        onClick();
        initModel();
        random = new Random();
        imageView.setImageBitmap(BitMapUtils.getImage(Constants.IMAGE));

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        entity = new Entity();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Entity");
        firebaseStorage = FirebaseStorage.getInstance();
        entity.setEmbedding(featureExtractor(Constants.bitmap));
    }


    private void initView() {
        edtFile = findViewById(R.id.edt_file);
        edtName = findViewById(R.id.edt_name);
        rbVideo = findViewById(R.id.rb_video);
        rbGif = findViewById(R.id.rb_hinhdong);
        rbYoutube = findViewById(R.id.rb_youtube);
        imageView = findViewById(R.id.img_image);
        cvUpload = findViewById(R.id.cv_upload);
        cvYoutube = findViewById(R.id.cv_youtube);
        edtLinkYoutube = findViewById(R.id.edt_link);

    }

    private static final int REQUEST_UPLOAD = 3234;

    private void onClick() {

        edtFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!rbYoutube.isChecked()) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(Intent.createChooser(intent, "Open folder"), REQUEST_UPLOAD);
                }

            }
        });
        rbGif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (rbGif.isChecked()) {
                    cvUpload.setVisibility(View.VISIBLE);
                    cvYoutube.setVisibility(View.GONE);
                }

            }
        });
        rbVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (rbVideo.isChecked()) {
                    cvUpload.setVisibility(View.VISIBLE);
                    cvYoutube.setVisibility(View.GONE);
                }

            }
        });
        rbYoutube.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (rbYoutube.isChecked()) {
                    cvUpload.setVisibility(View.GONE);
                    cvYoutube.setVisibility(View.VISIBLE);
                }


            }
        });
        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtName.getText().toString().equals("")) {
                    Toast.makeText(InformationActivity.this, "Tên không được bỏ trống", Toast.LENGTH_LONG).show();
                } else {
                    int type = 0;
                    if (rbGif.isChecked()) {
                        type = 1;
                    }
                    if (rbVideo.isChecked()) {
                        type = 0;
                    }
                    if (rbYoutube.isChecked()) {
                        type = 2;

                    }
                    entity.setId(random.nextInt());
                    entity.setName(edtName.getText().toString());
                    entity.setType(type);
                    add();


                }
            }
        });
    }


    public String youtubeParser(String url) {
        String regex = "^.*((youtu.be/)|(v/)|(/u/\\w/)|(embed/)|(watch\\?))\\??v?=?([^#&?]*).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches() && matcher.group(7).length() == 11) {
            return matcher.group(7);
        } else {
            return null;
        }
    }

    String fileName = "";

    private void add() {
        ProgressDialog dialog = new ProgressDialog(InformationActivity.this);
        dialog.setTitle("Đang upload");
        dialog.show();
        if (rbYoutube.isChecked()) {
            String values = edtLinkYoutube.getText().toString();
            fileName = youtubeParser(values);
            if (fileName == null) {
                Toast.makeText(InformationActivity.this, "Đường dẫn không hợp lệ", Toast.LENGTH_LONG).show();
            } else {
                entity.setPath_file_online(fileName);
                storageReference = firebaseStorage.getReference(fileName);
                storageReference.putBytes(Constants.IMAGE).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.e("TAG", "upload image " + uri.toString());
                                entity.setImage_online(uri.toString());
                                databaseReference.child(SUBJECT.getId()).child(String.valueOf(entity.getId())).setValue(entity).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Thành công", Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                            finish();
                                            startActivity(new Intent(getApplicationContext(), UserActivity.class));
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("TAG", "onFailure: " + e.toString());
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Upload lỗi", Toast.LENGTH_LONG).show();
                        Log.e("TAG", "onFailure: " + e.toString());
                        dialog.dismiss();
                    }
                });
            }

        } else {
            fileName = path.split("/")[path.split("/").length - 1];
            storageReference = firebaseStorage.getReference(fileName);
            storageReference.putFile(uriUploadFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.e("TAG", "upload file " + uri.toString());
                            entity.setPath_file_online(uri.toString());

                            storageReference = firebaseStorage.getReference("image" + fileName.split("\\.")[0] + ".jpg");
                            storageReference.putBytes(Constants.IMAGE).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.e("TAG", "upload image " + uri.toString());
                                            entity.setImage_online(uri.toString());
                                            databaseReference.child(SUBJECT.getId()).child(String.valueOf(entity.getId())).setValue(entity).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "Thành công", Toast.LENGTH_LONG).show();
                                                        dialog.dismiss();
                                                        finish();
                                                        startActivity(new Intent(getApplicationContext(), UserActivity.class));
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("TAG", "onFailure: " + e.toString());
                                                }
                                            });
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Upload lỗi", Toast.LENGTH_LONG).show();
                                    Log.e("TAG", "onFailure: " + e.toString());
                                    dialog.dismiss();
                                }
                            });
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Upload lỗi", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    Log.e("TAG", "onFailure: " + e.toString());
                }
            });
        }


    }

    String path;
    Uri uriUploadFile;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 255.0f;

    FeatureExtractor model;
    TensorBuffer inputFeature0;
    int DIM_BATCH_SIZE = 1;
    int DIM_IMG_SIZE_X = 32;
    int DIM_IMG_SIZE_Y = 32;
    int DIM_PIXEL_SIZE = 3;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_UPLOAD) {
            assert data != null;
            uriUploadFile = data.getData();
            path = fileUtils.getPath(uriUploadFile);
            Log.e("TAG", "File path " + path);
            edtFile.setText(path.split("/")[path.split("/").length - 1]);
        }
    }

    private void initModel() {
        try {
            model = FeatureExtractor.newInstance(this);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Float> featureExtractor(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        inputFeature0 = TensorBuffer.createFixedSize(new int[]{DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE}, DataType.FLOAT32);
        inputFeature0.loadBuffer(byteBuffer);
        FeatureExtractor.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
        float[] features = outputFeature0.getFloatArray();
        List<Float> listOfFloats = new ArrayList<>();
        for (float feat : features) {
            listOfFloats.add(feat);
        }
        return listOfFloats;
    }

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
}
package com.thpttranquangkhai.arreveal.Activity;

import static com.thpttranquangkhai.arreveal.MainActivity.fileUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.Random;

public class InformationActivity extends AppCompatActivity {

    TextView edtFile;
    EditText edtName;
    RadioButton rbVideo, rbGif, rbModel;
    ImageView imageView;
    Entity entity;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;
    Random random;

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

    }


    private void initView() {
        edtFile = findViewById(R.id.edt_file);
        edtName = findViewById(R.id.edt_name);
        rbVideo = findViewById(R.id.rb_video);
        rbGif = findViewById(R.id.rb_hinhdong);
        rbModel = findViewById(R.id.rb_mohinh);
        imageView = findViewById(R.id.img_image);

    }

    private static final int REQUEST_UPLOAD = 3;

    private void onClick() {
        edtFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "Open folder"), REQUEST_UPLOAD);
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
                    if (rbModel.isChecked()) {
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

    private void add() {
        ProgressDialog dialog = new ProgressDialog(InformationActivity.this);
        String fileName = path.split("/")[path.split("/").length - 1];
        dialog.setTitle("Đang upload");
        dialog.show();
        storageReference = firebaseStorage.getReference(fileName);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e("TAG", "upload file" + uri.toString());
                        entity.setPath_file_online(uri.toString());
                        storageReference = firebaseStorage.getReference(fileName.split("\\.")[0] + ".jpg");
                        storageReference.putBytes(Constants.IMAGE).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.e("TAG", "upload image" + uri.toString());
                                        entity.setImage_online(uri.toString());
                                        databaseReference.child(Constants.idClassroom).child(String.valueOf(entity.getId())).setValue(entity).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    String path;
    Uri uri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            assert data != null;
            uri = data.getData();
            path = fileUtils.getPath(uri);
            Log.e("TAG", "File path " + path);
            edtFile.setText(path.split("/")[path.split("/").length - 1]);
        }
    }

}
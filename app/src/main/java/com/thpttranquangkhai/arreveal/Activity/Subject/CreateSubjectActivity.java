package com.thpttranquangkhai.arreveal.Activity.Subject;

import static com.thpttranquangkhai.arreveal.MainActivity.fileUtils;
import static com.thpttranquangkhai.arreveal.Utilities.Constants.GRADE;
import static com.thpttranquangkhai.arreveal.Utilities.Constants.SCHOOL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thpttranquangkhai.arreveal.Activity.School.CreateActivity;
import com.thpttranquangkhai.arreveal.Models.Subject;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.Random;

public class CreateSubjectActivity extends AppCompatActivity {

    private static final int REQUEST_UPLOAD = 1234;
    TextInputEditText edtName, edtDetail;
    ImageView imgLogo;
    ImageView btnBack, btnOk;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_subject);
        initView();
        onClick();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
    }

    private void onClick() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (linkImage.isEmpty()) {
                    Toast.makeText(CreateSubjectActivity.this, "Đang upload image", Toast.LENGTH_SHORT).show();
                } else {
                    String name = edtName.getText().toString();
                    String detail = edtDetail.getText().toString();
                    if (name.isEmpty() || detail.isEmpty()) {
                        Toast.makeText(CreateSubjectActivity.this, "Không được bỏ trống ", Toast.LENGTH_SHORT).show();
                    } else {
                        int id = new Random().nextInt(100000);
                        Subject subject = new Subject(String.valueOf(id), name, detail, linkImage, "");
                        createSubject(subject);
                    }

                }
            }
        });
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalery();
            }
        });
    }

    private void initView() {
        edtName = findViewById(R.id.edt_subject_name);
        edtDetail = findViewById(R.id.edt_detail);
        imgLogo = findViewById(R.id.img_logo);
        btnOk = findViewById(R.id.btn_ok);
        btnBack = findViewById(R.id.btn_back);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Subject");
    }

    private void createSubject(Subject subject) {
        ProgressDialog dialog = new ProgressDialog(CreateSubjectActivity.this);
        dialog.setTitle("Đang xử lý");
        dialog.show();
        reference.child(SCHOOL.getId()).child(GRADE.getId()).child(subject.getId()).setValue(subject).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreateSubjectActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    Toast.makeText(CreateSubjectActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateSubjectActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

    }

    String linkImage = "";

    private void openGalery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_UPLOAD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPLOAD) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    assert data != null;
                    Uri uri = data.getData();
                    imgLogo.setImageURI(uri);
                    uploadImage(uri);

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(CreateSubjectActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    StorageReference storageReference;
    FirebaseStorage firebaseStorage;

    private void uploadImage(Uri uri) {
        firebaseStorage = FirebaseStorage.getInstance();
        String path = fileUtils.getPath(uri);
        String fileName = path.split("/")[path.split("/").length - 1];
        storageReference = firebaseStorage.getReference(fileName);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        linkImage = uri.toString();
                        Log.d("TAG", "onSuccess: upload sucess " + linkImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("GET LINK IMAGE", "onFailure: " + e, e);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("UPLOAD", "onFailure: " + e, e);
            }
        });
    }

}
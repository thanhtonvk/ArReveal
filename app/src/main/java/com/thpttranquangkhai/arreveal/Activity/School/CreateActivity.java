package com.thpttranquangkhai.arreveal.Activity.School;

import static com.thpttranquangkhai.arreveal.MainActivity.fileUtils;

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
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thpttranquangkhai.arreveal.Activity.MainPageActivity;
import com.thpttranquangkhai.arreveal.Models.School;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.Random;

public class CreateActivity extends AppCompatActivity {
    private static final int REQUEST_UPLOAD = 776;
    ImageView btnBack, btnDone, btnLogo;
    TextInputEditText edtSchoolName, edtAddress, edtPhoneNumber;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        initView();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        onClick();
    }

    private void onClick() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalery();
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtSchoolName.getText().toString();
                String address = edtAddress.getText().toString();
                String phoneNumber = edtPhoneNumber.getText().toString();
                if (linkImage.isEmpty()) {
                    Toast.makeText(CreateActivity.this, "Đang upload logo!", Toast.LENGTH_SHORT).show();
                } else {
                    if (name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
                        Toast.makeText(CreateActivity.this, "Thông tin không được bỏ trống", Toast.LENGTH_SHORT).show();
                    } else {
                        int id = new Random().nextInt(100000);
                        School school = new School(String.valueOf(id), name, address, linkImage, phoneNumber, Constants.ACCOUNT.getId());
                        createSchool(school);
                    }
                }

            }
        });
    }

    private void createSchool(School school) {
        ProgressDialog dialog = new ProgressDialog(CreateActivity.this);
        dialog.setTitle("Đang khởi tạo");
        dialog.show();
        reference.child("School").child(school.getId()).setValue(school).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    joinSchool(school);
                } else {
                    Toast.makeText(CreateActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
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

    private void joinSchool(School school) {

        reference = database.getReference("JoinedSchool");
        reference.child(Constants.ACCOUNT.getId()).child(school.getId()).setValue(school).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreateActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateActivity.this, "Thất bại, lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPLOAD) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    assert data != null;
                    Uri uri = data.getData();
                    btnLogo.setImageURI(uri);
                    uploadImage(uri);

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(CreateActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
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

    private void initView() {
        btnBack = findViewById(R.id.btn_back);
        btnDone = findViewById(R.id.btn_ok);
        btnLogo = findViewById(R.id.img_logo);
        edtSchoolName = findViewById(R.id.edt_school_name);
        edtAddress = findViewById(R.id.edt_address);
        edtPhoneNumber = findViewById(R.id.edt_phone_number);

    }
}
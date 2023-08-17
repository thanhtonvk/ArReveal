package com.thpttranquangkhai.arreveal.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thpttranquangkhai.arreveal.Activity.School.CreateActivity;
import com.thpttranquangkhai.arreveal.Adapter.SchoolAdapter;
import com.thpttranquangkhai.arreveal.Models.JoinedClassroom;
import com.thpttranquangkhai.arreveal.Models.School;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainPageActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase database;
    DatabaseReference reference;
    Random random;
    TextView tvName;
    FloatingActionButton btnAdd;
    List<School> schoolList = new ArrayList<>();
    SchoolAdapter schoolAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        initView();
        onClick();
        firebaseUser = firebaseAuth.getCurrentUser();
        tvName.setText(firebaseUser.getDisplayName());
        loadClassroom();


    }


    private void loadClassroom() {
        reference = database.getReference("JoinedSchool").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                schoolList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()
                ) {
                    schoolList.add(snapshot.getValue(School.class));
                    schoolAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onClick() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });
        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(MainPageActivity.this, LoginActivity.class));
            }
        });
    }

    private void alertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainPageActivity.this);
        builder.setTitle("Bạn muốn làm gì?");
        builder.setPositiveButton("Tham gia trường học", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogJoin();
            }
        });
        builder.setNeutralButton("Tạo trường học", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(getApplicationContext(), CreateActivity.class));
            }
        });
        builder.show();
    }


    private void dialogJoin() {
        Dialog dialog = new Dialog(MainPageActivity.this);
        dialog.setContentView(R.layout.dialog_class_room);
        Button btnJoin = dialog.findViewById(R.id.btn_add);
        EditText edtText = dialog.findViewById(R.id.edt_edit);
        btnJoin.setText("Tham gia");
        edtText.setHint("Nhập mã trường");
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = edtText.getText().toString();
                if (id.isEmpty()) {
                    Toast.makeText(MainPageActivity.this, "Mã trường không được để trống", Toast.LENGTH_SHORT).show();
                } else {
                    joinSchool(id);
                }
            }
        });
        dialog.show();
    }

    private void joinSchool(String id) {
        reference = database.getReference("School");
        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    Toast.makeText(MainPageActivity.this, "Trường không tồn tại", Toast.LENGTH_SHORT).show();
                } else {
                    School school = snapshot.getValue(School.class);
                    reference = database.getReference("JoinedSchool");
                    reference.child(Constants.ACCOUNT.getId()).child(school.getId()).setValue(school).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainPageActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainPageActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainPageActivity.this, "Thất bại, lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initView() {
        tvName = findViewById(R.id.tv_name);
        btnAdd = findViewById(R.id.btn_add);
        random = new Random();
        recyclerView = findViewById(R.id.rcv_item);
        schoolAdapter = new SchoolAdapter(MainPageActivity.this, schoolList);
        recyclerView.setAdapter(schoolAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
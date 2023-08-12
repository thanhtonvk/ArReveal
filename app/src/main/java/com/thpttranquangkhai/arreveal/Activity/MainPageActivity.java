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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thpttranquangkhai.arreveal.Adapter.ClassroomAdapter;
import com.thpttranquangkhai.arreveal.Models.Account;
import com.thpttranquangkhai.arreveal.Models.Classroom;
import com.thpttranquangkhai.arreveal.Models.JoinedClassroom;
import com.thpttranquangkhai.arreveal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MainPageActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase database;
    DatabaseReference reference;
    Random random;
    TextView tvName;
    FloatingActionButton btnAdd;
    List<JoinedClassroom> classroomList = new ArrayList<>();
    ClassroomAdapter classroomAdapter;
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
        reference = database.getReference("Joined").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                classroomList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()
                ) {
                    classroomList.add(snapshot.getValue(JoinedClassroom.class));
                    classroomAdapter.notifyDataSetChanged();
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
        builder.setPositiveButton("Tham gia lớp học", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogJoin();
            }
        });
        builder.setNeutralButton("Tạo lớp học", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogCreate();
            }
        });
        builder.show();
    }

    private void dialogCreate() {
        Dialog dialog = new Dialog(MainPageActivity.this);
        dialog.setContentView(R.layout.dialog_class_room);
        Button btnJoin = dialog.findViewById(R.id.btn_add);
        EditText edtText = dialog.findViewById(R.id.edt_edit);
        btnJoin.setText("Tạo lớp");
        edtText.setHint("Nhập tên lớp");

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String className = edtText.getText().toString();
                if (className.equals("")) {
                    Toast.makeText(MainPageActivity.this, "Tên lớp không được để trống", Toast.LENGTH_SHORT).show();
                } else {
                    Classroom classroom = new Classroom();
                    int id = random.nextInt(9000) + 1000;
                    classroom.setClass_name(className);
                    classroom.setId(String.valueOf(id));
                    classroom.setIdHost(firebaseUser.getUid());
                    classroom.setHost_name(firebaseUser.getDisplayName());
                    reference = database.getReference("Classroom");
                    reference.child(classroom.getId()).setValue(classroom).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                JoinedClassroom joinedClassroom = new JoinedClassroom();
                                joinedClassroom.setId(String.valueOf(id));
                                joinedClassroom.setHostName(classroom.getHost_name());
                                joinedClassroom.setName(classroom.getClass_name());
                                joinedClassroom.setIdHost(classroom.getIdHost());
                                reference = database.getReference("Joined");
                                reference.child(firebaseUser.getUid()).child(joinedClassroom.getId()).setValue(joinedClassroom).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            joinClass(joinedClassroom.getId());
                                            Toast.makeText(MainPageActivity.this, "Tạo thành công", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(MainPageActivity.this, "Tạo không thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(MainPageActivity.this, "Tạo không thành công", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        dialog.show();
    }

    private void dialogJoin() {
        Dialog dialog = new Dialog(MainPageActivity.this);
        dialog.setContentView(R.layout.dialog_class_room);
        Button btnJoin = dialog.findViewById(R.id.btn_add);
        EditText edtText = dialog.findViewById(R.id.edt_edit);
        btnJoin.setText("Tham gia");
        edtText.setHint("Nhập mã lớp");
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = edtText.getText().toString();
                reference = database.getReference("Classroom");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean check = false;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.getKey().equals(id)) {
                                check = true;
                                Classroom classroom = dataSnapshot.getValue(Classroom.class);
                                JoinedClassroom joinedClassroom = new JoinedClassroom();
                                joinedClassroom.setIdHost(classroom.getIdHost());
                                joinedClassroom.setId(classroom.getId());
                                joinedClassroom.setName(classroom.getClass_name());
                                joinedClassroom.setHostName(classroom.getHost_name());
                                reference = database.getReference("Joined");
                                reference.child(firebaseUser.getUid()).child(joinedClassroom.getId()).setValue(joinedClassroom).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            joinClass(joinedClassroom.getId());
                                            Toast.makeText(MainPageActivity.this, "Tham gia thành công", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(MainPageActivity.this, "Tham gia không thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                        if (!check) {
                            Toast.makeText(MainPageActivity.this, "Lớp học không tồn tại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        dialog.show();
    }

    private void joinClass(String idClass) {
        reference = database.getReference("ListJoined");
        Account account = new Account();
        account.setId(firebaseUser.getUid());
        account.setName(firebaseUser.getDisplayName());
        reference.child(idClass).child(account.getId()).setValue(account).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }

    private void initView() {
        tvName = findViewById(R.id.tv_name);
        btnAdd = findViewById(R.id.btn_add);
        random = new Random();
        recyclerView = findViewById(R.id.rcv_item);
        classroomAdapter = new ClassroomAdapter(MainPageActivity.this, classroomList);
        recyclerView.setAdapter(classroomAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }
}
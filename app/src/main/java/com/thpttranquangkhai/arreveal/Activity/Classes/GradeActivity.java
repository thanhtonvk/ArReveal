package com.thpttranquangkhai.arreveal.Activity.Classes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thpttranquangkhai.arreveal.Models.Grade;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.thpttranquangkhai.arreveal.Utilities.Constants.SCHOOL;

public class GradeActivity extends AppCompatActivity {
    ImageView btnInfo;
    TextView tvSchoolName;
    FloatingActionButton btnAdd;
    RecyclerView rcvGrade;
    FirebaseDatabase database;
    DatabaseReference reference;
    GradeAdapter gradeAdapter;
    List<Grade> gradeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);
        initView();
        checkAdmin();
        gradeAdapter = new GradeAdapter(GradeActivity.this, gradeList);
        rcvGrade.setAdapter(gradeAdapter);
        loadData();
        onClick();
        tvSchoolName.setText(SCHOOL.getId() + " - " + SCHOOL.getName());

    }

    private void onClick() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCreate();
            }
        });
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String info = String.format("Mã trường: %s\nTên trường: %s\nĐịa chỉ: %s\nSố điện thoại: %s", SCHOOL.getId(), SCHOOL.getName(), SCHOOL.getAddress(), SCHOOL.getPhoneNumber());
                new AlertDialog.Builder(GradeActivity.this).setTitle("Thông tin").setMessage(info).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });
    }

    private void loadData() {
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gradeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Grade grade = dataSnapshot.getValue(Grade.class);
                    gradeList.add(grade);
                    gradeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dialogCreate() {
        EditText edtGradeName = new EditText(GradeActivity.this);
        edtGradeName.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(GradeActivity.this).setTitle("Nhập khối/lớp").setView(edtGradeName).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String gradeName = edtGradeName.getText().toString();
                if (gradeName.isEmpty()) {
                    Toast.makeText(GradeActivity.this, "Không được để trống", Toast.LENGTH_SHORT).show();
                } else {
                    Grade grade = new Grade(gradeName, Integer.parseInt(gradeName), SCHOOL.getId());
                    createGrade(grade);
                }
            }
        }).setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void createGrade(Grade grade) {
        ProgressDialog dialog = new ProgressDialog(GradeActivity.this);
        dialog.setTitle("Đang tạo lớp");
        dialog.show();
        reference.child(grade.getId()).setValue(grade).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(GradeActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GradeActivity.this, "Không thành công", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GradeActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }


    private void checkAdmin() {
        if (!Objects.equals(Constants.ACCOUNT.getId(), SCHOOL.getIdAccount())) {
            btnAdd.setVisibility(View.GONE);
        }
    }

    private void initView() {
        btnInfo = findViewById(R.id.btn_info);
        tvSchoolName = findViewById(R.id.tv_name);
        btnAdd = findViewById(R.id.btn_add);
        rcvGrade = findViewById(R.id.rcv_item);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Grade").child(SCHOOL.getId());
    }
}
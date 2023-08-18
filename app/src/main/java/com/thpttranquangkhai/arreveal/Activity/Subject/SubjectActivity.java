package com.thpttranquangkhai.arreveal.Activity.Subject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thpttranquangkhai.arreveal.Models.Subject;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubjectActivity extends AppCompatActivity {
    ImageView btnInfo;
    TextView tvSchoolName;
    FloatingActionButton btnAdd;
    RecyclerView rcvSubject;
    List<Subject> subjectList = new ArrayList<>();
    SubjectAdapter adapter;
    DatabaseReference reference;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        initView();
        checkAdmin();
        onClick();
        loadData();
    }

    private void onClick() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SubjectActivity.this, CreateSubjectActivity.class));
            }
        });
    }

    private void checkAdmin() {
        if (!Objects.equals(Constants.ACCOUNT.getId(), Constants.SCHOOL.getIdAccount())) {
            btnAdd.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        reference.child(Constants.SCHOOL.getId()).child(Constants.GRADE.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Subject subject = dataSnapshot.getValue(Subject.class);
                    subjectList.add(subject);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initView() {
        btnInfo = findViewById(R.id.btn_info);
        tvSchoolName = findViewById(R.id.tv_name);
        btnAdd = findViewById(R.id.btn_add);
        rcvSubject = findViewById(R.id.rcv_item);
        adapter = new SubjectAdapter(SubjectActivity.this, subjectList);
        rcvSubject.setAdapter(adapter);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Subject");
    }
}
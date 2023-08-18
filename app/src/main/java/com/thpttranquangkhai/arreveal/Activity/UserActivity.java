package com.thpttranquangkhai.arreveal.Activity;

import static com.thpttranquangkhai.arreveal.Utilities.Constants.SCHOOL;
import static com.thpttranquangkhai.arreveal.Utilities.Constants.SUBJECT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thpttranquangkhai.arreveal.Adapter.EntityAdapter;
import com.thpttranquangkhai.arreveal.Models.Account;
import com.thpttranquangkhai.arreveal.Models.Entity;
import com.thpttranquangkhai.arreveal.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EntityAdapter adapter;
    List<Entity> entityList = new ArrayList<>();
    EditText edtSearch;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    TextView tvName;
    ImageView user;
    FirebaseAuth mAuth;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FloatingActionButton button_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Entity");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        initView();
        adapter = new EntityAdapter(entityList, UserActivity.this);
        recyclerView.setAdapter(adapter);
        loadData();
        onClick();
        mAuth = FirebaseAuth.getInstance();
//        tvName.setText("Mã lớp: " + Constants.idClassroom + "  -  " + Constants.className);
        if (Constants.SUBJECT.getIdTeacher().equals(firebaseUser.getUid()) || Constants.ACCOUNT.getId().equals(SCHOOL.getIdAccount())) {

        } else {
            button_add.setVisibility(View.GONE);
        }
    }


    private void onClick() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() != 0) {
                    List<Entity> temp = new ArrayList<>();
                    for (Entity entity : entityList
                    ) {
                        String search = edtSearch.getText().toString().toLowerCase(Locale.ROOT);
                        if (entity.getName().toLowerCase(Locale.ROOT).contains(search)) {
                            temp.add(entity);
                        }
                        adapter = new EntityAdapter(temp, getApplicationContext());
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    adapter = new EntityAdapter(entityList, getApplicationContext());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CameraActivity.class));
            }
        });
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, AccountActivity.class));
            }
        });
    }

    private void initView() {
        recyclerView = findViewById(R.id.rcv_item);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        edtSearch = findViewById(R.id.edt_search);
        tvName = findViewById(R.id.tv_name);
        user = findViewById(R.id.btn_logout);
        button_add = findViewById(R.id.btn_add);
    }

    private void loadData() {

        reference.child(SUBJECT.getId()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entityList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()
                ) {
                    Entity entity = snapshot.getValue(Entity.class);
                    entityList.add(entity);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
package com.thpttranquangkhai.arreveal.Activity.Subject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.Objects;

public class SubjectActivity extends AppCompatActivity {
    ImageView btnInfo;
    TextView tvSchoolName;
    FloatingActionButton btnAdd;
    RecyclerView rcvSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        initView();
        checkAdmin();
    }

    private void checkAdmin() {
        if (!Objects.equals(Constants.ACCOUNT.getId(), Constants.SCHOOL.getIdAccount())) {
            btnAdd.setVisibility(View.GONE);
        }
    }

    private void initView() {
        btnInfo = findViewById(R.id.btn_info);
        tvSchoolName = findViewById(R.id.tv_name);
        btnAdd = findViewById(R.id.btn_add);
        rcvSubject = findViewById(R.id.rcv_item);
    }
}
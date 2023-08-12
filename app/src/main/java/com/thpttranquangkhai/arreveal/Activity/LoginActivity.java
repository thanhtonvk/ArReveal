package com.thpttranquangkhai.arreveal.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.thpttranquangkhai.arreveal.R;

public class LoginActivity extends AppCompatActivity {

    EditText edt_email, edt_password;
    Button btnLogin;
    FirebaseAuth mAuth;
    TextView btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        onClick();
    }

    private void onClick() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
                dialog.setTitle("Đang đăng nhập");

                String email = edt_email.getText().toString();
                String pass = edt_password.getText().toString();
                if (email.equals("") || pass.equals("")) {
                    Toast.makeText(getApplicationContext(), "Thông tin không được để trống", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.e("TAG", "onComplete OK" );
                                startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
                                dialog.dismiss();
                            } else {
                                Log.e("TAG", "Failed" );
                                Toast.makeText(getApplicationContext(), "Tài khoản hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("TAG", "onFailure: " + e.toString());
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });
    }

    private void initView() {
        edt_email = findViewById(R.id.edt_username);
        edt_password = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        mAuth = FirebaseAuth.getInstance();
        btnRegister = findViewById(R.id.btn_register);
    }

}
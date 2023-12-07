package com.thpttranquangkhai.arreveal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thpttranquangkhai.arreveal.Activity.LoginActivity;
import com.thpttranquangkhai.arreveal.Activity.MainPageActivity;
import com.thpttranquangkhai.arreveal.Activity.PlayYoutubeActivity;
import com.thpttranquangkhai.arreveal.Models.Account;
import com.thpttranquangkhai.arreveal.Utilities.Constants;
import com.thpttranquangkhai.arreveal.Utilities.FileUtils;

public class MainActivity extends AppCompatActivity {
    public static FileUtils fileUtils;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileUtils = new FileUtils(this);
        mAuth = FirebaseAuth.getInstance();
    }

    private void getAccountById(String id) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Account");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Constants.ACCOUNT = dataSnapshot.getValue(Account.class);
                Log.w("TAG", "get account successfully" + dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        };
        reference.child(id).addValueEventListener(postListener);
    }

    private void getThreshold() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("threshold");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Constants.Threshold = Float.parseFloat(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getThreshold();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else {
            getAccountById(currentUser.getUid());
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
        }
    }
}


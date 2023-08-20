package com.thpttranquangkhai.arreveal.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thpttranquangkhai.arreveal.Adapter.UserAdapter;
import com.thpttranquangkhai.arreveal.Models.Account;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity {

    RecyclerView rcvItem;
    UserAdapter userAdapter;
    List<Account> accountList = new ArrayList<>();
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        initView();
        loadData();
    }

    private void initView() {
        rcvItem = findViewById(R.id.rcv_item);
        userAdapter = new UserAdapter(AccountActivity.this, accountList);
        rcvItem.setAdapter(userAdapter);
        database = FirebaseDatabase.getInstance();
    }

    private void loadData() {
        reference = database.getReference("JoinedSchool");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String idAccount = dataSnapshot.getKey();
                    Log.e("IDACCOUNT", "onDataChange: " + idAccount);
                    checkKeyExist(idAccount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkKeyExist(String idAccount) {
        reference = database.getReference("JoinedSchool").child(idAccount);
        reference.child(Constants.SCHOOL.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    getAccountById(idAccount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAccountById(String id) {
        reference = database.getReference("Account");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                accountList.add(dataSnapshot.getValue(Account.class));
                userAdapter.notifyDataSetChanged();
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
}
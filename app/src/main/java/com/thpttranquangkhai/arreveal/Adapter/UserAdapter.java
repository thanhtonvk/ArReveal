package com.thpttranquangkhai.arreveal.Adapter;


import static com.thpttranquangkhai.arreveal.Utilities.Constants.GRADE;
import static com.thpttranquangkhai.arreveal.Utilities.Constants.SCHOOL;
import static com.thpttranquangkhai.arreveal.Utilities.Constants.SUBJECT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thpttranquangkhai.arreveal.Models.Account;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.List;
import java.util.Random;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    Context context;
    List<Account> accountList;
    FirebaseDatabase database;
    DatabaseReference reference;
    Random random;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;

    public UserAdapter(Context context, List<Account> accountList) {
        this.context = context;
        this.accountList = accountList;
        database = FirebaseDatabase.getInstance();
        random = new Random();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Account account = accountList.get(position);
        int color = random.nextInt(Constants.colors.length - 1);
        holder.tvFullName.setTextColor(Constants.colors[color]);
        holder.tvFirstName.setTextColor(Constants.colors[color]);
        holder.tvFullName.setText(account.getName());
        holder.tvFirstName.setText(String.valueOf(account.getName().toCharArray()[0]));
        holder.tvEmail.setText(String.valueOf(account.getEmail()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog(account, position);
            }
        });
    }

    private void dialog(Account account, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bạn muốn cấp quyền chỉnh sửa cho tài khoản này?");
        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (firebaseUser.getUid().equals(account.getId())) {
                    Toast.makeText(context, "Bạn không thể cấp chính bạn", Toast.LENGTH_SHORT).show();
                } else {
                    acceptRole(account.getId());
                }

            }
        });
        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void acceptRole(String idAcc) {
        reference = database.getReference("Subject").child(SCHOOL.getId()).child(GRADE.getId()).child(SUBJECT.getId());
        SUBJECT.setIdTeacher(idAcc);
        reference.setValue(SUBJECT).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Cấp quyền thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Cấp quyền không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvFirstName, tvEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvFirstName = itemView.findViewById(R.id.tv_first_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
        }
    }
}

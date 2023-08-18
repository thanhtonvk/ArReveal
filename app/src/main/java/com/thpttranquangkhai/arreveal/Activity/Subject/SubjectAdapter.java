package com.thpttranquangkhai.arreveal.Activity.Subject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thpttranquangkhai.arreveal.Activity.UserActivity;
import com.thpttranquangkhai.arreveal.Models.Grade;
import com.thpttranquangkhai.arreveal.Models.Subject;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.List;
import java.util.Objects;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {


    FirebaseDatabase database;
    DatabaseReference reference;
    Context context;
    List<Subject> subjectList;

    public SubjectAdapter(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
        this.database = FirebaseDatabase.getInstance();
        this.reference = this.database.getReference("Subject");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        Glide.with(context).load(subject.getImage()).into(holder.imgLogo);
        holder.tvSubjectName.setText(subject.getName());
        holder.tvDetails.setText(subject.getDetail());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (Objects.equals(Constants.ACCOUNT.getId(), Constants.SCHOOL.getIdAccount())) {
                    dialog(subject);
                }
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.SUBJECT = subject;
                context.startActivity(new Intent(context, UserActivity.class));
            }
        });
    }

    private void dialog(Subject subject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bạn muốn làm gì?");
        builder.setPositiveButton("Xóa lớp", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                reference.child(Constants.SCHOOL.getId()).child(Constants.GRADE.getId()).child(subject.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            notifyDataSetChanged();
                            Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        } else {
                            Toast.makeText(context, "Xóa không thành công", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();


    }


    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView tvSubjectName, tvDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.img_image);
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
            tvDetails = itemView.findViewById(R.id.tv_details);
        }
    }
}

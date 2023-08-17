package com.thpttranquangkhai.arreveal.Activity.Classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thpttranquangkhai.arreveal.Activity.Subject.SubjectActivity;
import com.thpttranquangkhai.arreveal.Activity.UserActivity;
import com.thpttranquangkhai.arreveal.Models.Grade;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.List;
import java.util.Random;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.ViewHolder> {
    Context context;
    List<Grade> gradeList;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    FirebaseDatabase database;
    Random random;

    public GradeAdapter(Context context, List<Grade> gradeList) {
        this.context = context;
        this.gradeList = gradeList;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Grade").child(Constants.SCHOOL.getId());
        random = new Random();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new GradeAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_class, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int color = random.nextInt(Constants.colors.length - 1);
        holder.layout.setBackgroundColor(Constants.colors[color]);
        Grade grade = gradeList.get(position);
        holder.tvGrade.setText(String.valueOf(grade.getName()));
        holder.tvGradeName.setText("Khối " + grade.getName());
        if (!Constants.SCHOOL.getIdAccount().equals(firebaseUser.getUid())) {
            holder.btnMore.setVisibility(View.GONE);
        }
        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog(grade, position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.GRADE = grade;
                context.startActivity(new Intent(context, SubjectActivity.class));
            }
        });
    }

    private void dialog(Grade grade, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bạn muốn làm gì?");
        builder.setPositiveButton("Xóa khối", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                reference.child(grade.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
        return gradeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvGradeName, tvGrade;
        ImageButton btnMore;
        RelativeLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGradeName = itemView.findViewById(R.id.tv_class_name);
            tvGrade = itemView.findViewById(R.id.tv_class);
            btnMore = itemView.findViewById(R.id.btn_more);
            layout = itemView.findViewById(R.id.layout_color);
        }
    }
}

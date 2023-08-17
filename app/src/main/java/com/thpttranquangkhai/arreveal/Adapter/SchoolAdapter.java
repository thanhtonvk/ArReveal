package com.thpttranquangkhai.arreveal.Adapter;

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
import com.thpttranquangkhai.arreveal.Activity.Classes.GradeActivity;
import com.thpttranquangkhai.arreveal.Activity.UserActivity;
import com.thpttranquangkhai.arreveal.Models.School;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.List;
import java.util.Random;

public class SchoolAdapter extends RecyclerView.Adapter<SchoolAdapter.ViewHolder> {
    Context context;
    List<School> schoolsList;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    FirebaseDatabase database;
    Random random;

    public SchoolAdapter(Context context, List<School> schoolsList) {
        this.context = context;
        this.schoolsList = schoolsList;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        random = new Random();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new SchoolAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_school, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int color = random.nextInt(Constants.colors.length - 1);
        holder.layout.setBackgroundColor(Constants.colors[color]);
        School school = schoolsList.get(position);
        holder.tvIdSchool.setText(school.getId());
        holder.tvSchoolName.setText(school.getName());
        Glide.with(context).load(school.getAvatar()).into(holder.imgLogo);

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog(school, position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.SCHOOL = school;
                context.startActivity(new Intent(context, GradeActivity.class));
            }
        });
    }

    private void dialog(School school, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bạn muốn làm gì?");
        if (school.getIdAccount().equals(firebaseUser.getUid())) {
            builder.setPositiveButton("Xóa thông tin trường", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    reference = database.getReference("School");
                    reference.child(school.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                reference = database.getReference("JoinedSchool");
                                reference.child(firebaseUser.getUid()).child(school.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                            } else {
                                Toast.makeText(context, "Xóa không thành công", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        }
                    });
                }
            });
        } else {
            builder.setPositiveButton("Rời trường", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    reference = database.getReference("JoinedSchool");
                    reference.child(firebaseUser.getUid()).child(school.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Rời thành công", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                                dialogInterface.dismiss();
                            } else {
                                Toast.makeText(context, "Rời không thành công", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        }
                    });
                }
            });
        }
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
        return schoolsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSchoolName, tvIdSchool;
        ImageView imgLogo;
        ImageButton btnMore;
        RelativeLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSchoolName = itemView.findViewById(R.id.tv_school_name);
            tvIdSchool = itemView.findViewById(R.id.tv_id_school);
            imgLogo = itemView.findViewById(R.id.img_logo);
            btnMore = itemView.findViewById(R.id.btn_more);
            layout = itemView.findViewById(R.id.layout_color);
        }
    }
}

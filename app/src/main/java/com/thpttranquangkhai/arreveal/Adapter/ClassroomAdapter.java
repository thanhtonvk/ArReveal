package com.thpttranquangkhai.arreveal.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
import com.thpttranquangkhai.arreveal.Activity.UserActivity;
import com.thpttranquangkhai.arreveal.Models.JoinedClassroom;
import com.thpttranquangkhai.arreveal.R;
import com.thpttranquangkhai.arreveal.Utilities.Constants;

import java.util.List;
import java.util.Random;

public class ClassroomAdapter extends RecyclerView.Adapter<ClassroomAdapter.ViewHolder> {
    Context context;
    List<JoinedClassroom> classroomList;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    FirebaseDatabase database;
    Random random;

    public ClassroomAdapter(Context context, List<JoinedClassroom> classroomList) {
        this.context = context;
        this.classroomList = classroomList;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        random = new Random();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ClassroomAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_class, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int color = random.nextInt(Constants.colors.length - 1);


        holder.layout.setBackgroundColor(Constants.colors[color]);
        holder.tvFirstName.setTextColor(Constants.colors[color]);


        JoinedClassroom classroom = classroomList.get(position);
        holder.tvHostName.setText(classroom.getHostName());
        holder.tvClassName.setText(classroom.getName());
        holder.tvFirstName.setText((classroom.getHostName().toCharArray()[0] + "").toUpperCase());
        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog(classroom,position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.idClassroom = classroom.getId();
                Constants.idHost = classroom.getIdHost();
                Constants.className = classroom.getName();
                context.startActivity(new Intent(context, UserActivity.class));
            }
        });
    }

    private void dialog(JoinedClassroom joinedClassroom,int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bạn muốn làm gì?");
        if (joinedClassroom.getIdHost().equals(firebaseUser.getUid())) {
            builder.setPositiveButton("Xóa lớp học", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    reference = database.getReference("Classroom");
                    reference.child(joinedClassroom.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                reference = database.getReference("Joined");
                                reference.child(firebaseUser.getUid()).child(joinedClassroom.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            classroomList.remove(position);
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
            builder.setPositiveButton("Rời lớp học", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    reference = database.getReference("Joined");
                    reference.child(firebaseUser.getUid()).child(joinedClassroom.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
        return classroomList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvClassName, tvHostName, tvFirstName;
        ImageButton btnMore;
        RelativeLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tv_class_name);
            tvHostName = itemView.findViewById(R.id.tv_host_name);
            tvFirstName = itemView.findViewById(R.id.tv_first_name);
            btnMore = itemView.findViewById(R.id.btn_more);
            layout = itemView.findViewById(R.id.layout_color);
        }
    }
}

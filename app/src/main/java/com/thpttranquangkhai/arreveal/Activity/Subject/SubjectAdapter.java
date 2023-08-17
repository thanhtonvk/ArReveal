package com.thpttranquangkhai.arreveal.Activity.Subject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thpttranquangkhai.arreveal.Models.Subject;
import com.thpttranquangkhai.arreveal.R;

import java.util.List;

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

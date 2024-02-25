package com.example.mimanhwa.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mimanhwa.Activities.GeneralActivity.BookInfoActivity;
import com.example.mimanhwa.Activities.GeneralActivity.ReadBookActivity;
import com.example.mimanhwa.Extensions.MyApplication;
import com.example.mimanhwa.Models.ModelChapter;
import com.example.mimanhwa.R;
import com.example.mimanhwa.databinding.RowChapterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterChapter extends RecyclerView.Adapter<AdapterChapter.HolderChapter> {

    private static final String TAG = "CHAPTER_MANAGEMENT_TAG";

    private Context context;
    private ArrayList<ModelChapter> chapterArrayList;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    public AdapterChapter(Context context, ArrayList<ModelChapter> chapterArrayList) {
        this.context = context;
        this.chapterArrayList = chapterArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Por favor esperen");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderChapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowChapterBinding binding = RowChapterBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderChapter(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChapter holder, int position) {
        ModelChapter model = chapterArrayList.get(position);
        String title = model.getTitle();
        String chapterId = model.getId();
        String bookId = model.getBookId();

        holder.chapterTextView.setText(title);

        checkChapterReaded(chapterId, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle sendDates = new Bundle();
                sendDates.putString("chapterId", chapterId);
                sendDates.putString("bookId", bookId);

                Intent intent = new Intent(context, ReadBookActivity.class);
                intent.putExtras(sendDates);
                context.startActivity(intent);
            }
        });

        holder.viewChapterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.isReaded) {
                    MyApplication.removeFromReaded(context, chapterId);
                    holder.viewChapterBtn.setImageResource(R.drawable.ic_not_readed);
                } else {
                    MyApplication.addToReadedChapter(context, chapterId);
                    holder.viewChapterBtn.setImageResource(R.drawable.ic_readed);
                }
                holder.isReaded = !holder.isReaded;
            }
        });
    }

    private void checkChapterReaded(String chapterId, HolderChapter holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("ReadedChapters").child(chapterId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.isReaded = snapshot.exists();
                if (holder.isReaded) {
                    holder.viewChapterBtn.setImageResource(R.drawable.ic_readed);
                } else {
                    holder.viewChapterBtn.setImageResource(R.drawable.ic_not_readed);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking chapter read status: " + error.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapterArrayList.size();
    }

    static class HolderChapter extends RecyclerView.ViewHolder {
        TextView chapterTextView;
        ImageButton viewChapterBtn;
        boolean isReaded;

        public HolderChapter(RowChapterBinding binding) {
            super(binding.getRoot());
            chapterTextView = binding.chapterTitleTvTv;
            viewChapterBtn = binding.chapterMarkBtn;
        }
    }
}

package com.example.mimanhwa.Adapters;

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

import com.bumptech.glide.Glide;
import com.example.mimanhwa.Activities.EditActivity.EditProfileActivity;
import com.example.mimanhwa.Models.ModelComment;
import com.example.mimanhwa.R;
import com.example.mimanhwa.databinding.RowCommentBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment>{

    //context
    private Context context;

    private RowCommentBinding binding;

    private ArrayList<ModelComment> commentArrayList;

    private FirebaseAuth firebaseAuth;

    //constructor

    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate binding
        binding = RowCommentBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {

        //get data
        ModelComment modelComment = commentArrayList.get(position);
        String id = modelComment.getId();
        String bookId = modelComment.getBookId();
        String comment = modelComment.getComment();
        String uid = modelComment.getUid();
        String timestamp = modelComment.getTimestamp();

        //set data
        holder.commentTv.setText(comment);

        //loas profile picture and name of user
        loadUserDetails(modelComment,holder);

        //hable click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uid.equals(firebaseAuth.getUid())){
                    deleteComment(modelComment,holder);
                }
            }
        });
    }

    private void deleteComment(ModelComment modelComment, HolderComment holder) {

        //show dialog obout deleting
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Borrar comentario").setMessage("¿Esta seguro?").setPositiveButton("BORRAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(modelComment.getBookId()).child("Comments").child(modelComment.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Toast.makeText(context, "El comentario se borro..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, "Ocurrio un error al borrar comentario" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).setNegativeButton("CACELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cancele clicked
                dialog.dismiss();
            }
        }).show();
    }

    private void loadUserDetails(ModelComment modelComment, HolderComment holder) {

        String uid = modelComment.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = ""+snapshot.child("name").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();

                //set data
                holder.nameTv.setText(name);


                // Cargar la imagen usando Glide
                Glide.with(context)
                        .load(profileImage)  // El URL de la imagen
                        .placeholder(R.drawable.ic_person_white)
                        .into(holder.profileIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return commentArrayList.size(); //return records size,number of records
    }


    //View holder for row_comments.xml

    class HolderComment extends RecyclerView.ViewHolder {

        ShapeableImageView profileIv;
        TextView nameTv,commentTv;
        public HolderComment(@NonNull View itemView) {
            super(itemView);

            //init ui views
            profileIv = binding.comProfileIV;
            nameTv = binding.nameTv;
            commentTv = binding.commentTv;

        }
    }
}

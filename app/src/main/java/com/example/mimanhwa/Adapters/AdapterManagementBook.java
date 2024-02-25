package com.example.mimanhwa.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mimanhwa.Activities.AddActivity.AddBookActivity;
import com.example.mimanhwa.Activities.GeneralActivity.BookInfoActivity;
import com.example.mimanhwa.Activities.ManagementActivity.ManagementChapterActivity;
import com.example.mimanhwa.Extensions.MyApplication;
import com.example.mimanhwa.Filters.FilterManagementBook;
import com.example.mimanhwa.Models.ModelBook;
import com.example.mimanhwa.databinding.RowManagementBookBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterManagementBook extends RecyclerView.Adapter<AdapterManagementBook.HolderBookManagment> implements Filterable {

    private static final String TAG ="BOOK_MANAGEMENT_TAG";

    //context
    private Context context;

    //View binding row_book_managment
    private RowManagementBookBinding binding;

    //arraylist to hold list of data

    public ArrayList<ModelBook> bookArrayList,filterList;

    private FilterManagementBook filter;

    //progress dialog
    private ProgressDialog progressDialog;

    public AdapterManagementBook(Context context, ArrayList<ModelBook> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.filterList = bookArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Porfavor esperen");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderBookManagment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowManagementBookBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderBookManagment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBookManagment holder, int position) {

        //get data , set data , handle clicks

        //get data
        ModelBook model = bookArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        String imgUrl = model.getUrl();
        String genreID = model.getGenreId();

        //set data

        holder.bookManagementTitleTV.setText(title);
        holder.bookManagementDescriptionTV.setText(description);


        loadCategory(model,holder);

        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(imgUrl)  // El URL de la imagen
                .into(holder.frontPageImgView);

        //Handeñ click, show dialog wich options
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model,holder);
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bookId = model.getId();

                Bundle sendDates = new Bundle();
                sendDates.putString("bookId",bookId);

                Intent intent = new Intent(context, ManagementChapterActivity.class);
                //send bundle
                intent.putExtras(sendDates);

                context.startActivity(intent);
            }
        });

    }

    private void moreOptionsDialog(ModelBook model, HolderBookManagment holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        //options to show in dialog
        String[] options = {"Editar","Borrar"};

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eligir opción").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog options click
                if(which == 0){
                    //Edit clicked
                    Bundle sendDates = new Bundle();
                    sendDates.putString("stateActivity","edit");
                    sendDates.putString("bookId",bookId);

                    Intent intent = new Intent(context, AddBookActivity.class);
                    //send bundle
                    intent.putExtras(sendDates);

                    context.startActivity(intent);

                }else if(which == 1){

                    //Delete cliked
                    deleteBook(model,holder);
                }

            }
        }).show();
    }

    private void deleteBook(ModelBook model, HolderBookManagment holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        MyApplication.removeFromAllFavorites(context, bookId);

        Log.d(TAG,"borando lectura: Borando...");
        progressDialog.setMessage("Borando " +bookTitle+"...");
        progressDialog.show();

        Log.d(TAG,"borando lectura: Borando desde storage...");


        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Log.d(TAG,"onSucces: Borado desde storage");
                Log.d(TAG,"onSucces: Borando desde DB");

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");

                reference.child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG,"onSucces: Borrado desde data base");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Lectura borrada...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: Fallo al borrar desde db" + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d(TAG,"onFailure: Borrado desde storage fallido" + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void loadCategory(ModelBook model, HolderBookManagment holder) {

        String genreId = model.getGenreId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Genres");
        ref.child(genreId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String genre = ""+snapshot.child("genre").getValue();

                //set to genre text view
                holder.bookManagementGenreTV.setText(genre);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return bookArrayList.size(); //return number of records | list size
    }

    @Override
    public Filter getFilter() {

        if(filter ==null){

            filter = new FilterManagementBook(filterList,this);
        }

        return filter;
    }

//Viewb Holder class for row_book_managment

    class HolderBookManagment extends RecyclerView.ViewHolder{

        //Ui Views row_book_managment
        ImageView frontPageImgView;
        TextView bookManagementTitleTV,bookManagementDescriptionTV,bookManagementGenreTV;
        ImageButton moreBtn;

        public HolderBookManagment(@NonNull View itemView) {
            super(itemView);

            frontPageImgView = binding.managementBookFrontpageImgView;
            bookManagementTitleTV = binding.managementBookTitleTv;
            bookManagementDescriptionTV = binding.managementBookDescriptionTv;
            bookManagementGenreTV = binding.managementBookGenreTv;

            moreBtn = binding.managementBookMoreBtn;
        }
    }


}
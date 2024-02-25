package com.example.mimanhwa.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mimanhwa.Activities.GeneralActivity.BookInfoActivity;
import com.example.mimanhwa.Models.ModelBook;
import com.example.mimanhwa.databinding.RowPopularBookBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPopularBook extends RecyclerView.Adapter<AdapterPopularBook.HolderPopularBook>{

    private static final String TAG ="BOOK_POPULAR_TAG";

    //context
    private Context context;

    //View binding row_book_popular
    private RowPopularBookBinding binding;

    //arraylist to hold list of data

    public ArrayList<ModelBook> bookArrayList,filterList;


    //progress dialog
    private ProgressDialog progressDialog;

    // Referencia a la base de datos Firebase
    private DatabaseReference mDatabase;

    public AdapterPopularBook(Context context, ArrayList<ModelBook> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.filterList = bookArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Porfavor esperen");
        progressDialog.setCanceledOnTouchOutside(false);

        // Inicializar la referencia a la base de datos Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public HolderPopularBook onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowPopularBookBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPopularBook(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPopularBook holder, int position) {

        //get data , set data , handle clicks

        //get data
        ModelBook model = bookArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        String imgUrl = model.getUrl();
        String genreID = model.getGenreId();

        //set data

        holder.bookManagementTitleTV.setText(title);


        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(imgUrl)  // El URL de la imagen
                .into(holder.frontPageImgView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bookId = model.getId();

                increaseBookViews(bookId);

                Bundle sendDates = new Bundle();
                sendDates.putString("bookId",bookId);

                Intent intent = new Intent(context, BookInfoActivity.class);
                //send bundle
                intent.putExtras(sendDates);

                context.startActivity(intent);
            }
        });

    }


    // Método para incrementar las vistas del libro y guardarlo en Firebase
    private void increaseBookViews(String bookId) {
        // Obtener una referencia específica al libro en la base de datos
        DatabaseReference bookRef = mDatabase.child("Books").child(bookId).child("viewsCount");

        // Obtener el valor actual del contador de vistas
        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long currentViews = (long) dataSnapshot.getValue();

                    // Incrementar el contador de vistas
                    currentViews++;

                    // Actualizar el contador de vistas en Firebase
                    bookRef.setValue(currentViews);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores si los hay
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookArrayList.size(); //return number of records | list size
    }


//Viewb Holder class for row_book_managment

    class HolderPopularBook extends RecyclerView.ViewHolder{

        //Ui Views row_book_popular
        ImageView frontPageImgView;
        TextView bookManagementTitleTV;

        public HolderPopularBook(@NonNull View itemView) {
            super(itemView);

            frontPageImgView = binding.popularBookImg;
            bookManagementTitleTV = binding.popularBookNameTv;

        }
    }
}
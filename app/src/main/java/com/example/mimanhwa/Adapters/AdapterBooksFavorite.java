package com.example.mimanhwa.Adapters;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mimanhwa.Activities.GeneralActivity.BookInfoActivity;
import com.example.mimanhwa.Activities.GeneralActivity.ChaptersActivity;
import com.example.mimanhwa.Extensions.MyApplication;
import com.example.mimanhwa.Filters.FilterBook;
import com.example.mimanhwa.Filters.FilterFavoriteBook;
import com.example.mimanhwa.Models.ModelBook;
import com.example.mimanhwa.R;
import com.example.mimanhwa.databinding.RowFavoriteBookBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterBooksFavorite extends RecyclerView.Adapter<AdapterBooksFavorite.HolderBookFavorite> implements Filterable {

    private Context context;
    //arraylist to hold list of data

    public ArrayList<ModelBook> bookArrayList,filterList;

    private RowFavoriteBookBinding binding;

    private FilterFavoriteBook filter;

    private static final String TAG = "FAV_BOOK_TAG";

    public AdapterBooksFavorite(Context context, ArrayList<ModelBook> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.filterList = bookArrayList;
    }

    @NonNull
    @Override
    public HolderBookFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowFavoriteBookBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderBookFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBookFavorite holder, int position) {

        ModelBook model = bookArrayList.get(position);

        loadBookDetails(model,holder);

        takeChaptersNumber(model.getId(), holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String bookId = model.getId();

                Bundle sendDates = new Bundle();
                sendDates.putString("bookId",bookId);

                Intent intent = new Intent(context, BookInfoActivity.class);
                //send bundle
                intent.putExtras(sendDates);

                context.startActivity(intent);

            }
        });

        holder.deleteFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.removeFromFavorite(context,model.getId());
            }
        });

        holder.currentChapterTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle sendDates = new Bundle();
                sendDates.putString("bookId",model.getId());

                Intent intent = new Intent(context, ChaptersActivity.class);
                //send bundle
                intent.putExtras(sendDates);

                context.startActivity(intent);
            }
        });

    }

    private void takeChaptersNumber(String bookId,HolderBookFavorite holder) {
        // Referencia a la base de datos
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chapters");

        // Query para filtrar los capítulos por el ID del libro
        Query query = ref.orderByChild("bookId").equalTo(bookId);

        // Agregar un listener para escuchar los resultados de la consulta
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Obtener el número de capítulos
                long chaptersNumber = dataSnapshot.getChildrenCount();

            holder.numberChaptersText.setText("Capitulos subidos: "+chaptersNumber);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores si la consulta es cancelada
                Log.e(TAG, "Error al obtener el número de capítulos: " + databaseError.getMessage());
            }
        });
    }

    private void loadBookDetails(ModelBook model, HolderBookFavorite holder) {

        String bookId = model.getId();
        Log.d(TAG,"loadBookDetails: Detalles de lectura con id" + bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String bookTitle = ""+snapshot.child("title").getValue();
                String description = ""+snapshot.child("description").getValue();
                String categoryId = ""+snapshot.child("categoryId").getValue();
                String frontPageUrl = ""+snapshot.child("url").getValue();
                String timestamp = ""+snapshot.child("timestamp").getValue();
                String uid = ""+snapshot.child("uid").getValue();

                //set data to model

                model.setFavorite(true);
                model.setTitle(bookTitle);
                model.setDescription(description);
                model.setGenreId(categoryId);
                model.setUrl(frontPageUrl);
                model.setTimestamp(Long.parseLong(timestamp));
                model.setUid(uid);

                //set data to views

                holder.titleText.setText(bookTitle);


                loadBookFrontPage(model,holder);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void loadBookFrontPage(ModelBook model, AdapterBooksFavorite.HolderBookFavorite holder) {

        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(model.getUrl())  // El URL de la imagen
                .placeholder(R.drawable.ic_person)
                .into(holder.frontPage);
    }


    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }


    @Override
    public Filter getFilter() {

        if(filter ==null){

            filter = new FilterFavoriteBook(filterList,this);
        }

        return filter;
    }

    class HolderBookFavorite extends RecyclerView.ViewHolder{

        //View items
       ImageView frontPage;
       TextView titleText;
       TextView numberChaptersText;
       TextView currentChapterTextButton;

       ImageButton deleteFavorite;


        public HolderBookFavorite(@NonNull View itemView) {
            super(itemView);

            frontPage = binding.favoriteBookFrontpageImgView;
            titleText = binding.favoriteBookTitleTv;
            numberChaptersText = binding.favoriteBookNumChaptersTv;
            currentChapterTextButton = binding.favoriteBookCurrentChapterTv;
            deleteFavorite = binding.favoriteDeleteBtn;

        }
    }
}
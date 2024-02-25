package com.example.mimanhwa.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mimanhwa.Activities.GeneralActivity.BookInfoActivity;
import com.example.mimanhwa.Filters.FilterGenreBooks;
import com.example.mimanhwa.Models.ModelBook;
import com.example.mimanhwa.databinding.RowGenreBookBinding;

import java.util.ArrayList;

public class AdapterGenreBooks extends RecyclerView.Adapter<AdapterGenreBooks.HolderGenreBooks> implements Filterable {

    private static final String TAG ="BOOK_TAG";

    //context
    private Context context;

    //View binding row_book_managment
    private RowGenreBookBinding binding;

    //arraylist to hold list of data

    public ArrayList<ModelBook> bookArrayList,filterList;

    private FilterGenreBooks filter;

    //progress dialog
    private ProgressDialog progressDialog;

    public AdapterGenreBooks(Context context, ArrayList<ModelBook> bookArrayList) {
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
    public HolderGenreBooks onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowGenreBookBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderGenreBooks(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGenreBooks holder, int position) {

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


        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(imgUrl)  // El URL de la imagen
                .into(holder.frontPageImgView);


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

    }


    @Override
    public int getItemCount() {
        return bookArrayList.size(); //return number of records | list size
    }

    @Override
    public Filter getFilter() {

        if(filter ==null){

            filter = new FilterGenreBooks(filterList,this);
        }

        return filter;
    }

//Viewb Holder class for row_book_managment

    class HolderGenreBooks extends RecyclerView.ViewHolder{

        //Ui Views row_book_managment
        ImageView frontPageImgView;
        TextView bookManagementTitleTV,bookManagementDescriptionTV;

        public HolderGenreBooks(@NonNull View itemView) {
            super(itemView);

            frontPageImgView = binding.genreBookFrontpageImgView;
            bookManagementTitleTV = binding.genreBookTitleTv;
            bookManagementDescriptionTV = binding.genreBookDescriptionTv;

        }
    }
}
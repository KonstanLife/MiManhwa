package com.example.mimanhwa.Adapters;

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
import com.example.mimanhwa.Activities.GeneralActivity.GenreBooksActivity;
import com.example.mimanhwa.Filters.FilterGenre;
import com.example.mimanhwa.Filters.FilterManagementGenre;
import com.example.mimanhwa.Models.ModelGenre;
import com.example.mimanhwa.databinding.RowGenreBinding;

import java.util.ArrayList;

public class AdapterGenre extends RecyclerView.Adapter<AdapterGenre.HolderGenre> implements Filterable {

    private Context context;
    public ArrayList<ModelGenre> genreArrayList,filterList;

    //view binding
    private RowGenreBinding binding;

    private FilterGenre filter;

    private static final String TAG = "ADD_IMG_TAG";

    public AdapterGenre(Context context, ArrayList<ModelGenre> genreArrayList){

        this.context = context;
        this.genreArrayList = genreArrayList;
        this.filterList = genreArrayList;
    }

    @NonNull
    @Override
    public AdapterGenre.HolderGenre onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //bind row_category.xml
        binding = RowGenreBinding.inflate(LayoutInflater.from(context),parent,false);

        return new AdapterGenre.HolderGenre(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterGenre.HolderGenre holder, int position) {

        //get data
        ModelGenre model = genreArrayList.get(position);
        String id = model.getId();
        String genre = model.getGenre();
        String uid = model.getUid();
        String url = model.getUrl();
        String dirImg = model.getDirImg();
        long timestamp = model.getTimestamp();


        //set data
        holder.genreTitleTextView.setText(genre);

        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(url)  // El URL de la imagen
                .into(holder.genreImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String genreId = model.getId();

                Bundle sendDates = new Bundle();
                sendDates.putString("genreId",genreId);

                Intent intent = new Intent(context, GenreBooksActivity.class);
                //send bundle
                intent.putExtras(sendDates);

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return genreArrayList.size();
    }

    @Override
    public Filter getFilter() {

        if(filter == null){

            filter = new FilterGenre(filterList,this);
        }

        return filter;
    }



    class HolderGenre extends RecyclerView.ViewHolder{

        //ui views of row_category
        TextView genreTitleTextView;
        ImageView genreImageView;

        public HolderGenre(@NonNull View itemView){

            super(itemView);
            genreTitleTextView = binding.userNameTv;
            genreImageView = binding.genreImg;
        }

    }
}

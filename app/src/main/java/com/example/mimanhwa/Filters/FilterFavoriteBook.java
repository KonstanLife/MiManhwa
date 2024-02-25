package com.example.mimanhwa.Filters;

import android.widget.Filter;

import com.example.mimanhwa.Adapters.AdapterBook;
import com.example.mimanhwa.Adapters.AdapterBooksFavorite;
import com.example.mimanhwa.Models.ModelBook;

import java.util.ArrayList;

public class FilterFavoriteBook extends Filter {

    //array list which search content
    ArrayList<ModelBook> filterList;
    //adapter in which filter
    AdapterBooksFavorite adapterBooksFavorite;


    //constructor
    public FilterFavoriteBook(ArrayList<ModelBook> filterList, AdapterBooksFavorite adapterBooksFavorite){

        this.filterList = filterList;
        this.adapterBooksFavorite = adapterBooksFavorite;
    }


    @Override
    protected Filter.FilterResults performFiltering(CharSequence constraint) {
        Filter.FilterResults results = new Filter.FilterResults();

        if(constraint != null && constraint.length() > 0){

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelBook> filteredModels = new ArrayList<>();

            for(int i = 0; i<filterList.size(); i++){

                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }

            }

            results.count = filteredModels.size();
            results.values = filteredModels;

        }else{
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, Filter.FilterResults results) {

        adapterBooksFavorite.bookArrayList = (ArrayList<ModelBook>)results.values;

        adapterBooksFavorite.notifyDataSetChanged();

    }
}
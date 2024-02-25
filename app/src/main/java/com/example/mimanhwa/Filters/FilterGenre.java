package com.example.mimanhwa.Filters;

import android.widget.Filter;

import com.example.mimanhwa.Adapters.AdapterGenre;
import com.example.mimanhwa.Adapters.AdapterManagementGenre;
import com.example.mimanhwa.Models.ModelGenre;

import java.util.ArrayList;

public class FilterGenre extends Filter{
    //array list which search content
    ArrayList<ModelGenre> filterList;

    //adapter in which filter
    AdapterGenre adapterGenre;

    //constructor
    public FilterGenre(ArrayList<ModelGenre> filterList, AdapterGenre adapterGenre){

        this.filterList = filterList;
        this.adapterGenre = adapterGenre;
    }


    @Override
    protected Filter.FilterResults performFiltering(CharSequence constraint) {
        Filter.FilterResults results = new Filter.FilterResults();

        if(constraint != null && constraint.length() > 0){

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelGenre> filteredModels = new ArrayList<>();

            for(int i = 0; i<filterList.size(); i++){

                if(filterList.get(i).getGenre().toUpperCase().contains(constraint)){
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

        adapterGenre.genreArrayList = (ArrayList<ModelGenre>)results.values;

        adapterGenre.notifyDataSetChanged();

    }
}

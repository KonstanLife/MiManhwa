package com.example.mimanhwa.Filters;

import android.widget.Filter;

import com.example.mimanhwa.Adapters.AdapterManagementGenre;
import com.example.mimanhwa.Models.ModelGenre;

import java.util.ArrayList;

public class FilterManagementGenre extends Filter{

    //array list which search content
    ArrayList<ModelGenre> filterList;

    //adapter in which filter
    AdapterManagementGenre adapterManagementGenre;

    //constructor
    public FilterManagementGenre(ArrayList<ModelGenre> filterList, AdapterManagementGenre adapterManagementGenre){

        this.filterList = filterList;
        this.adapterManagementGenre = adapterManagementGenre;
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

        adapterManagementGenre.genreArrayList = (ArrayList<ModelGenre>)results.values;

        adapterManagementGenre.notifyDataSetChanged();

    }
}

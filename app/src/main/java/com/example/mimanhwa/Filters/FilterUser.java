package com.example.mimanhwa.Filters;

import android.widget.Filter;

import com.example.mimanhwa.Adapters.AdapterUsers;
import com.example.mimanhwa.Models.ModelUser;

import java.util.ArrayList;

public class FilterUser extends Filter{

    //array list which search content
    ArrayList<ModelUser> filterList;

    //adapter in which filter
    AdapterUsers adapterUsers;

    //constructor
    public FilterUser(ArrayList<ModelUser> filterList, AdapterUsers adapterUsers){

        this.filterList = filterList;
        this.adapterUsers = adapterUsers;
    }


    @Override
    protected Filter.FilterResults performFiltering(CharSequence constraint) {
        Filter.FilterResults results = new Filter.FilterResults();

        if(constraint != null && constraint.length() > 0){

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelUser> filteredModels = new ArrayList<>();

            for(int i = 0; i<filterList.size(); i++){

                if(filterList.get(i).getName().toUpperCase().contains(constraint)){
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

        adapterUsers.usersArrayList = (ArrayList<ModelUser>)results.values;

        adapterUsers.notifyDataSetChanged();

    }
}

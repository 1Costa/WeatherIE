package com.example.konstantin.weatherie.helpers;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.konstantin.weatherie.model.PlaceAPI;

import java.util.ArrayList;

/**
 * Created by Konstantin on 10/01/2017.
 */

public class LocationsSearch  extends AppCompatActivity {

//    class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
//
//        ArrayList<String> resultList;
//
//        Context mContext;
//        int mResource;
//
//        PlaceAPI mPlaceAPI = new PlaceAPI();
//
//        public PlacesAutoCompleteAdapter(Context context, int resource) {
//            super(context, resource);
//
//            mContext = context;
//            mResource = resource;
//        }
//
//        @Override
//        public int getCount() {
//            // Last item will be the footer
//            return resultList.size();
//        }
//
//        @Override
//        public String getItem(int position) {
//            return resultList.get(position);
//        }
//
//        @Override
//        public Filter getFilter() {
//            Filter filter = new Filter() {
//                @Override
//                protected FilterResults performFiltering(CharSequence constraint) {
//                    FilterResults filterResults = new FilterResults();
//                    if (constraint != null) {
//                        resultList = mPlaceAPI.autocomplete(constraint.toString());
//
//                        filterResults.values = resultList;
//                        filterResults.count = resultList.size();
//                    }
//
//                    return filterResults;
//                }
//
//                @Override
//                protected void publishResults(CharSequence constraint, FilterResults results) {
//                    if (results != null && results.count > 0) {
//                        notifyDataSetChanged();
//                    }
//                    else {
//                        notifyDataSetInvalidated();
//                    }
//                }
//            };
//
//            return filter;
//        }
//    }
}

package com.example.konstantin.weatherie.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.konstantin.weatherie.FiveDaysForecastActivity;
import com.example.konstantin.weatherie.R;

/**
 * Created by Konstantin on 20/02/2017.
 */

public class RecyclerViewFiveDaysFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FiveDaysForecastActivity fiveDaysForecastActivity = (FiveDaysForecastActivity) getActivity();
        recyclerView.setAdapter(fiveDaysForecastActivity.getAdapter(bundle.getInt("day")));
        return view;
    }
}

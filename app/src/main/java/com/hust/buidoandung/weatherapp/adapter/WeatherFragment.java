package com.hust.buidoandung.weatherapp.adapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hust.buidoandung.weatherapp.MainActivity;
import com.hust.buidoandung.weatherapp.R;

public class WeatherFragment extends Fragment {


    public WeatherFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        //inflate doi tuong view.
        View view = inflater.inflate(R.layout.recycler_view_weather, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //lay activity  dung fragment nay
        MainActivity mainActivity = (MainActivity) getActivity();
        //thiet lap adapter cho no
        recyclerView.setAdapter(mainActivity.getAdapter(bundle.getInt("day")));
        return view;
    }

}

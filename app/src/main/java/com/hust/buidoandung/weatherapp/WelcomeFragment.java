package com.hust.buidoandung.weatherapp;


import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment {

    TextView title;
    TextView content;
    TextView help;
    RelativeLayout layout;
    ImageView icon;
    public WelcomeFragment() {
        // Required empty public constructor
    }
    public void populateForm(int icon, String... texts) {
        title.setText(texts[0]);
        content.setText(texts[1]);
        if (texts.length == 3) {
            help.setText(texts[2]);
        }
        this.icon.setImageResource(icon);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int step = getArguments().getInt("step");

        View v = inflater.inflate(R.layout.fragment_welcome, container, false);
        layout = v.findViewById(R.id.welcomeFragmentHolder);
        title = v.findViewById(R.id.txtTitle);
        content = v.findViewById(R.id.txtContent);
        help = v.findViewById(R.id.txtHelp);
        icon = v.findViewById(R.id.imgWelcome);
        switch (step) {
            case 1:
                populateForm(R.drawable.sun, "Greeting",
                        "Welcome to our weather app!", "Swipe to find out more →");
                break;
            case 2:
                populateForm(R.drawable.wind, "Weather info in hands",
                        "Get weather infomation with you anywhere, anytime with our " +
                                "robust OpenWeatherMap API!");
                break;
            case 3:
                populateForm(R.drawable.moon, "More day forecast",
                        "Don't be afraid to go on a long trip! We provide more than 3 days forecast feature!");
                break;
            case 4:
                populateForm(R.drawable.rain, "Detect weather",
                        "We using GPS to dectect your weather and show you your weather is here!");
                break;
            case 5:
                populateForm(R.drawable.cloud, "And more...",
                        "We have a lot more interesting functionality. Go ahead and explore them yourself!");
                break;
        }
        return v;
    }
    public static WelcomeFragment newInstance(int step) {
        Bundle bundle = new Bundle();
        bundle.putInt("step", step);

        WelcomeFragment fragment = new WelcomeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}

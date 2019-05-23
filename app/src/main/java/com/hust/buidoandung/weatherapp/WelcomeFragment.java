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

public class WelcomeFragment extends Fragment {

    TextView title;
    TextView content;
    TextView help;
    RelativeLayout layout;
    ImageView icon;
    public WelcomeFragment() {

    }
    //Do du lieu
    public void setValue(int icon, String... texts) {
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
        //lay ra thong tin step thu may
        int step = getArguments().getInt("step");

        View v = inflater.inflate(R.layout.fragment_welcome, container, false);
        layout = v.findViewById(R.id.welcomeFragmentHolder);
        title = v.findViewById(R.id.txtTitle);
        content = v.findViewById(R.id.txtContent);
        help = v.findViewById(R.id.txtHelp);
        icon = v.findViewById(R.id.imgWelcome);
        switch (step) {
            case 1:
                setValue(R.drawable.sun, "Hi",
                        "Welcome to weather app !", "Swipe to find out more →");
                break;
            case 2:
                setValue(R.drawable.rain, "The life is easy",
                        "With weather app, you never have to be afraid of the sudden rain!");
                break;
            case 3:
                setValue(R.drawable.moon, "More day forecast",
                        "We provide more than 3 days forecast feature!");
                break;
            case 4:
                setValue(R.drawable.gps, "Detect weather",
                        "We can using GPS to dectect your weather and show you your weather is here!");
                break;
            case 5:
                setValue(R.drawable.cloud, "And more...",
                        "We have a lot more interesting functionality. Go ahead and explore them yourself!");
                break;
        }
        return v;
    }
    //craete new WelcomeFragment
    public static WelcomeFragment newInstance(int step) {
        Bundle bundle = new Bundle();
        bundle.putInt("step", step);

        WelcomeFragment fragment = new WelcomeFragment();
        //truyen doi tuong bundle sang de co the biet fragment so may duoc tao
        fragment.setArguments(bundle);
        return fragment;
    }
}

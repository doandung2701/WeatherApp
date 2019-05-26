package com.hust.buidoandung.weatherapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hust.buidoandung.weatherapp.WelcomeFragment;

import java.util.ArrayList;
import java.util.List;

public class WelcomePagerAdapter extends FragmentPagerAdapter {
    //danh sach phàn tử
    private final List<WelcomeFragment> fragmentList ;

    public WelcomePagerAdapter(FragmentManager fm,List<WelcomeFragment> fragments) {
        super(fm);
        this.fragmentList=fragments;
    }
    //lấy phần tử thứ i
    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }
    //trả về tổng phần tử
    @Override
    public int getCount() {
        return fragmentList.size();
    }
}

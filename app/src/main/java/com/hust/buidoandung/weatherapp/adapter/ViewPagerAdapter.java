package com.hust.buidoandung.weatherapp.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    //danh sách các phần tử
    private final List<Fragment> fragmentList = new ArrayList<>();
    //danh sách title của phần tử
    private final List<String> fragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
    //thêm mới 1 fragment
    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
    }
        //lấy tên fragment
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return  fragmentTitleList.get(position);
    }
}

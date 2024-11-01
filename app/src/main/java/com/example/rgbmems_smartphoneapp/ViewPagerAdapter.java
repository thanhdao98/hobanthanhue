package com.example.rgbmems_smartphoneapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FirstFragment();
            case 1:
                return new SecondFragment();
            default:
                return new FirstFragment(); // Default case
        }
    }

    @Override
    public int getItemCount() {

        return 2; // Number of fragments
    }
}


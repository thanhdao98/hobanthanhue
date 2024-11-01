package com.example.rgbmems_smartphoneapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private CustomViewPager customViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViewPager();
    }

    private void setupViewPager() {
        customViewPager = findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        customViewPager.setAdapter(adapter);

        customViewPager.addOnPageChangeListener(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSwipeState(position);
            }
        });
    }

    private void updateSwipeState(int position) {
        ViewPagerAdapter adapter = (ViewPagerAdapter) customViewPager.getViewPager().getAdapter();
        if (adapter != null) {
            Fragment fragment = adapter.createFragment(position);
            if (fragment instanceof SecondFragment) {
                SecondFragment secondFragment = (SecondFragment) fragment;
                int ToolMode = secondFragment.isToolMode();
                if (ToolMode == 0){ // ニュートラル状態のみスワイプを許可
                    customViewPager.setSwipeEnabled(true);
                }else{
                    customViewPager.setSwipeEnabled(false);
                }
            } else {
                customViewPager.setSwipeEnabled(true);
            }
        }
    }
}


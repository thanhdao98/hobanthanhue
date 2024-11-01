package com.example.rgbmems_smartphoneapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

public class CustomViewPager extends FrameLayout {
    private boolean isSwipeEnabled = true;
    private ViewPager2 viewPager;

    public CustomViewPager(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // Inflate layout custom cho ViewPager
        LayoutInflater.from(context).inflate(R.layout.custom_view_pager, this, true);

        // Create a new instance of ViewPager2
        viewPager = new ViewPager2(context);
        // Add ViewPager2 to CustomViewPager
        addView(viewPager, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Set up listener to prevent swiping if isSwipeEnabled is false
        viewPager.setUserInputEnabled(isSwipeEnabled);
    }

    public void setSwipeEnabled(boolean enabled) {
        this.isSwipeEnabled = enabled;
        viewPager.setUserInputEnabled(enabled);
    }

    // Set adapter
    public void setAdapter(ViewPagerAdapter adapter) {
        viewPager.setAdapter(adapter);
    }

    // Get ViewPager2
    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public void addOnPageChangeListener(ViewPager2.OnPageChangeCallback onPageChangeCallback) {
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
    }
}


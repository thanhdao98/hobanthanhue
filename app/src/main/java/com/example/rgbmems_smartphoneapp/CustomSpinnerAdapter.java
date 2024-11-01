package com.example.rgbmems_smartphoneapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private int selectedPosition = -1; // Default selected position

    public CustomSpinnerAdapter(Context context, String[] items) {
        super(context, R.layout.spinner_item, items);
    }

    // Update the selected position
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // Notify adapter to refresh
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, true); // Dropdown view
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, false); // Main view
    }

    @SuppressLint("SetTextI18n")
    private View getCustomView(int position, View convertView, ViewGroup parent, boolean isDropdown) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View row = inflater.inflate(R.layout.spinner_item, parent, false);
        TextView textView = row.findViewById(R.id.textViewItem);
        View divider = row.findViewById(R.id.divider);

        // Displaying checkmark for selected item in dropdown list
        if (isDropdown) {
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL); // Align right
            if (position == selectedPosition) {
                textView.setText("✓︎   " + getItem(position)); // Add check mark
            } else {
                textView.setText(getItem(position)); // Plain item text
            }

            // Only show divider if it's not the last item
            if (position == getCount() - 1) {
                divider.setVisibility(View.GONE); // Hide divider for the last item
            } else {
                divider.setVisibility(View.VISIBLE);
            }
        } else {
            textView.setText(getItem(position)); // Display item text in main Spinner view
            textView.setGravity(Gravity.CENTER); // Center align
            divider.setVisibility(View.GONE); // No divider in main Spinner view
        }

        return row;
    }
}

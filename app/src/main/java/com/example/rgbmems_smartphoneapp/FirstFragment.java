package com.example.rgbmems_smartphoneapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.ListPopupWindow;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {
    private Switch toggle_connect;
    private Switch toggle_onoff;
    private PendingMessage pendingMessage = null; // Store the pending message to be sent
    private ConnectToServer connectToServer;
    private Button sendButton;
    private String type = "";
    private int value;
    private TextView responseTextView;
    private ConnectionViewModel connectionViewModel;
    // Declare a handler for retrying connections
    private Handler retryHandler = new Handler();
    private Runnable retryRunnable;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        // Initialize ViewModel
        connectionViewModel = new ViewModelProvider(requireActivity()).get(ConnectionViewModel.class);

        // Initialize the views here
        responseTextView = view.findViewById(R.id.responseTextView);
        toggle_connect = view.findViewById(R.id.switch1);
        toggle_onoff = view.findViewById(R.id.switch2);
        sendButton = view.findViewById(R.id.button3);

        // Initialize ConnectToServer
        connectToServer = new ConnectToServer();
        connectToServer.setResponseTextView(responseTextView); // Assign the TextView to the ConnectToServer class

        // Set up the spinner
        setupSpinner(view);

        // Set initial state and connect to server
        toggle_connect.setChecked(true); // Set switch1 to ON
        handleToggleConnect(true); // Connect to the server
        connectToServer.setConnectionViewModel(connectionViewModel); // Call method to set up ViewModel

        // Handle the event when the state of toggle_connect changes
        toggle_connect.setOnCheckedChangeListener((buttonView, isChecked) -> handleToggleConnect(isChecked));

        // Handle the event when the state of toggle_onoff changes
        toggle_onoff.setOnCheckedChangeListener((buttonView, isChecked) -> handleToggleOnOff(isChecked));

        // Handle the event when the sendButton is clicked
        sendButton.setOnClickListener(v -> handleSendButtonClick());

        return view; // Return the inflated view
    }

    private void setupSpinner(View view) {
        Spinner dropdown = view.findViewById(R.id.spinner); // Get the Spinner from the inflated view
        List<String> numbersList = new ArrayList<>();
        for (int i = 0; i <= 99; i++) {
            numbersList.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, numbersList);
        dropdown.setAdapter(adapter);

        ListPopupWindow listPopupWindow = new ListPopupWindow(requireContext());
        listPopupWindow.setAnchorView(dropdown);
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setHeight(450);
        listPopupWindow.setModal(true);

        dropdown.setOnTouchListener((v, event) -> {
            listPopupWindow.show();
            return true;
        });

        listPopupWindow.setOnItemClickListener((parent, view1, position, id) -> {
            dropdown.setSelection(position);
            listPopupWindow.dismiss();
        });
    }

    private void handleToggleConnect(boolean isChecked) {
        if (isChecked) {
            // When Switch 1 is turned on, establish a connection to the server
            connectToServer.connectToServer(getActivity());
            // Set up retry mechanism if connection fails
            retryRunnable = new Runnable() {
                @Override
                public void run() {
                    Boolean isConnected = connectionViewModel.getConnectionStatus().getValue();

                    // Check for null and ensure the connection status is false
                    if (isConnected == null || !isConnected) {
                        Log.d("ConnectServer", "Retrying server connection...");
                        connectToServer.connectToServer(getActivity());
                        retryHandler.postDelayed(this, 5000); // Retry every 5 seconds
                    } else {
                        // Stop retrying once connected
                        retryHandler.removeCallbacks(retryRunnable);
                        Log.d("ConnectServer", "Connection established, stopping retries.");
                    }
                }
            };
            retryHandler.postDelayed(retryRunnable, 5000); // Start retrying in 5 seconds
        } else {
            // When Switch 1 is turned off, disconnect from the server
            connectToServer.disconnect(); // Call the disconnect method
            connectionViewModel.setConnectionStatus(false); // Update connection status
            connectToServer.updateResponseText("切断");
            retryHandler.removeCallbacks(retryRunnable); // Stop retries if toggle is off
        }
    }

    private void handleToggleOnOff(boolean isChecked) {
        type = "turnonoff";
        value = isChecked ? 1 : 0;

        if (toggle_connect.isChecked()) {
            if (connectToServer.isConnected()){
                // If Switch 1 is on, send the message immediately
                connectToServer.sendMessageToServer(type, value);
            } else {
                connectToServer.setPendingMessage(type, value);
            }
        } else {
            // If Switch 1 is off, store the pending message to be sent later
            connectToServer.setPendingMessage(type, value);
        }
    }

    private void handleSendButtonClick() {
        type = "sendnumber";
        Spinner dropdown = getView().findViewById(R.id.spinner); // Use getView() to access the Spinner
        int selectedNumber = Integer.parseInt(dropdown.getSelectedItem().toString());
        if (connectToServer.isConnected()) {
            connectToServer.sendMessageToServer(type, selectedNumber);
        } else {
            connectToServer.setPendingMessage(type, selectedNumber); // Store the pending message to be sent later
        }
    }
    // Flag variable to track connection status
    private boolean isResumedConnectionAttempted = false;

    @Override
    public void onResume() {
        super.onResume();
        if (toggle_connect.isChecked()) {
            Log.d("ConnectionStatus", "toggle_connect is ON. Attempting to connect to server.");
            if (!isResumedConnectionAttempted || !connectToServer.isConnected()) {
                // Ensure only one connection attempt in onResume
                isResumedConnectionAttempted = true;
                // Ensure the server connection is established when the fragment becomes visible
                connectToServer.connectToServer(requireActivity());
                Log.d("ConnectionStatus", "Attempting to connect to server in onResume.");
                if (!connectToServer.isConnected()){
                    Log.d("ConnectServer", "Failed to connect to server");
                }
                if (pendingMessage != null) {
                    connectToServer.sendMessageToServer(pendingMessage.getName(), pendingMessage.getCheckNumber());
                    pendingMessage = null; // Clear the pending message after sending
                }
            } else {
                Log.d("ConnectionStatus", "Already connected to server.");
            }
        } else {
            Log.d("ConnectionStatus", "toggle_connect is off. Not connected to server.");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // When disconnected
        connectionViewModel.setConnectionStatus(false); // Update connection status
    }
}

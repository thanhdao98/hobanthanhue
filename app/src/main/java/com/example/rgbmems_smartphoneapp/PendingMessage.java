package com.example.rgbmems_smartphoneapp;

// Class to represent a message that is pending to be sent
public class PendingMessage {
    private String type;
    private int value;

    // Constructor to initialize the PendingMessage with type and value
    public PendingMessage(String type, int value) {
        this.type = type;
        this.value = value;
    }

    // Getter for the message type
    public String getName() {
        return type;
    }

    // Getter for the message value
    public int getCheckNumber() {
        return value;
    }
}


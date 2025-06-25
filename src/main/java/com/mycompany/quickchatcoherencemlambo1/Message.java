/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quickchatcoherencemlambo1;

/**
 *
 * @author lab_services_student
 */
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Message {
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String content;
    private final String sender;
    private final String timestamp;

    // Constructor for new messages
    public Message(String messageID, int messageNumber, String recipient, String content, String sender) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.content = content;
        this.sender = sender;
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // Constructor for loading from JSON with existing timestamp
    public Message(String messageID, int messageNumber, String recipient, String content, String sender, String timestamp) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public static boolean isValidRecipient(String recipient) {
        return recipient != null && 
               recipient.matches("^\\+?[0-9]{10,13}$") &&
               recipient.length() <= 13;
    }

    public static String checkMessageLength(String message) {
        if (message == null) return "Message cannot be null";
        if (message.length() > 250) 
            return "Message too long (" + message.length() + "/250 characters)";
        return "Success";
    }

    public static String generateMessageID() {
        // Use UUID for unique ID
        return UUID.randomUUID().toString();
    }

    public String selectMessageAction() {
        String[] options = {"Send Message", "Store Message", "Disregard Message"};
        return (String) JOptionPane.showInputDialog(
            null,
            "Choose action for this message:",
            "Message Action",
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );
    }

    public String createMessageHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = sender + recipient + content + timestamp;
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback
            return Integer.toHexString((sender + recipient + content + timestamp).hashCode());
        }
    }

    public String fullMessageDetails() {
        return "Message ID: " + messageID + "\n" +
               "From: " + sender + "\n" +
               "To: " + recipient + "\n" +
               "Timestamp: " + timestamp + "\n" +
               "Hash: " + createMessageHash() + "\n" +
               "Content: " + content;
    }

    public String shortDetails() {
        String preview = content.length() > 20 ? content.substring(0, 20) + "..." : content;
        return "[" + messageID + "] " + sender + " → " + recipient + ": " + preview;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("messageID", messageID);
        obj.put("messageNumber", messageNumber);
        obj.put("recipient", recipient);
        obj.put("content", content);
        obj.put("sender", sender);
        obj.put("timestamp", timestamp);
        return obj;
    }

    // Getters
    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getContent() { return content; }
    public String getSender() { return sender; }
    public String getTimestamp() { return timestamp; }
}

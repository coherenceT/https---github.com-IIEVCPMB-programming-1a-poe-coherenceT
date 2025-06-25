package com.mycompany.quickchatcoherencemlambo1;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuickChatCoherenceMlambo1 {
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final Set<String> messageHashes = new HashSet<>();
    private static final Set<String> messageIDs = new HashSet<>();
    
    private static int totalMessagesSent = 0;
    private static String currentUser;

    public static void main(String[] args) {
        // User login
        currentUser = JOptionPane.showInputDialog(null,
                "Welcome to QuickChat by Coherence Tasimba Mlambo.\nEnter your username:");
        if (currentUser == null || currentUser.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Login failed. Exiting...");
            System.exit(0);
        }
        
        JOptionPane.showMessageDialog(null, "Welcome, " + currentUser + "!");
        loadMessagesFromFile();
        
        // Main application loop
        while (true) {
            String menu = "QuickChat Menu:\n"
                    + "1) Send Messages\n"
                    + "2) View Recent Messages\n"
                    + "3) Advanced Features\n"
                    + "4) Exit";
            
            String choice = JOptionPane.showInputDialog(null, menu);
            if (choice == null) System.exit(0);
            
            switch (choice) {
                case "1": sendMessages(); break;
                case "2": displayRecentMessages(); break;
                case "3": showAdvancedMenu(); break;
                case "4": exitApplication(); break;
                default: JOptionPane.showMessageDialog(null, "Invalid choice!");
            }
        }
    }

    private static void sendMessages() {
        int numMessages = getMessageCount();
        if (numMessages <= 0) return;
        
        for (int i = 0; i < numMessages; i++) {
            // Get recipient
            String recipient = getValidRecipient();
            if (recipient == null) return;
            
            // Get content
            String content = getValidContent();
            if (content == null) return;
            
            // Create message
            String messageId = generateUniqueID();
            int messageNum = totalMessagesSent + i + 1;
            Message msg = new Message(messageId, messageNum, recipient, content, currentUser);
            
            // Process message
            String action = msg.selectMessageAction();
            if (action == null) continue;
            
            switch (action) {
                case "Send Message":
                    sentMessages.add(msg);
                    messageHashes.add(msg.createMessageHash());
                    messageIDs.add(messageId);
                    totalMessagesSent++;
                    JOptionPane.showMessageDialog(null, "Message sent!\n" + msg.shortDetails());
                    break;
                case "Store Message":
                    storedMessages.add(msg);
                    messageHashes.add(msg.createMessageHash());
                    messageIDs.add(messageId);
                    JOptionPane.showMessageDialog(null, "Message stored!");
                    break;
                case "Disregard Message":
                    disregardedMessages.add(msg);
                    JOptionPane.showMessageDialog(null, "Message disregarded.");
                    break;
            }
        }
        saveMessagesToFile();
    }

    private static int getMessageCount() {
        while (true) {
            String input = JOptionPane.showInputDialog("How many messages do you want to send?");
            if (input == null) return 0;
            
            try {
                int count = Integer.parseInt(input);
                if (count > 0) return count;
                JOptionPane.showMessageDialog(null, "Please enter a positive number");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid number format");
            }
        }
    }

    private static String getValidRecipient() {
        while (true) {
            String recipient = JOptionPane.showInputDialog("Enter recipient (e.g., +27123456789):");
            if (recipient == null) return null;
            
            if (Message.isValidRecipient(recipient)) {
                return recipient;
            }
            JOptionPane.showMessageDialog(null, "Invalid recipient format. Use international format.");
        }
    }

    private static String getValidContent() {
        while (true) {
            String content = JOptionPane.showInputDialog("Enter message (max 250 characters):");
            if (content == null) return null;
            
            String validation = Message.checkMessageLength(content);
            if (validation.equals("Success")) {
                return content;
            }
            JOptionPane.showMessageDialog(null, validation);
        }
    }

    private static String generateUniqueID() {
        String id;
        do {
            id = Message.generateMessageID();
        } while (messageIDs.contains(id));
        return id;
    }

    // File handling methods
    @SuppressWarnings("unchecked")
    private static void loadMessagesFromFile() {
        File file = new File("chat_history.json");
        if (!file.exists()) return;
        
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            JSONArray jsonArray = (JSONArray) parser.parse(reader);
            
            for (Object obj : jsonArray) {
                JSONObject json = (JSONObject) obj;
                String id = (String) json.get("messageID");
                int num = ((Long) json.get("messageNumber")).intValue();
                String recipient = (String) json.get("recipient");
                String content = (String) json.get("content");
                String sender = (String) json.get("sender");
                String status = (String) json.get("status");
                
                Message msg = new Message(id, num, recipient, content, sender);
                switch (status) {
                    case "sent": sentMessages.add(msg); break;
                    case "stored": storedMessages.add(msg); break;
                    case "disregarded": disregardedMessages.add(msg); break;
                }
                
                messageIDs.add(id);
                messageHashes.add(msg.createMessageHash());
                if (status.equals("sent")) totalMessagesSent++;
            }
        } catch (IOException | ParseException e) {
            JOptionPane.showMessageDialog(null, "Error loading messages: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void saveMessagesToFile() {
        JSONArray jsonArray = new JSONArray();
        
        addMessagesToArray(jsonArray, sentMessages, "sent");
        addMessagesToArray(jsonArray, storedMessages, "stored");
        addMessagesToArray(jsonArray, disregardedMessages, "disregarded");
        
        try (FileWriter file = new FileWriter("chat_history.json")) {
            file.write(jsonArray.toJSONString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving messages: " + e.getMessage());
        }
    }

    private static void addMessagesToArray(JSONArray array, List<Message> messages, String status) {
        for (Message msg : messages) {
            JSONObject json = msg.toJSON();
            json.put("status", status);
            array.add(json);
        }
    }

    private static void displayRecentMessages() {
        StringBuilder sb = new StringBuilder("=== MESSAGE HISTORY ===\n\n");
        
        if (sentMessages.isEmpty() && storedMessages.isEmpty() && disregardedMessages.isEmpty()) {
            sb.append("No messages found");
        } else {
            sb.append("SENT MESSAGES:\n");
            for (Message msg : sentMessages) {
                sb.append(msg.shortDetails()).append("\n\n");
            }
            
            sb.append("\nSTORED MESSAGES:\n");
            for (Message msg : storedMessages) {
                sb.append(msg.shortDetails()).append("\n\n");
            }
            
            sb.append("\nDISREGARDED MESSAGES:\n");
            for (Message msg : disregardedMessages) {
                sb.append(msg.shortDetails()).append("\n\n");
            }
        }
        
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private static void showAdvancedMenu() {
        while (true) {
            String menu = "ADVANCED FEATURES:\n"
                    + "1) Sender/Recipient Pairs\n"
                    + "2) Longest Message\n"
                    + "3) Search by Message ID\n"
                    + "4) Search by Recipient\n"
                    + "5) Delete by Hash\n"
                    + "6) Full Message Report\n"
                    + "7) Back to Main Menu";
            
            String choice = JOptionPane.showInputDialog(null, menu);
            if (choice == null || choice.equals("7")) return;
            
            switch (choice) {
                case "1": showSenderRecipients(); break;
                case "2": showLongestMessage(); break;
                case "3": searchByID(); break;
                case "4": searchByRecipient(); break;
                case "5": deleteByHash(); break;
                case "6": showFullReport(); break;
                default: JOptionPane.showMessageDialog(null, "Invalid choice!");
            }
        }
    }

    private static void showSenderRecipients() {
        StringBuilder sb = new StringBuilder("SENDER/RECIPIENT PAIRS:\n\n");
        for (Message msg : sentMessages) {
            sb.append(msg.getSender()).append(" → ").append(msg.getRecipient()).append("\n");
        }
        showResult(sb.toString(), "Sender/Recipient Pairs");
    }

    private static void showLongestMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages found!");
            return;
        }
        
        Message longest = Collections.max(sentMessages, 
            Comparator.comparingInt(m -> m.getContent().length()));
            
        String result = "LONGEST MESSAGE (" + longest.getContent().length() + " chars):\n" +
                        "From: " + longest.getSender() + "\n" +
                        "To: " + longest.getRecipient() + "\n" +
                        "Content: " + longest.getContent();
        showResult(result, "Longest Message");
    }

    private static void searchByID() {
        String id = JOptionPane.showInputDialog("Enter Message ID:");
        if (id == null || id.trim().isEmpty()) return;
        
        for (Message msg : sentMessages) {
            if (msg.getMessageID().equals(id)) {
                showResult(msg.fullMessageDetails(), "Message Found");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message not found");
    }

    private static void searchByRecipient() {
        String recipient = JOptionPane.showInputDialog("Enter recipient number:");
        if (recipient == null) return;
        
        StringBuilder sb = new StringBuilder("MESSAGES TO " + recipient + ":\n\n");
        for (Message msg : sentMessages) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("From: ").append(msg.getSender()).append("\n")
                  .append("Message: ").append(msg.getContent()).append("\n\n");
            }
        }
        showResult(sb.toString(), "Messages to " + recipient);
    }

    private static void deleteByHash() {
        String hash = JOptionPane.showInputDialog("Enter message hash:");
        if (hash == null) return;
        
        Iterator<Message> iterator = sentMessages.iterator();
        while (iterator.hasNext()) {
            Message msg = iterator.next();
            if (msg.createMessageHash().equals(hash)) {
                iterator.remove();
                messageHashes.remove(hash);
                messageIDs.remove(msg.getMessageID());
                saveMessagesToFile();
                JOptionPane.showMessageDialog(null, "Message deleted");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message not found");
    }

    private static void showFullReport() {
        StringBuilder sb = new StringBuilder("FULL MESSAGE REPORT:\n\n");
        for (Message msg : sentMessages) {
            sb.append("Hash: ").append(msg.createMessageHash()).append("\n")
              .append("Recipient: ").append(msg.getRecipient()).append("\n")
              .append("Content: ").append(msg.getContent()).append("\n\n");
        }
        showResult(sb.toString(), "Full Message Report");
    }

    private static void showResult(String content, String title) {
        JTextArea textArea = new JTextArea(content);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private static void exitApplication() {
        JOptionPane.showMessageDialog(null, 
            "Goodbye, " + currentUser + "!\nTotal messages sent: " + totalMessagesSent);
        System.exit(0);
    }
}